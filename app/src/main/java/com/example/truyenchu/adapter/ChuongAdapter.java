package com.example.truyenchu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.truyenchu.R;
import com.example.truyenchu.database.AppDatabase;
import com.example.truyenchu.database.ChuongDao;
import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.model.DownloadedChuong;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChuongAdapter extends RecyclerView.Adapter<ChuongAdapter.ViewHolder> {
    private final List<Chuong> chuongList;
    private Context context;
    private String truyenId;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private OnChapterClickListener listener; // Thêm listener

    // Interface để xử lý sự kiện click
    public interface OnChapterClickListener {
        void onChapterClick(Chuong chapter);
    }

    public void setOnChapterClickListener(OnChapterClickListener listener) {
        this.listener = listener;
    }

    public ChuongAdapter(Context context, List<Chuong> chuongList) {
        this.context = context;
        this.chuongList = chuongList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chuong, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chuong chuong = chuongList.get(position);
        holder.tvTenChuong.setText(chuong.getTen());
        holder.tvNgayDang.setText(chuong.getNgayDang());

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "app-database")
                .allowMainThreadQueries() // Chỉ dùng tạm thời, nên dùng thread riêng cho production
                .fallbackToDestructiveMigration()
                .build();
        ChuongDao chuongDao = db.chuongDao();

        // Gán sự kiện click cho nút tải xuống
        holder.btnDownload.setOnClickListener(v -> {
            String downloadedId = truyenId + "_" + chuong.getId();
            DownloadedChuong downloadedChuong = chuongDao.getChuong(downloadedId);
            if (downloadedChuong != null) {
                Toast.makeText(context, "Chương đã được tải trước đó", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference chuongRef = FirebaseDatabase.getInstance().getReference()
                    .child("chuong").child(truyenId).child(chuong.getId()).child("noiDung");

            chuongRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String noiDung = snapshot.getValue(String.class);
                    if (noiDung != null) {
                        DownloadedChuong newDownloadedChuong = new DownloadedChuong(truyenId, chuong.getId());
                        newDownloadedChuong.noiDung = noiDung;
                        newDownloadedChuong.ngayDang = chuong.getNgayDang();
                        chuongDao.insertChuong(newDownloadedChuong);

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            DatabaseReference downloadedRef = FirebaseDatabase.getInstance().getReference()
                                    .child("user_library")
                                    .child(currentUser.getUid())
                                    .child("downloaded")
                                    .child(truyenId)
                                    .child(chuong.getId());

                            downloadedRef.setValue(true).addOnSuccessListener(aVoid ->
                                    Toast.makeText(context, "Tải xuống thành công: " + chuong.getTen(), Toast.LENGTH_SHORT).show()
                            ).addOnFailureListener(e ->
                                    Toast.makeText(context, "Không tìm thấy nội dung chương", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        Toast.makeText(context, "Không tìm thấy nội dung chương", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Lỗi khi tải chương", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Gán sự kiện click cho cả item để mở ReadingFragment
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChapterClick(chuong);
            }
        });
    }

    public void setTruyenId(String truyenId) {
        this.truyenId = truyenId;
    }

    @Override
    public int getItemCount() {
        return chuongList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenChuong, tvNgayDang;
        ImageButton btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenChuong = itemView.findViewById(R.id.tv_ten_chuong);
            tvNgayDang = itemView.findViewById(R.id.tv_ngay_dang);
            btnDownload = itemView.findViewById(R.id.btn_download_chapter);
        }
    }
}