package edu.neu.madcourse.musicloud.comments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.ArrayList;
import org.ocpsoft.prettytime.PrettyTime;

import edu.neu.madcourse.musicloud.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private ArrayList<Comment> commentsList;

    public RecyclerViewAdapter(ArrayList<Comment> commentsList) {
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_card, parent, false);
        return new RecyclerViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        PrettyTime p = new PrettyTime();

        Glide.with(holder.itemView).load(comment.getUser().getProfileImage()).into(holder.commentUserImg);

        holder.commentUsername.setText(comment.getUser().getUsername());
        holder.commentContent.setText(comment.getContent());
        holder.commentTime.setText(p.format(comment.getDate()));
        holder.commentLikes.setText(Integer.toString(comment.getLikeCnt()));
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

}
