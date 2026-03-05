package com.app.snapy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.snapy.R;
import com.app.snapy.activities.CommentActivity;
import com.app.snapy.fierbaseutils.FirebaseUtils;
import com.app.snapy.models.Post;
import com.app.snapy.models.User;
import com.app.snapy.utils.PostStatus;
import com.app.snapy.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {
    public static int totalPostComments = 0;

    List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    Context context;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view_post, parent, false);
        context = parent.getContext();
        MyHolder myHolder = new MyHolder(itemView);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Post postModel = postList.get(position);
        FirebaseUtils.getUser(postModel.getUserID(), new FirebaseUtils.ResponseListener() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.tvName.setText(Utils.getUserName(user.getEmail()));

                if (user.getProfileUrl() != null) {
                    if (user.getProfileUrl().equals("") == false) {
                        Picasso.get().load(user.getProfileUrl()).into(holder.profileImage);
                    }
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });
        holder.tvCaption.setText(postModel.getPostCaption());
        Picasso.get().load(postModel.getPostUrl()).into(holder.ivImagePost);
        holder.ratingBar.setRating(Float.valueOf(postModel.getPostRating()));
        String postKey = postModel.getPostId();
        holder.btnApprovePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.updatePostStatus(postKey, String.valueOf(PostStatus.APPROVED));
                holder.approvalStatusLayout.setVisibility(View.GONE);
            }
        });
        holder.llCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(new Intent(view.getContext(), CommentActivity.class).putExtra("SELECTED_POST", postModel).putExtra("KEY", postKey));
            }
        });
        //count total number of comments
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("posts/" + postKey + "/comments");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.tvCommentCount.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.btnRejectPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.updatePostStatus(postKey, String.valueOf(PostStatus.REJECTED));
                holder.approvalStatusLayout.setVisibility(View.GONE);
            }
        });
        if (postModel.getStatus().equals(String.valueOf(PostStatus.APPROVED))) {
            holder.approvalStatusLayout.setVisibility(View.GONE);
        }

        if (!Utils.isAdmin(context)) {
            holder.tvTime.setVisibility(View.VISIBLE);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                Date date = simpleDateFormat.parse(postModel.getPublishDateTime());
                holder.tvTime.setText(Utils.convertPublishTime(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tvCaption, tvName, tvCommentCount, tvTime;
        Button btnApprovePost, btnRejectPost;
        RatingBar ratingBar;
        LinearLayout parentLayout, approvalStatusLayout;
        ImageView ivImagePost, profileImage;
        LinearLayout llCommentIcon;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            ivImagePost = itemView.findViewById(R.id.ivImagePost);
            btnApprovePost = itemView.findViewById(R.id.btnApprove);
            btnRejectPost = itemView.findViewById(R.id.btnReject);
            ratingBar = itemView.findViewById(R.id.ratingBarPost);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            llCommentIcon = itemView.findViewById(R.id.llCommentIcon);
            parentLayout = itemView.findViewById(R.id.parentlayout);
            profileImage = itemView.findViewById(R.id.ivProfile);
            approvalStatusLayout = itemView.findViewById(R.id.approvalStatusLayout);
        }
    }//end of viewHolder
}

