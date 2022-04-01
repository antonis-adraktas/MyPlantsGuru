package com.example.myplantsguru.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.LoginActivity;
import com.example.myplantsguru.R;
import com.example.myplantsguru.WeatherRecommendations;
import com.example.myplantsguru.data.WeatherSmall;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private ArrayList<WeatherSmall> weatherDetails;

    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private final TextView time;
        private final ImageView weatherPic;
        private final TextView temp;
        public Context context;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            time = (TextView) view.findViewById(R.id.weather_time);
            weatherPic=view.findViewById(R.id.weather_icon_small);
            temp=view.findViewById(R.id.temperature_small);
            context=view.getContext();
        }

        public TextView getTime() {
            return time;
        }

        public ImageView getWeatherPic() {
            return weatherPic;
        }

        public TextView getTemp() {
            return temp;
        }

        public Context getContext() {
            return context;
        }
    }

    public WeatherAdapter(ArrayList<WeatherSmall> weatherDetails) {
        this.weatherDetails = weatherDetails;

    }


    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        Log.d(LoginActivity.LOGAPP,"onCreateViewHolder weather adapter called");
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.weather_column, viewGroup, false);

        // adjust the width of each item of the recycler view to fit 5 items per screen width
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (viewGroup.getWidth()==0){
            layoutParams.width = (int) (WeatherRecommendations.recyclerViewHeight * 0.2);
        }else{
            layoutParams.width = (int) (viewGroup.getWidth() * 0.2);
        }
        view.setLayoutParams(layoutParams);

        return new ViewHolder(view);
    }




    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WeatherAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getTime().setText(weatherDetails.get(position).getTime());
        int res=viewHolder.getContext().getResources().getIdentifier(weatherDetails.get(position).getWeatherIcon(),"drawable",viewHolder.getContext().getPackageName());
        viewHolder.getWeatherPic().setImageResource(res);
        viewHolder.getTemp().setText(weatherDetails.get(position).getTemperature());
//        Log.d(LoginActivity.LOGAPP,"onBindViewholder: "+weatherDetails.get(position).getTemperature());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return weatherDetails.size();
    }

}
