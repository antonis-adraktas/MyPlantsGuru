package com.example.myplantsguru.data;

import androidx.annotation.NonNull;

public class WeatherInfoPerTime {
    private double temperature;
    private int condition;
    private String date;
    private double windSpeed;


    public WeatherInfoPerTime(double temperature, int condition, String date, double windSpeed) {
        this.temperature = temperature;
        this.condition = condition;
        this.date = date;
        this.windSpeed = windSpeed;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getCondition() {
        return condition;
    }

    public String getDate() {
        return date;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\ndate: "+date+"\n"+"temperature: "+temperature+
                "\n"+"condition: "+String.valueOf(condition)+
                "\n"+"wind speed: "+String.valueOf(windSpeed)
                +"\n}";

    }
}
