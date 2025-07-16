package com.example.truyenchu.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.truyenchu.R;
import com.example.truyenchu.model.BaiDang;
import com.example.truyenchu.model.Truyen;
import com.example.truyenchu.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentSnapshot; // Thêm import
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaoBaiDangActivity extends AppCompatActivity {

    private ImageView ivAnhBaiDang;
    private EditText etNoiDung;
    private AutoCompleteTextView actvTenTruyen;
    private Button btnDangBai;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    private Map<String, Truyen> truyenMap = new HashMap<>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_bai_dang);

        ivAnhBaiDang = findViewById(R.id.iv_anh_bai_dang);
        etNoiDung = findViewById(R.id.et_noi_dung_bai_dang);
        actvTenTruyen = findViewById(R.id.actv_ten_truyen);
        btnDangBai = findViewById(R.id.btn_dang_bai);
        progressBar = findViewById(R.id.progressBar_tao_bai_dang);

        mAuth = FirebaseAuth.getInstance();

        // Gọi các hàm đã có của bạn
        loadTruyenSuggestions();
        ivAnhBaiDang.setOnClickListener(v -> openGallery());
        btnDangBai.setOnClickListener(v -> tryDangBai());
    }

    // =================================================================
    // =========== THÊM LẠI CÁC PHƯƠNG THỨC BỊ THIẾU ===================
    // =================================================================

    private void loadTruyenSuggestions() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("truyen");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> tenTruyenList = new ArrayList<>();
                truyenMap.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Truyen truyen = dataSnapshot.getValue(Truyen.class);
                    if (truyen != null) {
                        tenTruyenList.add(truyen.getTen());
                        truyenMap.put(truyen.getTen(), truyen);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(TaoBaiDangActivity.this,
                        android.R.layout.simple_dropdown_item_1line, tenTruyenList);
                actvTenTruyen.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivAnhBaiDang.setImageURI(selectedImageUri);
                    ivAnhBaiDang.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }


    // =================================================================
    // =========== CÁC PHƯƠNG THỨC ĐÃ CHỈNH SỬA ========================
    // =================================================================

    private void tryDangBai() {
        String noiDung = etNoiDung.getText().toString().trim();
        if (noiDung.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        fetchUserAndCreatePost(noiDung);
    }

    private void fetchUserAndCreatePost(String noiDung) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { /* ... */ return; }
        String uid = currentUser.getUid();

        // --- THAY THẾ CODE ĐỌC TỪ REALTIME DATABASE BẰNG CODE NÀY ---
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User userProfile = documentSnapshot.toObject(User.class);
                        if (userProfile == null) {
                            Toast.makeText(TaoBaiDangActivity.this, "Không thể chuyển đổi dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                            setLoading(false);
                            return;
                        }
                        // Khi đã có thông tin, tiếp tục đăng bài
                        if (selectedImageUri != null) {
                            uploadImageAndCreatePost(userProfile, noiDung);
                        } else {
                            createPost(userProfile, noiDung, null);
                        }
                    } else {
                        Toast.makeText(TaoBaiDangActivity.this, "Không tìm thấy thông tin người dùng trong Firestore", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void uploadImageAndCreatePost(User userProfile, String noiDung) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("post_images/" + UUID.randomUUID().toString());

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    createPost(userProfile, noiDung, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Tải ảnh lên thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void createPost(User userProfile, String noiDung, String imageUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("bai_dang");
        String postId = dbRef.push().getKey();
        String tenTruyen = actvTenTruyen.getText().toString().trim();

        BaiDang newPost = new BaiDang();
        newPost.setPostId(postId);
        newPost.setPostContent(noiDung);
        newPost.setTimestamp(System.currentTimeMillis());
        newPost.setUserId(userProfile.getUid());
        newPost.setUserName(userProfile.getDisplayName());
        newPost.setUserAvatar(userProfile.getProfileImage());

        if (imageUrl != null) {
            newPost.setPostImage(imageUrl);
        }

        if (!tenTruyen.isEmpty() && truyenMap.containsKey(tenTruyen)) {
            Truyen selectedTruyen = truyenMap.get(tenTruyen);
            if (selectedTruyen != null) {
                newPost.setStoryId(selectedTruyen.getId());
                newPost.setStoryName(selectedTruyen.getTen());
                newPost.setStoryAuthor(selectedTruyen.getTacGia());
                newPost.setStoryGenreTags(selectedTruyen.getTheLoaiTags());
                newPost.setStoryCoverImage(selectedTruyen.getAnhBia());
            }
        }

        if (postId != null) {
            dbRef.child(postId).setValue(newPost)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Đăng bài thất bại", Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    });
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnDangBai.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnDangBai.setEnabled(true);
        }
    }
}