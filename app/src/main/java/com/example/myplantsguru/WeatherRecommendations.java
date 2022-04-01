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
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.adapters.WeatherAdapter;
import com.example.myplantsguru.data.WeatherData;
import com.example.myplantsguru.data.WeatherInfoPerTime;
import com.example.myplantsguru.data.WeatherSmall;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class WeatherRecommendations extends AppCompatActivity {

    final int REQUEST_CODE = 123;
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast";
    // App ID to use OpenWeather data
    public static final String APP_ID = BuildConfig.open_weather_app_id;
    // Number of 3 hours forecasts received from api, if set to 8 it will fetch one day
    public static final String CNT="16";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    private TextView mCity;
    private TextView mTemperature;
    private TextView mDate;
    private TextView mWind;
    private TextView mAdviceText;
    private ImageView mWeatherImage;
    private ImageButton mRefresh;
    private  static WeatherData weather;
    private RecyclerView recyclerView;
    public static int recyclerViewHeight;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_recommendation);

        mCity=findViewById(R.id.locationW);
        mWeatherImage=findViewById(R.id.weatherIcon);
        mTemperature =findViewById(R.id.celcius);
        mDate=findViewById(R.id.weather_date);
        mWind=findViewById(R.id.weather_wind);
        mAdviceText=findViewById(R.id.adviceText);

        mRefresh = findViewById(R.id.refreshButton);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LoginActivity.LOGAPP, "Getting weather for current location, refresh button clicked");
                getWeatherForCurrentLocation();
            }
        });



        recyclerView =findViewById(R.id.weather_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        if (weather!=null){
            Log.d(LoginActivity.LOGAPP,"weather object already fetched, using to load data");

            ViewTreeObserver viewTreeObserver = recyclerView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int viewWidth = recyclerView.getWidth();
                        if (viewWidth != 0){
                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            Log.d(LoginActivity.LOGAPP,"viewWidth of weather list: "+viewWidth);
                            recyclerViewHeight=viewWidth;
                            WeatherAdapter adapter = new WeatherAdapter(parseAdapterData(weather));
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
            }

            updateUI(weather);
        }else{
            Log.d(LoginActivity.LOGAPP,"weather object is null now, calling getWeather function");
            getWeatherForCurrentLocation();
        }



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
    }

    public static WeatherData getWeather() {
        return weather;
    }

    public static void setWeather(WeatherData weather) {
        WeatherRecommendations.weather = weather;
    }

    private void getWeatherForCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d(LoginActivity.LOGAPP, "onlocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(LoginActivity.LOGAPP, "longitude is: " + longitude);
                Log.d(LoginActivity.LOGAPP, "latitude is: " + latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                params.put("units", "metric");
                params.put("cnt", CNT);
                letsDoSomeNetworking(params);
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation==null){
                myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (myLocation==null){
                    Toast.makeText(WeatherRecommendations.this,"myLocation is still null, couldn't retrieve last known location from GPS or Network provider",Toast.LENGTH_SHORT).show();
                }
            }
            if (myLocation!=null){
                String longitude= String.valueOf(myLocation.getLongitude());
                String latitude=String.valueOf(myLocation.getLatitude());
                RequestParams params=new RequestParams();
                params.put("lat",latitude);
                params.put("lon",longitude);
                params.put("appid",APP_ID);
                params.put("units","metric");
                params.put("cnt", CNT);
                letsDoSomeNetworking(params);
            }
        }else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
        }

    }

