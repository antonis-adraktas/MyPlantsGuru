package com.example.myplantsguru;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CheckPhoto extends AppCompatActivity {

    public static final String COMMENTS="Comments";

    private ImageView downloadPic;
    private AutoCompleteTextView mDescription;
    private AutoCompleteTextView mQuestion;
    private AutoCompleteTextView mAddComment;
    private RecyclerView mCommentList;
    private StorageReference storageReference;
    private ImageButton mSendComment;
    private ImageButton mDelete;
    private ImageButton mEdit;
    private TextView mAltitude;
    private Button mSave;
    private Button mDiscard;
    private CommentAdapter commentAdapter;
    public static int commentListHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_photo);

        downloadPic=findViewById(R.id.downloadPic);
        downloadPic.setClickable(false);
        mDescription=findViewById(R.id.item_description);
        mDescription.setKeyListener(null);
        mQuestion=findViewById(R.id.item_question);
        mQuestion.setKeyListener(null);
        mAddComment=findViewById(R.id.addComment);
        mSendComment=findViewById(R.id.sendComment);
        mDelete=findViewById(R.id.delete_button);
        mEdit=findViewById(R.id.edit_button);
        mSave=findViewById(R.id.save_button);
        mSave.setVisibility(View.INVISIBLE);
        mSave.setClickable(false);
        mDiscard=findViewById(R.id.discard);
        mDiscard.setVisibility(View.INVISIBLE);
        mDiscard.setClickable(false);
        mAltitude=findViewById(R.id.myPhoto_altitude);

        mCommentList=findViewById(R.id.commentList);
        //This will make sure onCreateViewHolder of my adapter gets called
        mCommentList.setLayoutManager(new LinearLayoutManager(this));

        mDescription.setText("Description: "+Myplants.selectedItem.getShortDescription());
        mQuestion.setText("Question: "+Myplants.selectedItem.getQuestion());
        mAltitude.setText("Altitude: "+Myplants.selectedItem.getAltitude()+"m");
        storageReference= FirebaseStorage.getInstance().getReference().child(Myplants.selectedItem.getStoragePath());
        Log.d(LoginActivity.LOGAPP,"storageReference "+storageReference.toString());

        //Glide module implemented in MyAppGlideModule class, dependencies setup in gradle, then rebuild project.
        //GlideApp is available for use to download Firebase storage pics directly to my imageView
        GlideApp.with(CheckPhoto.this).load(storageReference).into(downloadPic);

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myComment=mAddComment.getText().toString();
                Comment newComment=new Comment(MainActivity.CURRENT_USER,myComment);
                Myplants.selectedItemDbRef.child(COMMENTS).push().setValue(newComment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CheckPhoto.this, "Your comment was successfully added", Toast.LENGTH_SHORT).show();
                        mAddComment.setText(null);
                        //this will hide the keyboard once send is clicked, this fixes a bug with recycler view where layout height was considered smaller due to the keyboard
                        //comments were smaller and not properly visible
                        hideKeyboard(CheckPhoto.this);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                commentAdapter=new CommentAdapter(MainActivity.CURRENT_USER,Myplants.selectedItem.getUserEmail());
                                mCommentList.setAdapter(commentAdapter);
                            }
                        },500);  // set the adapter after 500 millis to give time to close the keyboard

                    }
                });
            }
        });

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
                        commentListHeight=viewHeight;
                        commentAdapter=new CommentAdapter(MainActivity.CURRENT_USER,Myplants.selectedItem.getUserEmail());
                        mCommentList.setAdapter(commentAdapter);
                }
            });
        }

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteConfirmationDialog();
            }
        });

        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDialogInfo();
            }
        });

    }

    private void deleteConfirmationDialog(){
        new AlertDialog.Builder(CheckPhoto.this)
                .setMessage(R.string.deleteConfirmation)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void editDialogInfo(){
        new AlertDialog.Builder(CheckPhoto.this)
                .setMessage(R.string.editInfo)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

    }

    private void delete(){
        Myplants.selectedItemDbRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {         //this removes entry from realtime db
            @Override
            public void onSuccess(Void aVoid) {
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {               //this deletes picture file from storage
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CheckPhoto.this, "Your post is successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(CheckPhoto.this, Myplants.class);
                finish();
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CheckPhoto.this, "Failed to delete this post. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void edit(){
        changeButtonsEdit();
        mDescription.setKeyListener(TextKeyListener.getInstance());
        mQuestion.setKeyListener(TextKeyListener.getInstance());
        mDescription.setText(Myplants.selectedItem.getShortDescription());
        mQuestion.setText(Myplants.selectedItem.getQuestion());

        mDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonsInitial();
                //reload the initial values
                mDescription.setText("Description: "+Myplants.selectedItem.getShortDescription());
                mQuestion.setText("Question: "+Myplants.selectedItem.getQuestion());
                GlideApp.with(CheckPhoto.this).load(storageReference).into(downloadPic);
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDesc=mDescription.getText().toString();
                String newQuestion=mQuestion.getText().toString();
                Myplants.selectedItemDbRef.child("question").setValue(newQuestion).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Myplants.selectedItemDbRef.child("shortDescription").setValue(newDesc).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CheckPhoto.this, "Changes submitted successfully", Toast.LENGTH_SHORT).show();
                                mDescription.setKeyListener(null);
                                mQuestion.setKeyListener(null);
                                Myplants.selectedItemDbRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Myplants.selectedItem=snapshot.getValue(ImageData.class);
                                        mDescription.setText("Description: "+Myplants.selectedItem.getShortDescription());
                                        mQuestion.setText("Question: "+Myplants.selectedItem.getQuestion());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(CheckPhoto.this, "Database read error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                changeButtonsInitial();
                            }
                        });
                    }
                });

            }
        });
    }

    public void onBackPressed() {
        Intent intent=new Intent(CheckPhoto.this,Myplants.class);
        finish();
        startActivity(intent);
    }

    private void changeButtonsEdit(){
        mDelete.setVisibility(View.INVISIBLE);
        mDelete.setClickable(false);
        mEdit.setVisibility(View.INVISIBLE);
        mEdit.setClickable(false);
        mSave.setClickable(true);
        mSave.setVisibility(View.VISIBLE);
        mDiscard.setClickable(true);
        mDiscard.setVisibility(View.VISIBLE);
    }
    private void changeButtonsInitial(){
        mSave.setVisibility(View.INVISIBLE);
        mSave.setClickable(false);
        mDiscard.setVisibility(View.INVISIBLE);
        mDiscard.setClickable(false);
        mEdit.setClickable(true);
        mEdit.setVisibility(View.VISIBLE);
        mDelete.setClickable(true);
        mDelete.setVisibility(View.VISIBLE);
    }



    @Override
    protected void onStop() {
        super.onStop();
        commentAdapter.cleanup();
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
