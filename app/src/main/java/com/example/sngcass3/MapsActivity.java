package com.example.sngcass3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private static final int READ_REQUEST_CODE = 42;
    private Button buttonPlot;
    private Button start;
    private Button stop;
    LocationManager locationManager;
    LocationListener locationListener;
    LocationManager locationManager1;
    LocationListener locationListener1;
    private TextView textView;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonPlot = (Button) findViewById(R.id.button);
        start = (Button) findViewById(R.id.button2);
        stop = (Button) findViewById(R.id.button3);
        textView = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                double speed = location.getSpeed();
                textView.setText("Longitude: " + longitude + "\n" + "Latitude: " + latitude + "\n" + "Speed: " + speed);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);

            }
        };

        locationManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener1 = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    writeToFile(location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                   return;
                }
                String fileName = "gpsLogger" + System.currentTimeMillis() + ".csv";
                file = new File(getExternalFilesDir(null), fileName);
                locationManager1.requestLocationUpdates("gps", 1000, 0, locationListener1);
                Toast toast = Toast.makeText(getApplicationContext(), "Started Logging location", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        stop.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager1.removeUpdates(locationListener1);
                Toast toast = Toast.makeText(getApplicationContext(), "Stopped Logging location" , Toast.LENGTH_LONG);
                toast.show();
            }
        }));
        buttonPlot.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        }));
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }
        locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


    public void performFileSearch(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = null;
            if(resultData != null){
                uri = resultData.getData();
                try{

                    PlotMap(uri);

                }catch(Exception e){
                    System.out.println("An exception occurred");
                    e.printStackTrace();
                }
            }
        }
    }
    private void PlotMap(Uri uri){
        Intent mapIntent = new Intent(this, PlotMap.class);
        mapIntent.setData(uri);
        startActivity(mapIntent);
    }

    private void writeToFile(Location location){
        String locInfo = System.currentTimeMillis() + "," + location.getSpeed() + "," + location.getLongitude() + "," + location.getLatitude() + "\n";

        try{
            OutputStream os = new FileOutputStream(file, true);
            os.write(locInfo.getBytes());
            os.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
