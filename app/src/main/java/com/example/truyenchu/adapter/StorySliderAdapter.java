package com.example.truyenchu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.Truyen;

import java.util.List;

public class StorySliderAdapter extends RecyclerView.Adapter<StorySliderAdapter.SliderViewHolder> {

    private final List<Truyen> storyList; // Đổi lại thành List<Truyen>
    private final Context context;
    private final OnItemClickListener listener; // Thêm lại listener

    public interface OnItemClickListener {
        void onItemClick(Truyen truyen);
    }

    public StorySliderAdapter(Context context, List<Truyen> storyList, OnItemClickListener listener) {
        this.context = context;
        this.storyList = storyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Truyen story = storyList.get(position);

        // Tải ảnh từ link trong Firebase
        Glide.with(context)
                .load(story.getBannerImage()) // Lấy ảnh banner
                .into(holder.imageView);

        // Gán lại sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(story));
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_slider_image);
        }
    }
}
