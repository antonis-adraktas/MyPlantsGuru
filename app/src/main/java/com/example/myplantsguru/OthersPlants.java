package com.example.myplantsguru;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.adapters.CommentAdapter;
import com.example.myplantsguru.data.Comment;
import com.example.myplantsguru.data.ImageData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OthersPlants extends AppCompatActivity {

    public static final String COMMENTS="Comments";

    private ImageView downloadPic;
    private AutoCompleteTextView mDescription;
    private AutoCompleteTextView mQuestion;
    private AutoCompleteTextView mAddComment;
    private TextView mAltitudeText;
    private RecyclerView mCommentList;
    private StorageReference storageReference;
    private ImageButton mSendComment;
    private CommentAdapter commentAdapter;
    public static int otherCommentListHeight;
    private ImageData postLoaded;

    private int count=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_others_photo);

        downloadPic=findViewById(R.id.downloadPic_other);
        downloadPic.setClickable(false);
        mDescription=findViewById(R.id.other_description);
        mDescription.setKeyListener(null);
        mQuestion=findViewById(R.id.other_question);
        mQuestion.setKeyListener(null);
        mAddComment=findViewById(R.id.other_addComment);
        mSendComment=findViewById(R.id.other_sendComment);
        mAltitudeText=findViewById(R.id.other_altitude);


        mCommentList=findViewById(R.id.other_commentList);
        //This will make sure onCreateViewHolder of my adapter gets called
        mCommentList.setLayoutManager(new LinearLayoutManager(this));

        count=0;

        OtherUploads.getSelectedMarkerRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postLoaded=(ImageData) snapshot.getValue(ImageData.class);
                mDescription.setText("Description: "+postLoaded.getShortDescription());
                mQuestion.setText("Question: "+postLoaded.getQuestion());
                mAltitudeText.setText("Altitude: "+postLoaded.getAltitude()+"m");
                storageReference= FirebaseStorage.getInstance().getReference().child(postLoaded.getStoragePath());
                Log.d(LoginActivity.LOGAPP,"storageReference "+storageReference.toString());

                //Glide module implemented in MyAppGlideModule class, dependencies setup in gradle, then rebuild project.
                //GlideApp is available for use to download Firebase storage pics directly to my imageView
                if (count==0){                              //counter will help to avoid reloading the pic every time a comment is added
                    GlideApp.with(OthersPlants.this).load(storageReference).into(downloadPic);
                    count=1;
                }


                //this is added to pass the recyclerview height to the adapter. Without it seems like adapter is called when recyclerview is empty which means getHeight returns 0.
                //Then onCreateViewHolder cannot create the view properly if you use layoutParams to edit height. Strange cause same implementation worked in Myplants.java.
                ViewTreeObserver viewTreeObserver = mCommentList.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int viewHeight = mCommentList.getHeight();
                            if (viewHeight != 0)
                                mCommentList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            Log.d(LoginActivity.LOGAPP,"viewHeight of comment list: "+viewHeight);
                            otherCommentListHeight=viewHeight;
                            commentAdapter=new CommentAdapter(MainActivity.CURRENT_USER,postLoaded.getUserEmail());
                            mCommentList.setAdapter(commentAdapter);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(LoginActivity.LOGAPP,"Data fetch from db failed");
            }
        });






        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myComment=mAddComment.getText().toString();
                Comment newComment=new Comment(MainActivity.CURRENT_USER,myComment);
                OtherUploads.getSelectedMarkerRef().child(COMMENTS).push().setValue(newComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(OthersPlants.this, "Your comment was successfully added", Toast.LENGTH_SHORT).show();
                        mAddComment.setText(null);
                        //this will hide the keyboard once send is clicked, this fixes a bug with recycler view where layout height was considered smaller due to the keyboard
                        //comments were smaller and not properly visible
                        CheckPhoto.hideKeyboard(OthersPlants.this);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                commentAdapter=new CommentAdapter(MainActivity.CURRENT_USER,postLoaded.getUserEmail());
                                mCommentList.setAdapter(commentAdapter);
                            }
                        },500);  // set the adapter after 500 millis to give time to close the keyboard

                    }
                });

            }
        });




    }

    public void onBackPressed() {
        Intent intent=new Intent(OthersPlants.this,OtherUploads.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        commentAdapter.cleanup();
    }
}
