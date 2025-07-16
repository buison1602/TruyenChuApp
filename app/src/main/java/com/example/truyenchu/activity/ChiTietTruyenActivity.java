package com.example.truyenchu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Import EditText
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.adapter.BinhLuanAdapter;
import com.example.truyenchu.adapter.ChuongAdapter;
import com.example.truyenchu.fragment.ReadingFragment;
import com.example.truyenchu.model.BinhLuan;
import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.model.Truyen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChiTietTruyenActivity extends AppCompatActivity {

    private ImageView ivAnhBia;
    private TextView tvTenTruyen, tvTacGia, tvTrangThai, tvDanhGia, tvMoTa, tvTheLoai;
    private RecyclerView rvChuong, rvBinhLuan;
    private ChuongAdapter chuongAdapter;
    private BinhLuanAdapter binhLuanAdapter;
    private List<Chuong> chuongList = new ArrayList<>();
    private List<BinhLuan> binhLuanList = new ArrayList<>();
    private String truyenId;
    private DatabaseReference database;

    private Button btnFollow;
    private Button btnRead;
    private FirebaseAuth mAuth;
    private boolean isTruyenInLibrary = false;

    private EditText etCommentInput; // Added
    private ImageButton btnSendComment; // Added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_truyen);

        truyenId = getIntent().getStringExtra("TRUYEN_ID");
        if (truyenId == null || truyenId.isEmpty()) {
            finish();
            return;
        }
        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerViews();
        loadAllData();
        setupCommentSection(); // Added
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_chi_tiet);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        ivAnhBia = findViewById(R.id.iv_anh_bia_detail);
        tvTenTruyen = findViewById(R.id.tv_ten_truyen_detail);
        tvTacGia = findViewById(R.id.tv_tac_gia_detail);
        tvTheLoai = findViewById(R.id.tv_the_loai_detail);
        tvTrangThai = findViewById(R.id.tv_trang_thai_detail);
        tvDanhGia = findViewById(R.id.tv_danh_gia_detail);
        tvMoTa = findViewById(R.id.tv_mo_ta_detail);
        rvChuong = findViewById(R.id.rv_chuong);
        rvBinhLuan = findViewById(R.id.rv_binh_luan);
        btnFollow = findViewById(R.id.btn_follow);
        btnRead = findViewById(R.id.btn_read);
        etCommentInput = findViewById(R.id.et_comment_input); // Added
        btnSendComment = findViewById(R.id.btn_send_comment); // Added
    }

    private void setupRecyclerViews() {
        rvChuong.setLayoutManager(new LinearLayoutManager(this));
        rvChuong.setNestedScrollingEnabled(false);
        chuongAdapter = new ChuongAdapter(this, chuongList);
        chuongAdapter.setTruyenId(truyenId);
        // Thêm listener để xử lý click vào chương
        chuongAdapter.setOnChapterClickListener(chapter -> {
            ReadingFragment readingFragment = new ReadingFragment();

            Bundle bundle = new Bundle();
            bundle.putString(ReadingFragment.KEY_STORY_ID, truyenId);
            bundle.putString(ReadingFragment.KEY_CHUONG_ID, chapter.getId());
            readingFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, readingFragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvChuong.setAdapter(chuongAdapter);

        rvBinhLuan.setLayoutManager(new LinearLayoutManager(this));
        rvBinhLuan.setNestedScrollingEnabled(false);
        binhLuanAdapter = new BinhLuanAdapter(binhLuanList);
        rvBinhLuan.setAdapter(binhLuanAdapter);
    }

    private void loadAllData() {
        loadTruyenInfo();
        loadChuongList();
        loadBinhLuanList();
        setupFollowButton();
        setupReadButton();
    }

    private void setupReadButton() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            btnRead.setText("Đăng nhập để đọc");
            btnRead.setEnabled(false);
            return;
        }

        DatabaseReference libraryRef = database.child("user_library")
                .child(currentUser.getUid())
                .child("reading")
                .child(truyenId);

        btnRead.setOnClickListener(v -> {
            libraryRef.setValue(true).addOnSuccessListener(aVoid ->
                    Toast.makeText(ChiTietTruyenActivity.this, "Đã thêm vào danh sách đang đọc", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(ChiTietTruyenActivity.this, "Lỗi khi thêm vào danh sách đang đọc", Toast.LENGTH_SHORT).show()
            );

            ReadingFragment readingFragment = new ReadingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ReadingFragment.KEY_STORY_ID, truyenId);
            readingFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, readingFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadTruyenInfo() {
        database.child("truyen").child(truyenId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Truyen truyen = snapshot.getValue(Truyen.class);
                if (truyen != null) {
                    tvTenTruyen.setText(truyen.getTen());
                    tvTacGia.setText(truyen.getTacGia());
                    tvTheLoai.setText(truyen.getTheLoaiTags());
                    tvTrangThai.setText(truyen.getTrangThai());
                    tvMoTa.setText(truyen.getMoTa());
                    String danhGiaText = String.format(Locale.US, "%.1f (%d reviews)", truyen.getDanhGia(), truyen.getSoLuongDanhGia());
                    tvDanhGia.setText(danhGiaText);
                    Glide.with(ChiTietTruyenActivity.this).load(truyen.getAnhBia()).into(ivAnhBia);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadChuongList() {
        database.child("chuong").child(truyenId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chuongList.clear();
                for (DataSnapshot chuongSnapshot : snapshot.getChildren()) {
                    Chuong chuong = chuongSnapshot.getValue(Chuong.class);
                    if (chuong != null) {
                        chuong.setId(chuongSnapshot.getKey()); // Gán id từ key
                        chuongList.add(chuong);
                    }
                }
                chuongAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadBinhLuanList() {
        // Use addValueEventListener to listen for real-time updates to comments
        database.child("binh_luan").child(truyenId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binhLuanList.clear();
                for (DataSnapshot blSnapshot : snapshot.getChildren()) {
                    BinhLuan binhLuan = blSnapshot.getValue(BinhLuan.class);
                    if (binhLuan != null) {
                        binhLuanList.add(binhLuan);
                    }
                }
                binhLuanAdapter.notifyDataSetChanged();
                rvBinhLuan.scrollToPosition(binhLuanList.size() - 1); // Scroll to the latest comment
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupFollowButton() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            btnFollow.setText("Đăng nhập để theo dõi");
            btnFollow.setEnabled(false);
            return;
        }

        btnFollow.setEnabled(true);
        DatabaseReference libraryRef = database.child("user_library")
                .child(currentUser.getUid())
                .child("followed")
                .child(truyenId);

        libraryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isTruyenInLibrary = snapshot.exists();
                updateFollowButtonState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChiTietTruyenActivity.this, "Lỗi kiểm tra tủ sách", Toast.LENGTH_SHORT).show();
            }
        });

        btnFollow.setOnClickListener(v -> toggleFollowStatus(currentUser.getUid()));
    }

    private void updateFollowButtonState() {
        if (isTruyenInLibrary) {
            btnFollow.setText("Bỏ theo dõi");
            btnFollow.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        } else {
            btnFollow.setText("+ Theo dõi");
            btnFollow.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        }
    }

    private void toggleFollowStatus(String userId) {
        DatabaseReference libraryRef = database.child("user_library").child(userId).child("followed").child(truyenId);

        if (isTruyenInLibrary) {
            libraryRef.removeValue().addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show()
            );
        } else {
            libraryRef.setValue(true).addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Đã theo dõi", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // New method to set up comment sending
    private void setupCommentSection() {
        btnSendComment.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(ChiTietTruyenActivity.this, "Vui lòng đăng nhập để bình luận.", Toast.LENGTH_SHORT).show();
                return;
            }

            String commentText = etCommentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(ChiTietTruyenActivity.this, "Bình luận không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique ID for the comment
            String commentId = database.child("binh_luan").child(truyenId).push().getKey();
            if (commentId == null) {
                Toast.makeText(ChiTietTruyenActivity.this, "Không thể tạo ID bình luận.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current user's display name and a dummy avatar URL (you'll need to replace this)
            String userName = currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = "Người dùng ẩn danh"; // Fallback if display name is not set
            }
            String userAvatarUrl = ""; // Replace with actual user avatar URL if you have one

            // Get current timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            BinhLuan newComment = new BinhLuan();
            newComment.setId(commentId);
            newComment.setTenNguoiDung(userName);
            newComment.setAvatarUrl(userAvatarUrl);
            newComment.setNoiDung(commentText);
            newComment.setThoiGian(currentTime);
            newComment.setSoLike(0); // Initialize likes to 0

            database.child("binh_luan").child(truyenId).child(commentId).setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ChiTietTruyenActivity.this, "Bình luận đã được gửi.", Toast.LENGTH_SHORT).show();
                        etCommentInput.setText(""); // Clear the input field
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChiTietTruyenActivity.this, "Lỗi khi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}