package com.example.test2;

import android.location.Location;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;

public class UTM_Speed {
    public double[] convertToUTM(double longitude, double latitude) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
        CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:32639");
        ProjCoordinate srcCoord = new ProjCoordinate(longitude,latitude);
        CoordinateTransform transform = new BasicCoordinateTransform(sourceCRS, targetCRS);
        ProjCoordinate destCoord = new ProjCoordinate();
        transform.transform(srcCoord, destCoord);
        double[] utmCoordinates = {destCoord.x, destCoord.y};
        return utmCoordinates;
    }


    public double calculateDistance(double[] utmCoordinates1, double[] utmCoordinates2) {

        double distance = Math.sqrt(Math.pow(utmCoordinates2[0] - utmCoordinates1[0], 2) + Math.pow(utmCoordinates2[1] - utmCoordinates1[1], 2));
        return distance;
    }


    public double calculateSpeed(Location previousLocation, Location currentLocation) {

        double[] utmCoordinates1 = convertToUTM(previousLocation.getLongitude(), previousLocation.getLatitude());
        double[] utmCoordinates2 = convertToUTM(currentLocation.getLongitude(), currentLocation.getLatitude());

        double distance = calculateDistance(utmCoordinates1, utmCoordinates2);

        long timeDifference = Math.abs(currentLocation.getTime() - previousLocation.getTime());

        double speed = distance / (timeDifference / 1000);

        return speed;
    }
}
