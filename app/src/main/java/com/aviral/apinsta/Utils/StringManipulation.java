package com.aviral.apinsta.Utils;

public class StringManipulation {

    public static String expandUsername(String username) {
        return username.replace("_", " ");
    }

    public static String compressUsername(String username) {
        return username.replace(" ", "_");
    }

    public static String getTags(String string) {
        if (string.indexOf("#") > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();

            boolean forwardWord = false;

            for (char c: charArray) {
                if (c == '#') {
                    forwardWord = true;
                    sb.append(c);
                } else {
                    if (forwardWord) {
                        sb.append(c);
                    }
                }
                if (c == ' ') {
                    forwardWord = false;
                }
            }

            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        }
        return string;
    }

}
