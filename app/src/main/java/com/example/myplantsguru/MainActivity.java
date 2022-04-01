package com.example.myplantsguru;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private Button mWeatherButton;
    private Button mUploadButton;
    private Button mOtherButton;
    private Button mMyPlants;
    private Button mQR;

    public static final String CURRENT_USER=FirebaseAuth.getInstance().getCurrentUser().getEmail();

    public static final int REQUEST_CODE = 123;
    // Time between location updates (5000 milliseconds or 5 seconds)
    static final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    static final float MIN_DISTANCE = 1000;


    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeatherButton=findViewById(R.id.weather_button);
        mUploadButton=findViewById(R.id.upload_button);
        mOtherButton=findViewById(R.id.other_button);
        mMyPlants=findViewById(R.id.myplants);
        mQR=findViewById(R.id.qrCode);


        mWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,WeatherRecommendations.class);
                finish();
                startActivity(intent);
            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,UploadPhoto.class);
                finish();
                startActivity(intent);
            }
        });

        mMyPlants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Myplants.class);
                finish();
                startActivity(intent);
            }
        });

        mOtherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,OtherUploads.class);
                finish();
                startActivity(intent);
            }
        });

        mQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Scanner.class);
                finish();
                startActivity(intent);
            }
        });

        checkForLocation();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }

    }


    private void checkForLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d(LoginActivity.LOGAPP, "onlocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(LoginActivity.LOGAPP, "longitude is: " + longitude);
                Log.d(LoginActivity.LOGAPP, "latitude is: " + latitude);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(LoginActivity.LOGAPP, "onProviderEnabled() callback received");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(LoginActivity.LOGAPP, "onProviderDisabled() callback received");
            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation==null){
                myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (myLocation==null){
                    Toast.makeText(MainActivity.this,"myLocation is still null, couldn't retrieve last known location from GPS or Network provider",Toast.LENGTH_SHORT).show();
                }
            }
            if (myLocation != null) {
                String longitude = String.valueOf(myLocation.getLongitude());
                String latitude = String.valueOf(myLocation.getLatitude());
                Log.d(LoginActivity.LOGAPP, "longitude is: " + longitude);
                Log.d(LoginActivity.LOGAPP, "latitude is: " + latitude);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }


    // This is the callback that's received when the permission is granted (or denied)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checking against the request code we specified earlier.
        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LoginActivity.LOGAPP, "onRequestPermissionsResult(): Permission granted!");

                // Getting weather only if we were granted permission.
//                    getWeatherForCurrentLocation();
            } else {
                Log.d(LoginActivity.LOGAPP, "Permission denied");
            }
        }
    }

    public void onBackPressed() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager!=null) locationManager.removeUpdates(locationListener);
    }
}