package com.example.truyenchu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.adapter.DownloadedChapterAdapter;
import com.example.truyenchu.model.Chuong;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDownloadedActivity extends AppCompatActivity {

    private static final String TAG = "ManageDownloadedActivity";
    private ImageView backToProfile;
    private RecyclerView rvDownloadedChapters;
    private DownloadedChapterAdapter adapter;
    private List<Chuong> downloadedChapters;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_downloaded);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ views
        backToProfile = findViewById(R.id.backToProfile);
        rvDownloadedChapters = findViewById(R.id.rv_downloaded_chapters);

        // Setup RecyclerView
        downloadedChapters = new ArrayList<>();
        adapter = new DownloadedChapterAdapter(this, downloadedChapters, mAuth.getCurrentUser());
        rvDownloadedChapters.setLayoutManager(new LinearLayoutManager(this));
        rvDownloadedChapters.setAdapter(adapter);

        // Sự kiện quay lại
        backToProfile.setOnClickListener(v -> onBackPressed());

        // Load danh sách chương đã tải xuống
        loadDownloadedChapters();
    }

    private void loadDownloadedChapters() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để quản lý tải xuống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference downloadedRef = dbRef.child("user_library")
                .child(currentUser.getUid())
                .child("downloaded");

        downloadedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                downloadedChapters.clear();
                for (DataSnapshot truyenSnapshot : snapshot.getChildren()) {
                    String truyenId = truyenSnapshot.getKey();
                    for (DataSnapshot chuongSnapshot : truyenSnapshot.getChildren()) {
                        String chuongId = chuongSnapshot.getKey();
                        // Kiểm tra nếu giá trị là true (flag tải xuống)
                        if (chuongSnapshot.getValue(Boolean.class) != null && chuongSnapshot.getValue(Boolean.class)) {
                            // Truy vấn chi tiết từ bảng chuong/TruyenId/chuongId
                            DatabaseReference chuongRef = dbRef.child("chuong").child(truyenId).child(chuongId);
                            chuongRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot chuongDataSnapshot) {
                                    Log.d(TAG, "Dữ liệu từ chuong/" + truyenId + "/" + chuongId + ": " + chuongDataSnapshot.getValue());
                                    Chuong chuong = chuongDataSnapshot.getValue(Chuong.class);
                                    if (chuong != null) {
                                        chuong.setId(chuongId);
                                        chuong.setTruyenId(truyenId);
                                        downloadedChapters.add(chuong);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.w(TAG, "Không tìm thấy thông tin chương tại chuong/" + truyenId + "/" + chuongId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Lỗi khi truy vấn chi tiết chương", error.toException());
                                }
                            });
                        } else {
                            Log.w(TAG, "Dữ liệu không phải flag true tại truyenId: " + truyenId + ", chuongId: " + chuongId);
                        }
                    }
                }
                // Đảm bảo UI được cập nhật sau khi xử lý
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDownloadedActivity.this, "Lỗi khi tải danh sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi khi tải danh sách", error.toException());
            }
        });
    }
}