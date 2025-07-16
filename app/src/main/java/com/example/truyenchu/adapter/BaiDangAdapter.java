package com.example.truyenchu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.BaiDang;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BaiDangAdapter extends RecyclerView.Adapter<BaiDangAdapter.ViewHolder> {

    private final Context context;
    private final List<BaiDang> baiDangList;
    private final OnStoryClickListener listener;

    public interface OnStoryClickListener {
        void onStoryClick(String storyId);
    }

    public BaiDangAdapter(Context context, List<BaiDang> baiDangList, OnStoryClickListener listener) {
        this.context = context;
        this.baiDangList = baiDangList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bai_dang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaiDang baiDang = baiDangList.get(position);
        holder.bind(baiDang, listener);
    }

    @Override
    public int getItemCount() {
        return baiDangList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Views for the user info
        ImageView ivUserAvatar;
        TextView tvUserName;
        // Views for the post content
        TextView tvPostContent;
        ImageView ivPostImage; // The new ImageView for the user's uploaded image
        // Views for the attached story info
        LinearLayout layoutStoryInfo;
        ImageView ivStoryCover;
        TextView tvStoryName, tvStoryAuthor, tvStoryGenre;
        // View for the timestamp
        TextView tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar_post);
            tvUserName = itemView.findViewById(R.id.tv_user_name_post);
            tvPostContent = itemView.findViewById(R.id.tv_post_content);
            ivPostImage = itemView.findViewById(R.id.iv_post_image); // Find the new ImageView by its ID
            layoutStoryInfo = itemView.findViewById(R.id.layout_story_info_post);
            ivStoryCover = itemView.findViewById(R.id.iv_story_cover_post);
            tvStoryName = itemView.findViewById(R.id.tv_story_name_post);
            tvStoryAuthor = itemView.findViewById(R.id.tv_story_author_post);
            tvStoryGenre = itemView.findViewById(R.id.tv_story_genre_post);
            tvTimestamp = itemView.findViewById(R.id.tv_post_timestamp);
        }

        public void bind(final BaiDang baiDang, final OnStoryClickListener listener) {
            // Display user info and post content
            tvUserName.setText(baiDang.getUserName());
            tvPostContent.setText(baiDang.getPostContent());
            Glide.with(context).load(baiDang.getUserAvatar()).circleCrop().into(ivUserAvatar);

            // LOGIC TO DISPLAY THE POST IMAGE
            // Check if the postImage URL exists and is not empty
            if (baiDang.getPostImage() != null && !baiDang.getPostImage().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE); // Make the ImageView visible
                Glide.with(context)
                        .load(baiDang.getPostImage()) // Load the image from the URL
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE); // Hide the ImageView if there is no image
            }

            // Display attached story info
            if (baiDang.getStoryId() != null) {
                layoutStoryInfo.setVisibility(View.VISIBLE);
                tvStoryName.setText(baiDang.getStoryName());
                tvStoryAuthor.setText("Tác giả: " + baiDang.getStoryAuthor());
                tvStoryGenre.setText("Thể loại: " + baiDang.getStoryGenreTags());
                Glide.with(context).load(baiDang.getStoryCoverImage()).into(ivStoryCover);

                // Set click listener for the whole block
                layoutStoryInfo.setOnClickListener(v -> listener.onStoryClick(baiDang.getStoryId()));
            } else {
                layoutStoryInfo.setVisibility(View.GONE);
            }

            // Format and display the timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(baiDang.getTimestamp()));
            tvTimestamp.setText(formattedDate);
        }
    }
}
