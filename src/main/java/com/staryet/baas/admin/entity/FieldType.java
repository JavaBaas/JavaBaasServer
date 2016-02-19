package com.staryet.baas.admin.entity;

/**
 * Created by Staryet on 15/6/17.
 */
public class FieldType {

    public static final int STRING = 1;
    public static final int NUMBER = 2;
    public static final int BOOLEAN = 3;
    public static final int DATE = 4;
    public static final int FILE = 5;
    public static final int OBJECT = 6;
    public static final int ARRAY = 7;
    public static final int POINTER = 8;
    public static final int GEOPOINT = 9;//TODO 地理坐标

    public static boolean isValid(int type) {
        if (type < 1 || type > 9) {
            return false;
        } else {
            return true;
        }
    }

}
