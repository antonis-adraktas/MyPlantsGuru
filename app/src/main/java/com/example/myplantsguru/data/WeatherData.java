package com.example.myplantsguru.data;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myplantsguru.LoginActivity;
import com.example.myplantsguru.WeatherRecommendations;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherData {
    private String city;
    private double latitude;
    private double longitude;
    private ArrayList<WeatherInfoPerTime> weatherInfo=new ArrayList<>();

    public WeatherData() {
    }

    public WeatherData parseJson(JSONObject jsonObject){
        WeatherData weatherData=new WeatherData();
        Log.d(LoginActivity.LOGAPP,"parseJson invoked");
        try{
            //parse city name from json received to city member variable
            weatherData.city=jsonObject.getJSONObject("city").getString("name");
            //parse latitude from json received to latitude member variable
            weatherData.latitude=jsonObject.getJSONObject("city").getJSONObject("coord").getDouble("lat");
            //parse longitude from json received to longitude member variable
            weatherData.longitude =jsonObject.getJSONObject("city").getJSONObject("coord").getDouble("lat");

            for (int i=0;i<jsonObject.getJSONArray("list").length();i++){
                //Here we parse date, temperature,condition and wind speed to parameter values from json file for each 3 hour forecast. Then a WeatherInfoPerTime object is created with these values.
                String date=jsonObject.getJSONArray("list").getJSONObject(i).getString("dt_txt");
                double temp=jsonObject.getJSONArray("list").getJSONObject(i).getJSONObject("main").getDouble("temp");
                int condition=jsonObject.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getInt("id");
                double windSpeed=jsonObject.getJSONArray("list").getJSONObject(i).getJSONObject("wind").getDouble("speed");
                WeatherInfoPerTime weather=new WeatherInfoPerTime(temp,condition,date,windSpeed);
                // WeatherInfoPerTime is added to the member variable arraylist
                weatherData.weatherInfo.add(weather);
            }
//            Log.d(LoginActivity.LOGAPP,weatherData.toString());
            return weatherData;

        }catch (JSONException e){
            e.printStackTrace();
            Log.d(LoginActivity.LOGAPP,"json exception found "+ e.getMessage());
            return null;
        }
    }

    public String getCity() {
        return city;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ArrayList<WeatherInfoPerTime> getWeatherInfo() {
        return weatherInfo;
    }

    @NonNull
    @Override
    public String toString() {
        return "\ncity: "+city+"\n"+"Latitude: "+String.valueOf(latitude)+
                "\n"+"Longitude: "+String.valueOf(longitude)+
                "\n"+"weather Info: "+weatherInfo.toString()
                +"\n";
    }
}
