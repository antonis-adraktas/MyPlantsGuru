package com.example.myplantsguru;

import android.Manifest;
import android.app.AlertDialog;
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

import com.example.myplantsguru.data.ImageData;
import com.example.myplantsguru.data.MyMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OtherUploads extends AppCompatActivity
        implements OnMapReadyCallback {

    private Button infoButton;
    LocationManager locationManager;
    private Location myLocation;
    LocationListener locationListener;
    private  DatabaseReference databaseReference;
    private static DatabaseReference selectedMarkerRef;
    private FirebaseAuth auth;
//    private ArrayList<DatabaseReference> dbRefList;

    private ArrayList<MyMarker> markerList;
    private GoogleMap myMap;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.my_map_layout);

        databaseReference= FirebaseDatabase.getInstance().getReference().child(UploadPhoto.DB_DATA);
        auth=FirebaseAuth.getInstance();
//        dbRefList=new ArrayList<>();
        markerList=new ArrayList<>();

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoButton = findViewById(R.id.requestInfo);


        loadRefData();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.REQUEST_CODE);
            return;
        }
    }

    public static DatabaseReference getSelectedMarkerRef() {
        return selectedMarkerRef;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onMapReady(GoogleMap googleMap) {
        myMap=googleMap;
        enableMyLocation(myMap);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provideInfo(OtherUploads.this.getString(R.string.otherUploadInfo));
            }
        });
        if (myLocation!=null){
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), 11));
        }else{
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.882943, 23.657002), 11));
        }

        myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                Toast.makeText(OtherUploads.this, "Info window clicked", Toast.LENGTH_SHORT).show();
                Log.d(LoginActivity.LOGAPP,"db reference: "+marker.getTag().toString());
                selectedMarkerRef=(DatabaseReference) marker.getTag();
                Intent intent=new Intent(OtherUploads.this,OthersPlants.class);
                finish();
                startActivity(intent);
            }
        });

//        loadMarkers(googleMap,markerList);

    }



    private void provideInfo(String message) {
        new AlertDialog.Builder(OtherUploads.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
//        loadRefData();
//        loadMarkers(googleMap,markerList);
    }

    private void enableMyLocation(GoogleMap map) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                myLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (myLocation==null){
                    myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (myLocation==null){
                        Toast.makeText(OtherUploads.this,"myLocation is still null, couldn't retrieve last known location from GPS or Network provider",Toast.LENGTH_SHORT).show();
                    }
                }
                if (myLocation!=null){
                    locationListener= new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            myLocation=location;
                        }
                    };
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,20,locationListener);
                }else{
                    Toast.makeText(OtherUploads.this,"Location data is missing",Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MainActivity.REQUEST_CODE);
        }
    }

    public void onBackPressed() {
        Intent intent=new Intent(OtherUploads.this,MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void loadRefData(){
        Query uploads=databaseReference;

        uploads.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnaps: snapshot.getChildren()){
                    Log.d(LoginActivity.LOGAPP,"ref key loaded "+userSnaps.getKey());
                    if (!userSnaps.getKey().equals(UploadPhoto.replaceDotsWithUnderscore(auth.getCurrentUser().getEmail()))){
                        for (DataSnapshot uploadSnap:userSnaps.getChildren()){
//                            dbRefList.add(uploadSnap.getRef());
                            Log.d(LoginActivity.LOGAPP,"ref added in list "+uploadSnap.getRef());
                            ImageData currentData=(ImageData) uploadSnap.getValue(ImageData.class);
                            MyMarker myMarker=new MyMarker(currentData.getMyLatLng(),currentData.getShortDescription(),currentData.getUserEmail(),uploadSnap.getRef());
                            markerList.add(myMarker);
                        }
                        loadMarkers(myMap,markerList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(LoginActivity.LOGAPP, "onCancelled", error.toException());
            }
        });
    }

    private void loadMarkers(GoogleMap googleMap,ArrayList<MyMarker> list){
        for (MyMarker marker : list){
            LatLng googleLatLng=new LatLng(marker.getMyLatLng().getLatitude(),marker.getMyLatLng().getLongitude());

            Marker googleMarker=googleMap.addMarker(new MarkerOptions().position(googleLatLng).title(marker.getTitle()).snippet(marker.getSnippet()));
            googleMarker.setTag(marker.getReference());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager!=null) locationManager.removeUpdates(locationListener);
    }
}
