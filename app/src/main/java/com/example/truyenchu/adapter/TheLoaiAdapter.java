package com.example.truyenchu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.truyenchu.R;
import com.example.truyenchu.model.TheLoai;
import java.util.List;

public class TheLoaiAdapter extends RecyclerView.Adapter<TheLoaiAdapter.ViewHolder> {
    private final List<TheLoai> theLoaiList;

    public TheLoaiAdapter(List<TheLoai> theLoaiList) {
        this.theLoaiList = theLoaiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_the_loai, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TheLoai theLoai = theLoaiList.get(position);
        holder.tvTenTheLoai.setText(theLoai.getTen());
        // Highlight tab đầu tiên
        if (position == 0) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return theLoaiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenTheLoai;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenTheLoai = itemView.findViewById(R.id.tv_ten_the_loai);
        }
    }
}
