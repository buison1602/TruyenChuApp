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

public class TruyenTrendingAdapter extends RecyclerView.Adapter<TruyenTrendingAdapter.ViewHolder> {

    private final Context context;
    private final List<Truyen> truyenList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Truyen truyen);
    }

    public TruyenTrendingAdapter(Context context, List<Truyen> truyenList, OnItemClickListener listener) {
        this.context = context;
        this.truyenList = truyenList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_truyen_trending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Truyen truyen = truyenList.get(position);
        holder.bind(truyen, position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return truyenList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSoThuTu, tvTenTruyen, tvTacGia, tvTheLoai;
        ImageView ivAnhBia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSoThuTu = itemView.findViewById(R.id.tv_so_thu_tu);
            tvTenTruyen = itemView.findViewById(R.id.tv_ten_truyen);
            tvTacGia = itemView.findViewById(R.id.tv_tac_gia);
            tvTheLoai = itemView.findViewById(R.id.tv_the_loai);
            ivAnhBia = itemView.findViewById(R.id.iv_anh_bia);
        }

        public void bind(final Truyen truyen, int rank, final OnItemClickListener listener) {
            tvSoThuTu.setText(String.valueOf(rank));
            tvTenTruyen.setText(truyen.getTen());
            tvTacGia.setText(truyen.getTacGia());
            tvTheLoai.setText(truyen.getTheLoaiTags());

            Glide.with(itemView.getContext())
                    .load(truyen.getAnhBia())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivAnhBia);

            itemView.setOnClickListener(v -> listener.onItemClick(truyen));
        }
    }
}