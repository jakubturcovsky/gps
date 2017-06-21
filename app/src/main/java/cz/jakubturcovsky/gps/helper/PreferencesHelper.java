package cz.jakubturcovsky.gps.helper;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.util.Base64;

import java.util.Collections;
import java.util.Set;

import cz.jakubturcovsky.gps.GpsApplication;

public class PreferencesHelper {

    public static final String PREF_ACQUIRE_LOCATION_PERIOD = "pref_acquire_location_period";
    public static final String PREF_ROUTE_LINE_COLOR = "pref_route_line_color";
    public static final String PREF_ROUTE_LINE_WIDTH = "pref_route_line_width";
    public static final int DEFAULT_ROUTE_LINE_WIDTH = 10;

    public static long getAcquireLocationPeriod() {
        return Long.valueOf(getString(PREF_ACQUIRE_LOCATION_PERIOD, String.valueOf(-1)));
    }

    public static void setAcquireLocationPeriod(long acquireLocationPeriod) {
        setString(PREF_ACQUIRE_LOCATION_PERIOD, String.valueOf(acquireLocationPeriod));
    }

    @ColorInt
    public static int getRouteLineColor() {
        return Integer.valueOf(getString(PREF_ROUTE_LINE_COLOR, String.valueOf(Color.BLACK)));
    }

    public static int getRouteLineWidth() {
        return Integer.valueOf(getString(PREF_ROUTE_LINE_WIDTH, String.valueOf(DEFAULT_ROUTE_LINE_WIDTH)));
    }

    /* INTERNAL */
    public static void clearAllPreferences() {
        edit().clear().commit();
    }

    private static byte[] getByteArray(String key) {
        String value = getString(key, null);
        if (value == null) {
            return null;
        }

        return Base64.decode(value, Base64.DEFAULT);
    }

    private static void setByteArray(String key, byte[] value) {
        setString(key, Base64.encodeToString(value, Base64.DEFAULT));
    }

    private static String getString(String key, String defaultValue) {
        return getPreferences().getString(key, defaultValue);
    }

    private static void setString(String key, String value) {
        edit().putString(key, value).commit();
    }

    private static int getInt(String key) {
        return getInt(key, 0);
    }

    private static int getInt(String key, int defaultValue) {
        return getPreferences().getInt(key, defaultValue);
    }

    private static void setInt(String key, int value) {
        edit().putInt(key, value).commit();
    }

    private static long getLong(String key) {
        return getLong(key, 0L);
    }

    private static long getLong(String key, long defaultValue) {
        return getPreferences().getLong(key, defaultValue);
    }

    private static void setLong(String key, long value) {
        edit().putLong(key, value).commit();
    }

    private static float getFloat(String key) {
        return getFloat(key, 0f);
    }

    private static float getFloat(String key, float defaultValue) {
        return getPreferences().getFloat(key, defaultValue);
    }

    private static void setFloat(String key, float value) {
        edit().putFloat(key, value).commit();
    }

    private static boolean getBoolean(String key){
        return getBoolean(key, false);
    }

    private static boolean getBoolean(String key, boolean defaultValue){
        return getPreferences().getBoolean(key, defaultValue);
    }

    private static void setBoolean(String key, boolean value) {
        edit().putBoolean(key, value).commit();
    }

    private static Set<String> getStringSet(String key) {
        return getStringSet(key, Collections.<String>emptySet());
    }

    private static Set<String> getStringSet(String key, Set<String> defaultValues) {
        return getPreferences().getStringSet(key, defaultValues);
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(GpsApplication.getInstance());
    }

    private static SharedPreferences.Editor edit() {
        return getPreferences().edit();
    }
}
