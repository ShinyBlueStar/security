package com.sample.system.vault.service.domain.utility;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");
    public static final String FARSI_LANGUAGE = "fa";
    public static final Locale FARSI_LOCALE = new Locale(FARSI_LANGUAGE);
    public static final Locale ENGLISH_LOCALE = Locale.ENGLISH;
    private final static Pattern patternPersian = Pattern.compile("^[\\u0600-\\u06FF]+(?:[\\s0-9()،,-]+[\\u0600-\\u06FF]+)*$");


    public static String getLocaleText(Locale locale, String text) {

        if (text != null && locale != null && FARSI_LANGUAGE.equalsIgnoreCase(locale.getLanguage())) {
            StringBuffer sb;
            char c;

            sb = new StringBuffer();

            for (int i = 0; i < text.length(); i++) {
                c = text.charAt(i);
                if (c >= '0' && c <= '9') {
                    c = (char) (c + 0x6C0);
                }
                sb.append(c);
            }

            text = sb.toString();
        }

        return text;
    }

    public static String fixSomeWord(String statement) {
        if (Arrays.asList(statement.split(" ")).contains("نسده")) {
            return statement.replace("نسده", "نشده");
        }
        return statement;
    }

    public static boolean isMatched(String patternStr, String path) {
        Pattern pattern;

        patternStr = org.springframework.util.StringUtils.replace(patternStr, ".", "\\.");
        patternStr = org.springframework.util.StringUtils.replace(patternStr, "*", "+.*");
        pattern = Pattern.compile(patternStr);

        return pattern.matcher(path).matches();
    }

    public static String trimLeadingChars(String str, char ch) {
        String prunedString;
        int i;

        if (str == null) {
            return null;
        }

        prunedString = str;

        for (i = 0; i < str.length(); ++i) {
            if (str.charAt(i) != ch) {
                prunedString = str.substring(i, str.length());
                break;
            }
        }

        if (i == str.length()) {
            prunedString = String.valueOf(ch);
        }

        return prunedString;
    }

    public static boolean isNumber(String str) {

        if (str == null || "".equalsIgnoreCase(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {

            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasText(String src) {
        if (src == null) {
            return false;
        }

        src = src.trim();

        return src.length() > 0;
    }

    public static boolean isPersianString(String str) {
        if (str == null || "".equalsIgnoreCase(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return true;
            }
        }

        return false;
    }

    public static String format(String value, int length) {
        StringBuffer result;

        result = new StringBuffer(value);

        while (result.length() < length) {
            result.insert(0, "0");
        }

        return result.toString();
    }

    public static String trim(String str, char ch) {
        String prunedString;
        int i;

        if (str == null) {
            return null;
        }

        prunedString = str;

        for (i = 0; i < str.length(); ++i) {
            if (str.charAt(i) != ch) {
                prunedString = str.substring(i, str.length());
                break;
            }
        }

        if (i == str.length()) {
            prunedString = String.valueOf(ch);
        }

        return prunedString;
    }

    protected static String getReplacement(Map model, Matcher aMatcher) throws Exception {
        String s;
        String beanName;
        Object value;

        s = aMatcher.group(0);
        s = s.substring(2, s.length() - 1);

        if (s.indexOf('.') == 0) {
            s = " " + s;
        }

        beanName = s;

        return ((value = model.get(beanName)) != null) ? value.toString() : "";
    }

    public static boolean equals(String[] items, String value) {
        for (String item : items) {
            if (!item.equals(value)) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(String s, String t) {

        if (s == null) {
            return t == null;
        }

        if (t == null) {
            return false;
        }

        s = s.trim();

        t = t.trim();

        return s.equals(t);
    }

    public static String nvl(String str) {

        if (str == null || str.equals("null")) {

            str = "";
        }

        return str;
    }

    public static String encode(String text) {

        if (!StringUtils.hasText(text)) {
            text = " ";
        }
        StringBuffer encoded = new StringBuffer();

        for (char c : text.toCharArray()) {
            encoded.append(encode(c));
        }
        return encoded.toString().length() > 25 ? encoded.toString().substring(0, 25) : encoded.toString();
    }

    private static String encode(char c) {
        switch (c) {
            case '\u0627':
                return "a";
            case '\u0622':
                return "A";
            case '\u0628':
                return "b";
            case '\u067e':
                return "p";

            case '\u062a':
                return "t";

            case '\u062b':
                return "C";

            case '\u062c':
                return "j";

            case '\u0686':
                return "c";

            case '\u062d':
                return "h";

            case '\u062e':
                return "x";

            case '\u062f':
                return "d";

            case '\u0630':
                return "z";

            case '\u0631':
                return "r";

            case '\u0632':
                return "e";

            case '\u0698':
                return "w";

            case '\u0633':
                return "s";

            case '\u0634':
                return "u";

            case '\u0635':
                return "S";

            case '\u0636':
                return "X";

            case '\u0637':
                return "T";

            case '\u0638':
                return "Z";

            case '\u0639':
                return "i";

            case '\u063a':
                return "Q";

            case '\u0641':
                return "f";

            case '\u0642':
                return "q";

            case '\u06a9':
            case '\u0643':
                return "k";

            case '\u06af':
                return "g";

            case '\u0644':
                return "l";

            case '\u0645':
                return "m";

            case '\u0646':
                return "n";

            case '\u0648':
                return "v";

            case '\u0647':
                return "H";

            case '\u06cc':
            case '\u064a':
                return "y";
            case '\u0626':
                return "I";

            case '\u0020':
                return " ";

            case '\u0640':
                return "";

            default:
                return "+" + String.valueOf(c);

        }
    }

    public static boolean hasTextAndIsPersian(String src) {
        boolean isPersian = false;
        if (src == null) {
            return false;
        }
        src = src.trim();
        if (src.length() > 0) {

            for (int i = 0; i < src.length(); i++) {
                Matcher matcher = patternPersian.matcher(String.valueOf(src.charAt(i)));
                if (matcher.matches()) {
                    isPersian = true;
                    break;
                }

            }
        }
        return isPersian;

    }

    public static String separateNumberByComma(String src) {
        try {
            double amount = Double.parseDouble(src);
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            return decimalFormat.format(amount);
        } catch (NumberFormatException ex) {
            return "";
        }
    }

}
