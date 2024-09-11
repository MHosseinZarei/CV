package com.example.test2;

import android.location.Location;

public class AzimuthCalculator {

    public double Final_AZIMUTH (Location previousLocation, Location currentLocation) {

        float bearing = previousLocation.bearingTo(currentLocation);
        float azimuth = (360-bearing);

        if (azimuth<0){
            azimuth+=360;
        }

        if (azimuth>360) {
            azimuth -= 360;



        }
        return azimuth;


    }

}