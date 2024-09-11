package com.example.test2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class work_activity extends AppCompatActivity {

    //MapView is a object that Installed by Osmdroid
    MapView map;
    MapController mapController;
    Button btn_update_location, button_start, button_stop;
    TextView lat, lon, Speed, Accuracy,Warning;
    CheckBox CheckBox, Rotation, Driving;
    LocationManager locationManager;
    LocationListener locationListener;

    private static final String TAG = "OsmActivity";

    List<Location> locationList = new ArrayList<>();
    // for setzoom marker
    private boolean isFirstLocationChange = true;
    //for stop and start button
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_work);

        map = findViewById(R.id.mapView);
        btn_update_location = findViewById(R.id.btn_update_location);
        button_start = findViewById(R.id.button_start);
        button_stop = findViewById(R.id.button_stop);
        lat = findViewById(R.id.textView_lat);
        lon = findViewById(R.id.textView_lon);
        Speed = findViewById(R.id.textView_Speed);
        Accuracy = findViewById(R.id.textView_accuracy);
        CheckBox = findViewById(R.id.checkBox_fixmode);
        Rotation = findViewById(R.id.checkBox_rotation);
        Driving = findViewById(R.id.checkBox_MODE);
        Warning = findViewById(R.id.textView_warning);
        // after made context,we can set Configuration and details and setting
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // set map type
        map.setTileSource(TileSourceFactory.MAPNIK);
        // enable zoom button
        map.setBuiltInZoomControls(true);
        // MultiTouch
        map.setMultiTouchControls(true);
        // control "map"
        mapController = (MapController) map.getController();
        mapController.setZoom(6);
        map.setMinZoomLevel(3.0);
        GeoPoint tehran_center = new GeoPoint(32.5, 53.5);
        map.getController().setCenter(tehran_center);

        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        // Every time Button Clicked run enteral functions in Listener
        btn_update_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RandomStringGenerator generator = new RandomStringGenerator();
                String imei  = generator.generateRandomString(5);


                try {
                    // defining location manager for context and get access to LOCATION_SERVICE phone (to get location updates)
                    locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                    // check if we have no Access
                    if (ActivityCompat.checkSelfPermission(work_activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(work_activity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // request for access
                        ActivityCompat.requestPermissions(work_activity.this, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 100);
                        return;
                        // if we have access Request from
                    }
                    // minDistance: distance between Previous location and current location
                    // requestLocationUpdates must save in  locationListener
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                locationListener = new LocationListener() {
                    @Override
                    //Inheritance location from Location
                    public void onLocationChanged(@NonNull Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        double accuracy = location.getAccuracy();
                        lat.setText("Latitude: " + String.format("%.4f", latitude) + " ̊");
                        lon.setText("Longitude: " + String.format("%.4f", longitude) + " ̊");
                        Accuracy.setText("Accuracy: " + String.format("%.4f", accuracy) + " m");

                        GeoPoint currentPosition = new GeoPoint(latitude, longitude);
                        //  Overlay
                        Overlay accuracyCircleOverlay = new Overlay() {
                            @Override
                            public void draw(Canvas canvas, MapView mapView, boolean shadow) {
                                Projection projection = mapView.getProjection();
                                Point centerPoint = ((Projection) projection).toPixels(currentPosition, null);
                                float accuracyRadius = projection.metersToEquatorPixels((float) accuracy);

                                Paint accuracyCirclePaint = new Paint();
                                accuracyCirclePaint.setStyle(Paint.Style.FILL);
                                accuracyCirclePaint.setColor(Color.parseColor("#753083FF"));
                                accuracyCirclePaint.setAntiAlias(true);

                                canvas.drawCircle(centerPoint.x, centerPoint.y, accuracyRadius, accuracyCirclePaint);
                            }
                        };
                        //  marker
                        Marker marker = new Marker(map);
                        marker.setPosition(currentPosition);
                        marker.setIcon(getResources().getDrawable(R.drawable.marker16));
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                        map.getOverlays().clear();
                        map.getOverlays().add(accuracyCircleOverlay);
                        map.getOverlays().add(marker);
                        // Compass
                        compassOverlay.enableCompass();
                        map.getOverlays().add(compassOverlay);

                        if (isFirstLocationChange) {
                            int zoomLevel = 22;
                            int duration = 10000;
                            mapController.setCenter(currentPosition);
                            mapController.animateTo(currentPosition, (double) zoomLevel, (long) duration);
                            isFirstLocationChange = false;
                        }
                        
                        if (CheckBox.isChecked()) {
                            mapController.setCenter(currentPosition);
                            mapController.animateTo(currentPosition);
                        }

                        map.invalidate();
                        locationList.add(location);

                        // SPEED
                        if (locationList.size() > 1) {
                            Location previousLocation = locationList.get(locationList.size() - 2);
                            Speed speedCalculator = new Speed();
                            double speed = speedCalculator.calculateSpeed(previousLocation, location);
                            Speed.setText("Speed: " + String.format("%.4f", speed) + " m/s");
                        }
                        // azimuth
                        if (Rotation.isChecked()) {
                            if (locationList.size() > 1) {
                                Location previousLocation = locationList.get(locationList.size() - 2);
                                Location currentLocation = locationList.get(locationList.size() - 1);
                                // azimuth
                                AzimuthCalculator GETAzimuth = new AzimuthCalculator();
                                double azimuth = GETAzimuth.Final_AZIMUTH(previousLocation, currentLocation);
                                map.setMapOrientation((float) azimuth);
                            }
                        }

                        // RotationGestureOverlay MapView
                        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(map.getContext(), map);
                        rotationGestureOverlay.setEnabled(true);
                        map.getOverlays().add(rotationGestureOverlay);
                        sendDataToServer(latitude, longitude, getBatteryLevel(), Driving.isChecked(), imei);  

                    }

                    //for turn on GPS:
                    @Override
                    public void onProviderDisabled(String s) {
                        Toast.makeText(getApplicationContext(), "Please Turn on Your GPS", Toast.LENGTH_SHORT).show();
                        // go to mobile setting
                        // intent for movement between pages
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                };
            }
        });

        //for Keep screen ON:
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for lock in PORTRAIT mode _ dont rotate
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
    }

    private void startRecording() {
        if (!isRecording) {
            isRecording = true;
            locationList.clear();
            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Recording already started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            Toast.makeText(getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
            saveGeoJSON();
        } else {
            Toast.makeText(getApplicationContext(), "Recording not started yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGeoJSON() {
        // Check if locationList is not empty
        if (locationList != null && !locationList.isEmpty()) {
            GeoJson geoJsonConverter = new GeoJson();
            String filePath = getExternalFilesDir(null).getPath() + "/secondTEST.geojson";
            geoJsonConverter.saveCoordinatesToGeoJson(locationList, filePath);
            Toast.makeText(getApplicationContext(), "GeoJSON file saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Location list is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED & checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
