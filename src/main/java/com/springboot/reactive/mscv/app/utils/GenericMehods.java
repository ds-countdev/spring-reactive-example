package com.springboot.reactive.mscv.app.utils;

public class GenericMehods {
    
    public static <T> String getValue(T... values){
        for (T value : values){
            if (value instanceof String){
                return value.toString();
            }{
                return "";
            }
        }
        return null;
    }
}
