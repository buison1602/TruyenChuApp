package com.example.truyenchu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.Truyen;
import java.util.List;

public class FeaturedTruyenAdapter extends RecyclerView.Adapter<FeaturedTruyenAdapter.ViewHolder> {
    private final Context context;
    private final List<Truyen> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(Truyen truyen); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    public FeaturedTruyenAdapter(Context context, List<Truyen> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_truyen_featured, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Truyen truyen = list.get(position);
        holder.tvTenTruyen.setText(truyen.getTen());
        Glide.with(context).load(truyen.getAnhBia()).into(holder.ivAnhBia);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(truyen);
        });
    }

    @Override public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnhBia;
        TextView tvTenTruyen;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnhBia = itemView.findViewById(R.id.iv_anh_bia_featured);
            tvTenTruyen = itemView.findViewById(R.id.tv_ten_truyen_featured);
        }
    }
}
