package com.locationtrigger;

import android.location.Location;

/**
 * Created by nikunj on 21/11/17.
 */

public class LocationModel {
    Location location;
    String mLocationUpdateTime;



    public String getmLocationUpdateTime() {
        return mLocationUpdateTime;
    }

    public void setmLocationUpdateTime(String mLocationUpdateTime) {
        this.mLocationUpdateTime = mLocationUpdateTime;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
