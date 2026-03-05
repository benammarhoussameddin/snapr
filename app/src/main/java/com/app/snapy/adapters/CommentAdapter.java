package com.app.snapy.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.snapy.R;
import com.app.snapy.fierbaseutils.FirebaseUtils;
import com.app.snapy.models.Comment;
import com.app.snapy.models.User;
import com.app.snapy.utils.Session;
import com.app.snapy.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    List<Comment> totalComments;
    Context context;

    int mLastPosition = 0;

    public CommentAdapter(List<Comment> passedListItem, Context context) {
        this.totalComments = passedListItem;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view_comments, parent, false);
        MyViewHolder holder = new MyViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (totalComments.size() > 0) {

            Comment comment = totalComments.get(position);
            FirebaseUtils.getUser(comment.getUserId(), new FirebaseUtils.ResponseListener() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    holder.tvName.setText(Utils.getUserName(user.getEmail()));
                    if (user.getProfileUrl() != null && !user.getProfileUrl().equals("")) {
                        Picasso.get().load(user.getProfileUrl()).into(holder.ivProfile);
                    }
                }

                @Override
                public void onFailure(String error) {

                }
            });
            holder.itemTextView.setText(comment.getCommentCaption());
            //Date for showing status in form of hours ,mins and days
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            try {
                Date date = simpleDateFormat.parse(comment.getCommentPublishDate());
                holder.tvTime.setText(Utils.convertPublishTime(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @Override
    public int getItemCount() {
        return (null != totalComments ? totalComments.size() : 0);
    }

    public void notifyData(ArrayList<Comment> totalComments) {
        this.totalComments = totalComments;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemTextView, tvTime, tvName;
        ImageView ivProfile;

        public MyViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            itemTextView = view.findViewById(R.id.tvCommentCaption);
            tvTime = view.findViewById(R.id.tvTime);
            ivProfile = view.findViewById(R.id.ivProfile);
        }
    }

}
