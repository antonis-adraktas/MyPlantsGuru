package com.example.myplantsguru.data;

import androidx.annotation.NonNull;

public class MyLatLng {
    private Double latitude;
    private Double longitude;

    public MyLatLng() {}

    public MyLatLng(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return "latitude: "+latitude+", longitude: "+longitude;
    }
}