//    // This is the callback that's received when the permission is granted (or denied)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        // Checking against the request code we specified earlier.
//        if (requestCode == REQUEST_CODE) {
//
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d(LoginActivity.LOGAPP, "onRequestPermissionsResult(): Permission granted!");
//
//                // Getting weather only if we were granted permission.
//                getWeatherForCurrentLocation();
//            } else {
//                Log.d(LoginActivity.LOGAPP, "Permission denied");
//            }
//        }
//    }

    public  void letsDoSomeNetworking(RequestParams params){
        AsyncHttpClient client=new AsyncHttpClient();
        Log.d(LoginActivity.LOGAPP,"inside networking");
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(LoginActivity.LOGAPP,"Success! JSON: "+response.toString());
                weather= new WeatherData().parseJson(response);
                if (weather!=null){
//                    Log.d(LoginActivity.LOGAPP,"weather data object is "+weather.toString());
                    updateUI(weather);                                     //if weather object is fetched successfully and has data update the UI
                    WeatherAdapter adapter = new WeatherAdapter(parseAdapterData(weather));  //set adapter once data is fetched successfully
                    recyclerView.setAdapter(adapter);
                }else{
                    Log.d(LoginActivity.LOGAPP,"weather data object is null");
                    Toast.makeText(WeatherRecommendations.this,"Couldn't parse weather data. Something unexpected happened!",Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d(LoginActivity.LOGAPP,"Fail "+throwable.toString());
                Log.d(LoginActivity.LOGAPP,"Status code "+statusCode);
                Toast.makeText(WeatherRecommendations.this,"Request failed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUI(WeatherData weather){
        String currentTemperature=String.valueOf(Math.round(weather.getWeatherInfo().get(0).getTemperature()));   //round temperature to the closest integer
        mTemperature.setText(currentTemperature+"°");
        mCity.setText(weather.getCity());
        int resourseID=getResources().getIdentifier(updateWeatherIcon(weather.getWeatherInfo().get(0).getCondition()),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourseID);
        //manipulate date extracted to a "dd--MMM hh:mm" format
        String[] date=weather.getWeatherInfo().get(0).getDate().split(" ");
        String day=date[0];
        String hour=date[1].substring(0,5);
        mDate.setText(dateFormat(day)+" "+hour);
        String windSpeed=weather.getWeatherInfo().get(0).getWindSpeed()+"m/s";
        mWind.setText("Windspeed: "+windSpeed);
        mAdviceText.setText(recommendations(weather));       //populate adviceText with the recommendations derived from weather data

    }
    //function to change "2021-03-26" date format to "26-March"
    private String dateFormat(String D){
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MMMM");
        Date date = null;
        try {
            date = format1.parse(D);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateString = format2.format(date);
        Log.d(LoginActivity.LOGAPP,dateString);
        return dateString;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager!=null) locationManager.removeUpdates(locationListener);
    }


    public static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }

        return "dunno";
    }

    // function to create an Arraylist of  WeatherSmall objects from the WeatherData object. This will be used as input for the adapter of the recycleView
    private ArrayList<WeatherSmall> parseAdapterData(WeatherData obj){
        Log.d(LoginActivity.LOGAPP,"parseAdapterData called for weather object");
        ArrayList<WeatherSmall> weatherSmallList=new ArrayList<>();
        for (int i=1;i<obj.getWeatherInfo().size();i++){
            String hour=obj.getWeatherInfo().get(i).getDate().split(" ")[1].substring(0,5);  //process dt_txt from json "2021-03-25 15:00:00" to only keep "15:00"
            String icon=updateWeatherIcon(obj.getWeatherInfo().get(i).getCondition());
            String temp= Math.round(obj.getWeatherInfo().get(i).getTemperature()) +"°";
            WeatherSmall weatherSmall=new WeatherSmall(hour,icon,temp);
            weatherSmallList.add(weatherSmall);
        }
        return weatherSmallList;
    }

    //function to provide recommendations plants based on weather conditions retrieved by the API
    private String recommendations(WeatherData weather){
        //initialize some boolean variables
        boolean frostAlert=false;
        boolean heatAlert=false;
        boolean rain=false;
        boolean snow=false;
        boolean strongWinds=false;

        ArrayList<WeatherInfoPerTime> myData=weather.getWeatherInfo();
        //raise my flags to true if weather conditions are met
        for (int i=0;i<myData.size();i++){
            double temp=myData.get(i).getTemperature();
            int condition=myData.get(i).getCondition();
            double windSpeed=myData.get(i).getWindSpeed();

            if (temp>32){
                heatAlert=true;
            }else if (temp<1){
                frostAlert=true;
            }else if ((condition >= 600 && condition <= 700)||condition==903){
                snow=true;
            }else if ((condition >= 0 && condition < 600)||(condition >= 772 && condition < 800)||(condition >= 900 && condition <= 902)
            ||(condition >= 905 && condition <= 1000)){
                rain=true;
            }else if (windSpeed>10){
                strongWinds=true;
            }
        }

        //populate my advice string depending on the flags raised
        StringBuilder str=new StringBuilder();
        if (frostAlert||snow){
            str.append(getResources().getString(R.string.frostAdvice));
        }else if (heatAlert){
            str.append(getResources().getString(R.string.heatAdvice));
        }else if (rain){
            str.append(getResources().getString(R.string.rainAdvice));
        }else {
            str.append(getResources().getString(R.string.dryAdvice));
        }

        if (strongWinds){
            str.append(" "+getResources().getString(R.string.strongWindsAdvice));
        }

    return str.toString();
    }


    public void onBackPressed() {
        Intent intent=new Intent(WeatherRecommendations.this,MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
