package com.gagan.raise.to.wake.persistant;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gaganpreetsingh on 2/28/2017.
 */

public class AppPref {

    private static final String PREF_FILE                   = "raise_to_wake.pref";
    private static final String AUTO_START                  = "pref_auto_start";
    private static final String ENABLE_PROXIMITY_SENSOR     = "pref_enable_proximity_sensor";
    private static final String ENABLE_NIGHT_MODE           = "pref_enable_night_mode";

    private static AppPref mInstance;

    private SharedPreferences mPref;

    public static AppPref getInstance(Context context) {
        if(mInstance == null)
            mInstance = new AppPref(context);

        return mInstance;
    }

    private AppPref(Context context) {
        mPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public void setAutoStart(boolean value) {
        mPref.edit().putBoolean(AUTO_START, value).apply();
    }

    public boolean isAutoStart() {
        return mPref.getBoolean(AUTO_START, false);
    }

    public void setEnableProximitySensor(boolean enable) {
        mPref.edit().putBoolean(ENABLE_PROXIMITY_SENSOR, enable).apply();
    }

    public boolean isProximitySensorEnabled() {
        return mPref.getBoolean(ENABLE_PROXIMITY_SENSOR, false);
    }

    public void setEnableNightMode(boolean value) {
        mPref.edit().putBoolean(ENABLE_NIGHT_MODE, value).apply();
    }

    public boolean isNightModeEnabled() {
        return mPref.getBoolean(ENABLE_NIGHT_MODE, false);
    }
}
