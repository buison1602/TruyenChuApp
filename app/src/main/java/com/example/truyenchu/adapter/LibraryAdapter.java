package com.example.truyenchu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.model.Truyen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private final Context context;
    private List<Object> itemList; // Sử dụng Object để hỗ trợ cả Truyen và Chuong
    private String category;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Object item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public LibraryAdapter(Context context, List<Object> itemList, String category) {
        this.context = context;
        this.itemList = itemList;
        this.category = category;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_library_truyen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = itemList.get(position);

        if (item instanceof Truyen) {
            Truyen truyen = (Truyen) item;
            holder.title.setText(truyen.getTen());

            Glide.with(context)
                    .load(truyen.getAnhBia())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(holder.cover);
            holder.chuongText.setVisibility(View.GONE);

            if (holder.newChapterBadge != null) {
                holder.newChapterBadge.setVisibility(View.GONE);
            }
        }
        else if (item instanceof Chuong) {
            Chuong chuong = (Chuong) item;
            holder.chuongText.setText(chuong.getTen());
            holder.chuongText.setVisibility(View.VISIBLE);

            DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference()
                    .child("truyen").child(chuong.getTruyenId());

            // Lấy dữ liệu từ Firebase
            libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Truyen truyen = snapshot.getValue(Truyen.class);
                    if (truyen != null) {
                        holder.title.setText(truyen.getTen());
                        Glide.with(context)
                                .load(truyen.getAnhBia())
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(holder.cover);
                    } else {
                        holder.title.setText(chuong.getTruyenId());
                        Glide.with(context)
                                .load(R.drawable.default_avatar)
                                .into(holder.cover);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("LibraryAdapter", "Failed to load truyen data: " + error.getMessage());
                    // Sử dụng giá trị mặc định nếu có lỗi
                    holder.title.setText(chuong.getTruyenId());
                    Glide.with(context)
                            .load(R.drawable.default_avatar)
                            .into(holder.cover);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, chuongText, newChapterBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.iv_anh_bia_featured);
            title = itemView.findViewById(R.id.tv_ten_truyen_featured);
            newChapterBadge = itemView.findViewById(R.id.tv_new_chapter_badge);
            chuongText = itemView.findViewById(R.id.tv_chuong_truyen_featured);
        }
    }
}