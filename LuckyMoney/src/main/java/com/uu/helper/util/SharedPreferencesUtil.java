package com.uu.helper.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static final String FILENAME = "config";

    public SharedPreferencesUtil() {
    }

    public static void saveString(Context con, String key, String value) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        sp.edit().putString(key, value).commit();
    }

    public static String getString(Context con, String key) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        return sp.getString(key, (String)null);
    }

    public static void saveInt(Context con, String key, int value) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        sp.edit().putInt(key, value).commit();
    }

    public static int getInt(Context con, String key) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        return sp.getInt(key, 0);
    }

    public static int getIntWithDefault(Context con, String key, int value) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        return sp.getInt(key, value);
    }

    public static void saveBoolean(Context con, String key, boolean value) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context con, String key) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        return sp.getBoolean(key, false);
    }

    public static boolean getBooleanWithDefault(Context con, String key, boolean value) {
        SharedPreferences sp = con.getSharedPreferences(FILENAME, 0);
        return sp.getBoolean(key, value);
    }
}
