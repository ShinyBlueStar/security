package com.sample.system.vault.service.domain.utility;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

public class Utility {

    private static final Logger log = LogManager.getLogger(Utility.class);
    private static Random random = new Random();

    public static String pad(String number, int len) {
        String normal = number;
        while (normal.length() < len) {
            normal = "0" + normal;
        }
        return normal;
    }

    public static boolean isAllowedList(String allowedList, String value) {
        if (!StringUtils.hasText(allowedList)) {
            log.info("value for ip list is null");
            return true;
        }
        List<String> ipList = new LinkedList<>(Arrays.asList(allowedList.split(",")));

        if (!ipList.contains(value)) {
            log.error("value: {} isn't in valid list", value);
            return false;
        }
        return true;
    }

    public static boolean isNull(String data) {
        return !org.springframework.util.StringUtils.hasText(data);
    }

    public static String addComma(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public static int generateRandomNumber(int min, int max) {
        return random.ints(min, (max + 1)).findFirst().getAsInt();
    }

    public static String cleanPhoneNumber(String mobile) {

        if (StringUtils.hasText(mobile)) {
            String onlyNumber = mobile.replaceAll("\\D", "");
            if (onlyNumber.startsWith("09")) {
                return onlyNumber.replaceFirst("^09", "9");
            }
            if (onlyNumber.startsWith("989")) {
                return onlyNumber.replaceFirst("^989", "9");
            }
            if (onlyNumber.startsWith("+989")) {
                return onlyNumber.replaceFirst("\\+989", "9");
            }
            if (onlyNumber.startsWith("9")) {
                return mobile;
            }
        }
        return mobile;
    }

    public static String cleanPhoneNumberAddZero(String mobile) {
        if (isNull(mobile)) {
            return mobile;
        }

        mobile = mobile.replaceAll("\\D", "");

        if (mobile.startsWith("09")) {
            return mobile;
        }
        if (mobile.startsWith("989")) {
            return mobile.replaceAll("^989", "09");
        }
        if (mobile.startsWith("9")) {
            return mobile.replaceAll("^9", "09");
        }
        return null;
    }

    public static String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static String getCallerClassAndMethodName() {
        return Thread.currentThread().getStackTrace()[2].getClassName() + '.' +
                Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static String createResponse(String id, Long amount) {
        return id + "|" + amount;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    /*private static boolean verifySignature(byte[] data, byte[] signature, String publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(publicKey));
        sig.update(data);
        return sig.verify(org.bouncycastle.util.encoders.Base64.decode(signature));
    }

    private static PublicKey getPublic(String key) throws Exception {

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }*/

   /* public static void main(String[] args) {
        String input = "10000|0079993141|1|98a3f433-ac26-42ad-9b0d-227a121250df";

        try {
            String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzwYeMHB6XDoHpizJ785KcabXAAGsvaQ/ACnZtHNEoO/fSmRsBKERUMWt1GRUbMuX7ZOimA/sxGtZptV/wI+P3LuVpCKoVLeR0UIPrUUkXlEQ9UFPYHZHg2eWftTssI1EQOEcq/HcDo3/6lCtnepzpJ2AKMuXtWNQNx7gd4m/L7mG5jiLSjL3NJ+bMU+iNoeF6frqxewJMBx3ugeQRqL+m9VZZeDkOxwIo1SxYLdJgtXlqNY5PUDO/6CEKmGRx3eQRVEq1E5gkpET7QvBY/U5avdYuRptlr5SuMM+iOHrqptPsRY053ddpw3JkXKXKtnIlADjuUBe6LkVI5YwE8bXWwIDAQAB";

            String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPBh4wcHpcOgem\n" +
                    "LMnvzkpxptcAAay9pD8AKdm0c0Sg799KZGwEoRFQxa3UZFRsy5ftk6KYD+zEa1mm\n" +
                    "1X/Aj4/cu5WkIqhUt5HRQg+tRSReURD1QU9gdkeDZ5Z+1OywjURA4Ryr8dwOjf/q\n" +
                    "UK2d6nOknYAoy5e1Y1A3HuB3ib8vuYbmOItKMvc0n5sxT6I2h4Xp+urF7AkwHHe6\n" +
                    "B5BGov6b1Vll4OQ7HAijVLFgt0mC1eWo1jk9QM7/oIQqYZHHd5BFUSrUTmCSkRPt\n" +
                    "C8Fj9Tlq91i5Gm2WvlK4wz6I4euqm0+xFjTnd12nDcmRcpcq2ciUAOO5QF7ouRUj\n" +
                    "ljATxtdbAgMBAAECggEAUKZGfoJi+KjWsAMEzDomQC5J1cPRQrPIo0yqdiTtmHC6\n" +
                    "ISYL+qWwtDG+bV6EkTmjPzdjgS+7Ai376AWGVkLXPZuKST4DK7WzxbyhlNO5vlCA\n" +
                    "dbryrFaHt4ZUV6alaoYuD8Riwg1ft//TsbmqWTmrwXZmJf5iZJSC/GY39fmglHtT\n" +
                    "u1MHSveU/mq0YqurzUYKhHBiXYEww8c4cLArAtpMmpPPJNL8bMiK4UmfG6Fz4jju\n" +
                    "NsQXkxpqlqNO7+BfF7je7pnLj3i2LgWHtosF6RG3ZiQn6DXs0/DjvOi+S+uQ6xxn\n" +
                    "gjBWZtjwShINgGKyg9Oy4H9wMkEyLrD335tpaNMR4QKBgQDuTSf6ueDMi/RqZUvR\n" +
                    "4o1CEBW2UCvzyLBw4f3m/P/UPV8mR2Kq2af81tuKK8MMJ6Ajg1rkuqAcin08fEr/\n" +
                    "KGLpERME0hV8N3BUAjgoEt7EMmeeWEkzV6IVu31gKEUX5Y7lAXaSiszD8g+DdU9J\n" +
                    "HNE75f24QbjxDCraQysTn3XwkQKBgQDeZke8u5vgLZma7eK4SKYM1owbUsU+rPCC\n" +
                    "+rmje1aVo6yUPAqZmjJSgtHZJ2l14cO3dezu0wEj32pQ0qTELI1m7RVtVpjQZRPg\n" +
                    "rMMMvKbAi56oZWQsVyNM7J+MXqYQUO6rnXdW94fJA7Ej50DdliDJrqQTmHviN15s\n" +
                    "7P/cpmf/KwKBgQCKarClNyC3TzfaMRp4QELSs6sY6bqN8O1jtDEZ4azr5/YDswVB\n" +
                    "vgmQmHCO8lpqDf47goniP/DOgza5UmzxhtDlFfDZJPor27vYYC2kQUm55pk/ZYKn\n" +
                    "Wuif/PaXSuzPM5zrsgzgk9TaoBiYwCQckKuMQkw8oZg+E1Y0zz0POdl0sQKBgHxq\n" +
                    "baFotvf/qpngkOAVTEeMUs8KS+wXJWmwx99sJXELIoW+3RM3DwXXx3ubL8VRqoLc\n" +
                    "xtKIWm/uEXTrkl9oqSY2ZbFIK68RjMA5Bdj/RK4crYJ5TkP773SeP1dwr6gbDpoV\n" +
                    "Omj9iwnqNdPnEAOmc+s/9uw7drqDzSBUqYKP2UgHAoGBAKBk9mAQdChbrH7S+qYc\n" +
                    "GjxcM579ORgTkdq+sqDQrqp81MnKdxgYo4r1u6gSq9ollgTiVXxYj1tZdiafBBk5\n" +
                    "P347Ur2T4Prb4Zaq5vpox4iOjaFh2jRGScz0fEhwhngHHXjXH6SnbNh/AcAISkxN\n" +
                    "uhuq9Y6R4Otj3H3nj03+rot3";


            System.out.println(publicKey.hashCode());


            String signValue = signValue(input, privateKey);
            System.out.println("SignValue: " + signValue);
            boolean resultVerify = verifySignature(input.getBytes(), signValue.getBytes(), publicKey);
            System.out.println("ResultVerify: " + resultVerify);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String signValue(String input, String privateKeyStr) {

        try {
            PrivateKey privateKey = readPrivateKey(privateKeyStr);
            byte[] sign = new byte[0];
            sign = sign(privateKey, input);
            return org.apache.commons.codec.binary.Base64.encodeBase64String(sign);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] sign(PrivateKey prvKey, String message) throws UnsupportedEncodingException {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(prvKey);
            signer.update(message.getBytes("UTF-8"));
            return signer.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            // log error
        }
        return null;
    }

    private static PrivateKey readPrivateKey(String prvKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(prvKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            // Remove the "BEGIN" and "END" lines
            if (line.contains("-BEGIN") || line.contains("-END"))
                continue;
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        byte[] pkcs8EncodedBytes = Base64.decodeBase64(pkcs8Pem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }*/

    public static String mapToJsonOrNull(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("mapToJson JsonProcessingException !", e);
            return "";
        }
    }

    public int generateRandomDigits(int n) {
        int m = (int) Math.pow(10, n - 1);
        return m + this.random.nextInt(9 * m);
    }


    public boolean isAllowedList(String allowedList, String value, String delimiter) {
        List<String> ipList = new LinkedList<>(Arrays.asList(allowedList.split(delimiter)));

        if (!ipList.contains(value)) {
            log.error("value: {} isn't in valid list", value);
            return false;
        }
        return true;
    }

    /*public String convertGeorgianDateToPersianDate(String georgianDate) {


        //set default one month later
        Date expireDate = new Date();
        Calendar myCal = Calendar.getInstance();
        myCal.setTime(expireDate);
        myCal.add(Calendar.MONTH, +1);
        expireDate = myCal.getTime();

        try {
            expireDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(georgianDate);
        } catch (ParseException e) {
            log.error("error in parse date {} and error is {}", georgianDate, e.getMessage());
        }
        return DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, expireDate, "yyyy/MM/dd", true);
    }*/
}
