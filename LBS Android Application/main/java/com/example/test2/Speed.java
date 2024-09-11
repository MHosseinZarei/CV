package com.example.test2;

import android.location.Location;

public class Speed {

    // Haversine formula
    public double calculateDistance(Location loc1, Location loc2) {
        double lat1 = loc1.getLatitude();
        double lon1 = loc1.getLongitude();
        double lat2 = loc2.getLatitude();
        double lon2 = loc2.getLongitude();

        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return distance * 1000;
    }

    public double calculateSpeed(Location loc1, Location loc2) {
        double distance = calculateDistance(loc1, loc2);
        long timeDifference = loc2.getTime() - loc1.getTime();
        return distance / (timeDifference / 1000);
    }
}
