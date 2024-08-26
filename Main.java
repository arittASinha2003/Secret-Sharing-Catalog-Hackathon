import org.json.JSONObject;
import org.json.JSONArray;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static BigInteger modInverse(BigInteger a, BigInteger prime) {
        return a.modInverse(prime);
    }

    public static BigInteger convertToDecimal(int base, String value) {
        return new BigInteger(value, base);
    }

    public static BigInteger interpolate(BigInteger x, List<BigInteger> xValues, List<BigInteger> yValues, BigInteger prime) {
        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < xValues.size(); i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < xValues.size(); j++) {
                if (i != j) {
                    numerator = numerator.multiply(x.subtract(xValues.get(j))).mod(prime);
                    denominator = denominator.multiply(xValues.get(i).subtract(xValues.get(j))).mod(prime);
                }
            }

            BigInteger lagrangeTerm = yValues.get(i).multiply(numerator).multiply(modInverse(denominator, prime)).mod(prime);
            secret = secret.add(lagrangeTerm).mod(prime);
        }

        return secret;
    }

    public static BigInteger reconstructSecret(List<BigInteger[]> shares, BigInteger prime) {
        List<BigInteger> xValues = new ArrayList<>();
        List<BigInteger> yValues = new ArrayList<>();

        for (BigInteger[] share : shares) {
            xValues.add(share[0]);
            yValues.add(share[1]);
        }

        return interpolate(BigInteger.ZERO, xValues, yValues, prime);
    }

    public static BigInteger processTestCase(JSONObject testCase, BigInteger prime) {
        JSONObject keys = testCase.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        List<BigInteger[]> shares = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            if (testCase.has(String.valueOf(i))) {
                JSONObject share = testCase.getJSONObject(String.valueOf(i));
                int base = Integer.parseInt(share.getString("base"));
                String value = share.getString("value");
                BigInteger decimalValue = convertToDecimal(base, value);
                shares.add(new BigInteger[]{BigInteger.valueOf(i), decimalValue});
            }
        }

        List<BigInteger[]> chosenShares = shares.subList(0, k);
        return reconstructSecret(chosenShares, prime);
    }

    public static void main(String[] args) {
        try {
            // Reading JSON content from file
            String content = new String(Files.readAllBytes(Paths.get("data.json")));
            JSONObject testCase = new JSONObject(content);

            // Choosing a large enough prime for the calculations
            BigInteger prime = BigInteger.valueOf(2).pow(61).subtract(BigInteger.ONE);

            // Processing the test case from the JSON file
            BigInteger reconstructedSecret = processTestCase(testCase, prime);
            System.out.println("Reconstructed Secret: " + reconstructedSecret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
