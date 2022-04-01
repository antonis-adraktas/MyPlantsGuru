package com.example.myplantsguru.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.LoginActivity;
import com.example.myplantsguru.R;
import com.example.myplantsguru.UploadPhoto;
import com.example.myplantsguru.data.ImageData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyPlantsAdapter extends RecyclerView.Adapter<MyPlantsAdapter.ViewHolder> {

    private DatabaseReference dbMyPlants;
    private ArrayList<ImageData> myListData;
    private ArrayList<DatabaseReference> dbReferences;

    public MyPlantsAdapter() {

        this.dbMyPlants= FirebaseDatabase.getInstance().getReference().child(UploadPhoto.DB_DATA).child(UploadPhoto.replaceDotsWithUnderscore(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        dbMyPlants.addChildEventListener(listener);
        myListData=new ArrayList<>();
        dbReferences=new ArrayList<>();
    }

    public ArrayList<ImageData> getMyListData() {
        return myListData;
    }

    public ArrayList<DatabaseReference> getDbReferences() {
        return dbReferences;
    }

    private ChildEventListener listener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            ImageData imageData=snapshot.getValue(ImageData.class);
            myListData.add(imageData);
            dbReferences.add(snapshot.getRef());
            Log.d(LoginActivity.LOGAPP,"Data added: "+imageData.getShortDescription());
            Log.d(LoginActivity.LOGAPP,"Db reference added: "+snapshot.getRef().toString());
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            myListData.remove(snapshot.getValue(ImageData.class));
            dbReferences.remove(snapshot.getRef());
            notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView shortDescriptionView;
        private final TextView questionView;
        private final TextView locationView;
        private final TextView altitudeView;

        public TextView getShortDescriptionView() {
            return shortDescriptionView;
        }

        public TextView getQuestionView() {
            return questionView;
        }

        public TextView getLocationView() {
            return locationView;
        }

        public TextView getAltitudeView() {
            return altitudeView;
        }

        public ViewHolder(View view){
            super(view);
            shortDescriptionView=view.findViewById(R.id.shortDesc);
            questionView=view.findViewById(R.id.question_row);
            locationView=view.findViewById(R.id.location_row);
            altitudeView=view.findViewById(R.id.myplants_altitude);
        }
    }


    @Override
    public MyPlantsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.myplants_row, viewGroup, false);

        // adjust the height of each item of the recycler view to fit 4 items per screen height
//        Log.d(LoginActivity.LOGAPP,"height of view.getLayoutParams() row: "+view.getLayoutParams().height);
//        Log.d(LoginActivity.LOGAPP,"height of viewGroup for my plants : "+viewGroup.getHeight());
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (viewGroup.getHeight() * 0.25);
        view.setLayoutParams(layoutParams);
//        Log.d(LoginActivity.LOGAPP,"height of myPlants row: "+layoutParams.height);
//        Log.d(LoginActivity.LOGAPP,"onCreateViewHolder of Myplantsadapter called");

        return new MyPlantsAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyPlantsAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getShortDescriptionView().setText("Description: "+myListData.get(position).getShortDescription());
        viewHolder.getQuestionView().setText("Question: "+myListData.get(position).getQuestion());
        String latitude=myListData.get(position).getMyLatLng().getLatitude().toString();
        String longitude=myListData.get(position).getMyLatLng().getLongitude().toString();
//        viewHolder.getLocationView().setText("Lat: " + latitude + "\nLong: " + longitude);
        viewHolder.getLocationView().setText("City: "+myListData.get(position).getCity());
        viewHolder.getAltitudeView().setText("Altitude: "+myListData.get(position).getAltitude()+"m");
//        Log.d(LoginActivity.LOGAPP,"onBindViewholder of MyPlantsAdapter: "+myListData.get(position).getShortDescription());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        Log.d(LoginActivity.LOGAPP,"Adapter list size is "+myListData.size()+" when getItemCount was called");
        return myListData.size();
    }

    public void cleanup(){
        dbMyPlants.removeEventListener(listener);
    }


}
