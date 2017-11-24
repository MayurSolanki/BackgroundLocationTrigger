package com.locationtrigger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nikunj on 3/1/17.
 */

public class PreferenceHelper {


    private static final String APP_SHARED_PREFS = PreferenceHelper.class.getSimpleName();
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor prefsEditor;



    public static final String KEY_PREFS_LOCATION_UPDATE_TIME = "location_update_time";



    public PreferenceHelper(Context context) {
        this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = _sharedPrefs.edit();

    }

    public void setLocationUpdateTime(int locationUpdateTime) {
        prefsEditor.putInt(KEY_PREFS_LOCATION_UPDATE_TIME, locationUpdateTime);
        prefsEditor.commit();

    }

    public int getLocationUpdateTime() {
        return _sharedPrefs.getInt(KEY_PREFS_LOCATION_UPDATE_TIME, 20000);
    }






}
