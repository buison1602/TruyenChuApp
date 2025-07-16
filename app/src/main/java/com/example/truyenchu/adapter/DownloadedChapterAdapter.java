package com.example.truyenchu.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.database.AppDatabase;
import com.example.truyenchu.database.ChuongDao;
import com.example.truyenchu.model.Chuong;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DownloadedChapterAdapter extends RecyclerView.Adapter<DownloadedChapterAdapter.ViewHolder> {

    private final Context context;
    private List<Chuong> chuongList;
    private FirebaseUser currentUser;
    private AppDatabase db;
    private ChuongDao chuongDao;

    public DownloadedChapterAdapter(Context context, List<Chuong> chuongList, FirebaseUser currentUser) {
        this.context = context;
        this.chuongList = chuongList;
        this.currentUser = currentUser;
        this.db = AppDatabase.getInstance(context);
        this.chuongDao = db.chuongDao();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_downloaded_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chuong chuong = chuongList.get(position);
        holder.tvChapterName.setText(chuong.getTen());

        // Lấy tên truyện từ Firebase dựa trên truyenId
        if (chuong.getTruyenId() != null) {
            DatabaseReference truyenRef = FirebaseDatabase.getInstance().getReference()
                    .child("truyen").child(chuong.getTruyenId());
            truyenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String storyName = snapshot.child("ten").getValue(String.class);
                        if (storyName != null) {
                            holder.tvStoryName.setText(storyName);
                        } else {
                            holder.tvStoryName.setText("Truyện không xác định");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.tvStoryName.setText("Truyện không xác định");
                }
            });
        } else {
            holder.tvStoryName.setText("TruyenId không hợp lệ");
        }

        holder.btnDeleteChapter.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && currentUser != null && chuong.getTruyenId() != null && chuong.getId() != null) {
                Chuong chuongToDelete = chuongList.get(adapterPosition);
                String downloadedId = chuongToDelete.getTruyenId() + "_" + chuongToDelete.getId();
                DatabaseReference downloadedRef = FirebaseDatabase.getInstance().getReference()
                        .child("user_library")
                        .child(currentUser.getUid())
                        .child("downloaded")
                        .child(chuongToDelete.getTruyenId())
                        .child(chuongToDelete.getId());

                // Xóa trên Firebase
                downloadedRef.removeValue().addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã xóa chương: " + chuongToDelete.getTen(), Toast.LENGTH_SHORT).show();

                    // Xóa trên Room Database trong thread riêng
                    new Thread(() -> {
                        chuongDao.deleteDownloadedChuong(downloadedId);
                        // Cập nhật UI trên main thread với kiểm tra vị trí hợp lệ
                        ((Activity) context).runOnUiThread(() -> {
                            if (adapterPosition >= 0 && adapterPosition < chuongList.size()) {
                                chuongList.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                                notifyItemRangeChanged(adapterPosition, chuongList.size());
                            } else {
                                notifyDataSetChanged(); // Cập nhật toàn bộ nếu vị trí không hợp lệ
                            }
                        });
                    }).start();
                }).addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi khi xóa chương", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return chuongList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoryName, tvChapterName;
        ImageButton btnDeleteChapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryName = itemView.findViewById(R.id.tv_story_name);
            tvChapterName = itemView.findViewById(R.id.tv_chapter_name);
            btnDeleteChapter = itemView.findViewById(R.id.btn_delete_chapter);
        }
    }
}