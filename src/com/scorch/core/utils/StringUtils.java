package com.scorch.core.utils;

import java.util.Random;

public class StringUtils {

    public static String getUniqueString (int length){
        if(length <= 0) return "";
        String dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for(int i = 0; i < length; i++){
            res = res + dict.indexOf(new Random().nextInt(dict.length()));
        }
        return res;
    }

}
