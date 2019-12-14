package com.example.homework.utils;

import java.util.regex.Pattern;

public class DataTypeUtil {

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static boolean canSharePreferences(String str) {
        boolean flag = true;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetter(str.charAt(i))) {
                flag = false;
            }
        }
        return flag;
    }
}
