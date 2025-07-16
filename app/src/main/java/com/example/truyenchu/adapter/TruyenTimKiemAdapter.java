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

public class TruyenTimKiemAdapter extends RecyclerView.Adapter<TruyenTimKiemAdapter.ViewHolder> {
    private final Context context;
    private final List<Truyen> truyenList;
    private final OnItemClickListener listener; // <-- Thêm listener

    // Interface để Activity có thể lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Truyen truyen);
    }

    public TruyenTimKiemAdapter(Context context, List<Truyen> truyenList, OnItemClickListener listener) {
        this.context = context;
        this.truyenList = truyenList;
        this.listener = listener; // <-- Khởi tạo listener
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_truyen_tim_kiem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Truyen truyen = truyenList.get(position);
        holder.bind(truyen, listener); // <-- Truyền listener vào hàm bind
    }

    @Override
    public int getItemCount() {
        return truyenList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnhBia;
        TextView tvTenTruyen, tvTacGia, tvTheLoai;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ item layout. Giả sử ID của chúng là như sau:
            ivAnhBia = itemView.findViewById(R.id.iv_anh_bia_tk);
            tvTenTruyen = itemView.findViewById(R.id.tv_ten_truyen_tk);
            tvTacGia = itemView.findViewById(R.id.tv_tac_gia_tk);
            tvTheLoai = itemView.findViewById(R.id.tv_the_loai_tk);
        }

        // Hàm bind để gán dữ liệu và xử lý sự kiện click
        public void bind(final Truyen truyen, final OnItemClickListener listener) {
            tvTenTruyen.setText(truyen.getTen());
            tvTacGia.setText(truyen.getTacGia());
            tvTheLoai.setText(truyen.getTheLoaiTags());
            Glide.with(itemView.getContext()).load(truyen.getAnhBia()).into(ivAnhBia);

            // Gán sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> listener.onItemClick(truyen));
        }
    }
}
