package com.example.myplantsguru.data;

import androidx.annotation.NonNull;

public class WeatherSmall {
    private String time;
    private String weatherIcon;
    private String temperature;

    public WeatherSmall(String time, String weatherIcon, String temperature) {
        this.time = time;
        this.weatherIcon = weatherIcon;
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getTemperature() {
        return temperature;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\ntime: "+time+"\n"+"temperature: "+temperature+
                "\n"+"weatherIcon: "+weatherIcon;
    }
}
