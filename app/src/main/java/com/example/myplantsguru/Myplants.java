package com.example.myplantsguru;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.adapters.ItemClickSupport;
import com.example.myplantsguru.adapters.MyPlantsAdapter;
import com.example.myplantsguru.data.ImageData;
import com.google.firebase.database.DatabaseReference;

public class Myplants extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyPlantsAdapter myPlantsAdapter;
    public static ImageData selectedItem;
    public static DatabaseReference selectedItemDbRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplants);

        recyclerView =findViewById(R.id.myUploadsList);
        //This will make sure onCreateViewHolder of my adapter gets called
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //add divider between rows
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.my_divider));
        recyclerView.addItemDecoration(divider);

//        recyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(getApplicationContext(), new   RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        Log.d(LoginActivity.LOGAPP, "Clicked list position: " + position);
//                    }
//                })
//        );

        ItemClickSupport.addTo(recyclerView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        Log.d(LoginActivity.LOGAPP, "Clicked list position: " + position);
                        selectedItem=(ImageData) myPlantsAdapter.getMyListData().get(position);
                        selectedItemDbRef=myPlantsAdapter.getDbReferences().get(position);
                        Log.d(LoginActivity.LOGAPP, "Clicked item description: " + selectedItem.getShortDescription());
                        Intent intent=new Intent(Myplants.this,CheckPhoto.class);
                        finish();
                        startActivity(intent);
                    }
                });

        ItemClickSupport.addTo(recyclerView)
                .setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                        Log.d(LoginActivity.LOGAPP, "Long Clicked list position: " + position);
                        return false;
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();
        myPlantsAdapter=new MyPlantsAdapter();
        recyclerView.setAdapter(myPlantsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myPlantsAdapter=new MyPlantsAdapter();
        recyclerView.setAdapter(myPlantsAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        myPlantsAdapter.cleanup();
    }

    public void onBackPressed() {
        Intent intent=new Intent(Myplants.this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}
