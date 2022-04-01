package com.example.myplantsguru.data;

import com.google.firebase.database.DatabaseReference;

public class MyMarker {
    private MyLatLng myLatLng;
    private String title;
    private String snippet;
    private DatabaseReference reference;

    public MyMarker(MyLatLng myLatLng, String title, String snippet, DatabaseReference reference) {
        this.myLatLng = myLatLng;
        this.title = title;
        this.snippet = snippet;
        this.reference = reference;
    }

    public MyLatLng getMyLatLng() {
        return myLatLng;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public DatabaseReference getReference() {
        return reference;
    }
}
