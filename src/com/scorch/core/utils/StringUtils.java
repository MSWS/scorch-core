package com.scorch.core.utils;

import java.util.Random;

public class StringUtils {

    public static String getUniqueString (int length){
        if(length <= 0) return "";
        Random rand = new Random();
        String dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            builder.append(dict.charAt(rand.nextInt(dict.length())));
        }
        return builder.toString();
    }

}
