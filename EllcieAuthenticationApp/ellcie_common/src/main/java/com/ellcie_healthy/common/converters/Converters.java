package com.ellcie_healthy.common.converters;

// Converters - converts value between different numeral system
public class Converters {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();


    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param version1 a string of ordinal numbers separated by decimal points.
     * @param version2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if version1 is _numerically_ less than version2.
     * The result is a positive integer if version1 is _numerically_ greater than version2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int compareVersion(String version1, String version2) {
        String[] version1Numbers = version1.split("\\.");
        String[] version2Numbers = version2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < version1Numbers.length && i < version2Numbers.length && version1Numbers[i].equals(version2Numbers[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < version1Numbers.length && i < version2Numbers.length) {
            int diff = -1;

            try {
                diff = Integer.valueOf(version1Numbers[i]).compareTo(Integer.valueOf(version2Numbers[i]));
            } catch (Exception ignored) {}
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(version1Numbers.length - version2Numbers.length);
    }

    public static boolean isLowerVersion(String version1, String version2) {
        return compareVersion(version1, version2) < 0;
    }


    /**
     * Get hex value for database
     * Result will be something like this : 0x532244
     * (no space, prefixing by 0x)
     */
    public static String getHexValueForDb(byte[] value) {
        if (value == null) {
            return "";
        }
        char[] hexChars = new char[value.length * 2];
        int v;
        for (int j = 0; j < value.length; j++) {
            v = value[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    // Gets value in hexadecimal system
    public static String getHexValue(byte[] value) {
        if (value == null) {
            return "";
        }

        char[] hexChars = new char[value.length * 3];
        int v;
        for (int j = 0; j < value.length; j++) {
            v = value[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    // Gets value in hexadecimal system for single byte
    public static String getHexValue(byte b) {
        char[] hexChars = new char[2];
        int v;
        v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
        return new String(hexChars);
    }

    // Gets value in decimal system for single byte
    @SuppressWarnings("unused")
    public static String getDecimalValue(byte b) {
        String result = "";
        result += ((int) b & 0xff);

        return result;
    }
}
