package com.example.myplantsguru.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myplantsguru.CheckPhoto;
import com.example.myplantsguru.LoginActivity;
import com.example.myplantsguru.Myplants;
import com.example.myplantsguru.OtherUploads;
import com.example.myplantsguru.OthersPlants;
import com.example.myplantsguru.R;
import com.example.myplantsguru.data.Comment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private DatabaseReference loadedEntryRef;
    private ArrayList<Comment> commentArrayList;
    private String currentUser;
    private String postOwner;


    public CommentAdapter(String currentUser, String postOwner) {
        this.currentUser = currentUser;
        this.postOwner = postOwner;
        if (currentUser.equals(postOwner)){
            this.loadedEntryRef= Myplants.selectedItemDbRef.child(CheckPhoto.COMMENTS);
        }else{
            this.loadedEntryRef= OtherUploads.getSelectedMarkerRef().child(CheckPhoto.COMMENTS);
        }
        loadedEntryRef.addChildEventListener(listener);
        this.commentArrayList=new ArrayList<>();
        Log.d(LoginActivity.LOGAPP,"CommentAdapter created");
    }

    private ChildEventListener listener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Comment comment=(Comment) snapshot.getValue(Comment.class);
            commentArrayList.add(comment);
            Log.d(LoginActivity.LOGAPP,"onChildAdded commentAdapter called: "+comment.getComment());
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            commentArrayList.remove(snapshot.getValue(Comment.class));
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
        private final TextView user;
        private final TextView comment;

        public TextView getUser() {
            return user;
        }

        public TextView getComment() {
            return comment;
        }

        public ViewHolder(View view){
            super(view);
            user=view.findViewById(R.id.comment_user);
            comment=view.findViewById(R.id.comment_text);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        Log.d(LoginActivity.LOGAPP,"Adapter list size is "+commentArrayList.size()+"when getItemCount was called");
        return commentArrayList.size();
    }

    public void cleanup(){
        loadedEntryRef.removeEventListener(listener);
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.comment_row, viewGroup, false);


        Log.d(LoginActivity.LOGAPP,"height of view.getLayoutParams() row: "+view.getLayoutParams().height);
        Log.d(LoginActivity.LOGAPP,"height of viewGroup for comments : "+viewGroup.getHeight());

        // adjust the height of each item of the recycler view to fit 4 items per screen height
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        //get height from CheckPhoto or othersPlants variable once recyclerview is created. You avoid getting 0s this way
        if (viewGroup.getHeight()==0){
            if (currentUser.equals(postOwner)){
                layoutParams.height = (int) (CheckPhoto.commentListHeight * 0.5);
                Log.d(LoginActivity.LOGAPP,"height set from class variable: "+layoutParams.height);
            }else{
                layoutParams.height = (int) (OthersPlants.otherCommentListHeight * 0.5);
                Log.d(LoginActivity.LOGAPP,"height set from class variable: "+layoutParams.height);
            }
        }else{
            layoutParams.height = (int) (viewGroup.getHeight() * 0.5);
            Log.d(LoginActivity.LOGAPP,"height set from viewGroup: "+layoutParams.height);
        }


        view.setLayoutParams(layoutParams);

        Log.d(LoginActivity.LOGAPP,"height of comment row: "+layoutParams.height);
//        Log.d(LoginActivity.LOGAPP,"onCreateViewholder of CommentAdapter called ");

        return new CommentAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getUser().setText(commentArrayList.get(position).getUser());
        viewHolder.getComment().setText(commentArrayList.get(position).getComment());
        Log.d(LoginActivity.LOGAPP,"onBindViewholder of CommentAdapter: "+commentArrayList.get(position).getComment());
    }
}
