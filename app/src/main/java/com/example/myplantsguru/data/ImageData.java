package com.example.myplantsguru.data;

public class ImageData {
    private String userEmail;
    private String storagePath;
    private String shortDescription;
    private String question;
    private MyLatLng myLatLng;
    private int altitude;
    private String city;

    public ImageData(String userEmail, String storagePath, String shortDescription, String question, MyLatLng myLatLng,int altitude,String city) {
        this.userEmail = userEmail;
        this.storagePath = storagePath;
        this.shortDescription = shortDescription;
        this.question = question;
        this.myLatLng = myLatLng;
        this.altitude=altitude;
        this.city=city;
    }

    public ImageData() {
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getQuestion() {
        return question;
    }

    public MyLatLng getMyLatLng() {
        return myLatLng;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public int getAltitude() {
        return altitude;
    }

    public String getCity() {
        return city;
    }
}
