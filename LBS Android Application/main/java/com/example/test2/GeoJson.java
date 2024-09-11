package com.example.test2;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GeoJson {

    public void saveCoordinatesToGeoJson(List<Location> locations, String filePath) {
        JSONObject featureCollection = new JSONObject();
        JSONArray features = new JSONArray();

        try {
            JSONArray coordinatesArray = new JSONArray();
            for (Location location : locations) {
                JSONArray coordinates = new JSONArray();
                coordinates.put(location.getLongitude());
                coordinates.put(location.getLatitude());
                coordinatesArray.put(coordinates);
            }

            JSONObject geometry = new JSONObject();
            geometry.put("type", "LineString");
            geometry.put("coordinates", coordinatesArray);

            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);

            features.put(feature);

            featureCollection.put("type", "FeatureCollection");
            featureCollection.put("features", features);

            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(featureCollection.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

}