package com.app.snapy.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.app.snapy.adapters.CommentAdapter;
import com.app.snapy.databinding.ActivityCommentBinding;
import com.app.snapy.models.Comment;
import com.app.snapy.models.Post;
import com.app.snapy.utils.Session;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends BaseActivity {
    ArrayList<Comment> commentList = new ArrayList<>();

    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    ActivityCommentBinding binding;
    DatabaseReference commentDbReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        setListeners();

    }

    private void initViews() {
        commentDbReference = FirebaseDatabase.getInstance().getReference("posts/"+ getCurrentPostKey()+"/comments");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recyclerview.setLayoutManager(layoutManager);
        binding.recyclerview.getLayoutManager().setMeasurementCacheEnabled(false);
        commentAdapter = new CommentAdapter(commentList,CommentActivity.this);
        binding.recyclerview.setAdapter(commentAdapter);
        //showToast(getCurrentPostID());
        if(Session.CURRENT_USER.getProfileUrl() == null || Session.CURRENT_USER.getProfileUrl().equals("")){
        }else{
            Picasso.get().load(Session.CURRENT_USER.getProfileUrl()).into(binding.ivProfile);
        }
    }

    private void setListeners() {
        binding.ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommentActivity.this,MainActivity.class));
            }
        });//end
        binding.ivPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // showToast("clicked...");
                try{
                    postComment();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });//end
    }
    private void postComment(){
        Comment comment = new Comment();
        comment.setUserId(Session.CURRENT_USER.getUid());
        comment.setCommentCaption(binding.etCommentBody.getText().toString());
        comment.setCommentPublishDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).toString());
        commentList.add(comment);
        //showToast(String.valueOf(commentList.size()));
        commentDbReference.push().setValue(comment);
        commentAdapter.notifyData(commentList);
        binding.etCommentBody.setText("");
    }
    @Override
    protected void onStart() {
        super.onStart();

        Query query = commentDbReference;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot studentsnapshot : dataSnapshot.getChildren())
                    {
                        Comment comment = studentsnapshot.getValue(Comment.class);

                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("unable to post comment...");
            }
        });
    }
    private Post getCurrentPost(){
        Intent intent = getIntent();
        Post post = (Post) intent.getSerializableExtra("SELECTED_POST");
        return post;
    }

    private String getCurrentPostKey(){
        Intent intent = getIntent();
        String postKey = intent.getStringExtra("KEY");
        return postKey;
    }

}