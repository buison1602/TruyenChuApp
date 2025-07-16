package com.example.truyenchu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.BinhLuan;
import java.util.List;

public class BinhLuanAdapter extends RecyclerView.Adapter<BinhLuanAdapter.ViewHolder> {
    private final List<BinhLuan> binhLuanList;

    public BinhLuanAdapter(List<BinhLuan> binhLuanList) {
        this.binhLuanList = binhLuanList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_binh_luan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BinhLuan binhLuan = binhLuanList.get(position);
        holder.tvTenNguoiDung.setText(binhLuan.getTenNguoiDung());
        holder.tvThoiGian.setText(binhLuan.getThoiGian());
        holder.tvNoiDung.setText(binhLuan.getNoiDung());
        holder.tvSoLike.setText(String.valueOf(binhLuan.getSoLike()));
        Glide.with(holder.itemView.getContext())
                .load(binhLuan.getAvatarUrl())
                .placeholder(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(holder.ivAvatar);
    }

    @Override
    public int getItemCount() {
        return binhLuanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvTenNguoiDung, tvThoiGian, tvNoiDung, tvSoLike;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvTenNguoiDung = itemView.findViewById(R.id.tv_ten_nguoi_dung);
            tvThoiGian = itemView.findViewById(R.id.tv_thoi_gian);
            tvNoiDung = itemView.findViewById(R.id.tv_noi_dung);
            tvSoLike = itemView.findViewById(R.id.tv_so_like);
        }
    }
}