package com.example.truyenchu.activity; // Thay bằng package của bạn

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    // Khai báo đầy đủ các View
    private CircleImageView profileAvatarIv;
    private ImageView editAvatarIcon, backToProfile;
    private TextInputEditText editEmail, editDisplayName, editPhoneNumber;
    private MaterialButton btnChangePassword, btnSaveChange;

    // URI của ảnh được chọn từ thư viện
    private Uri imageUri;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    // Launcher để chọn ảnh từ thư viện
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng chờ");
        progressDialog.setMessage("Đang cập nhật thông tin...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Ánh xạ đầy đủ các views
        profileAvatarIv = findViewById(R.id.profile_avatar_iv);
        editAvatarIcon = findViewById(R.id.edit_avatar_icon);
        backToProfile = findViewById(R.id.backToProfile);
        editEmail = findViewById(R.id.editEmail);
        editDisplayName = findViewById(R.id.editDisplayName);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSaveChange = findViewById(R.id.btnSaveChange);

        // Đăng ký ActivityResultLauncher
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                imageUri = uri;
                Glide.with(this).load(imageUri).into(profileAvatarIv);
            } else {
                Toast.makeText(this, "Không có ảnh nào được chọn", Toast.LENGTH_SHORT).show();
            }
        });

        // Tải thông tin người dùng hiện tại lên các view
        loadUserInfo();

        // Gán sự kiện cho các nút
        setupClickListeners();
    }

    private void setupClickListeners() {
        backToProfile.setOnClickListener(v -> finish());

        editAvatarIcon.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Chuyển đến trang đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this, ChangePasswordActivity.class));
        });

        btnSaveChange.setOnClickListener(v -> {
            // Bắt đầu quá trình lưu thay đổi
            saveProfileChanges();
        });
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Email không thể thay đổi, chỉ hiển thị
        editEmail.setText(firebaseUser.getEmail());
        editEmail.setEnabled(false);

        // Lấy thông tin từ Firestore
        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            editDisplayName.setText(user.getDisplayName());
                            editPhoneNumber.setText(user.getPhoneNumber());

                            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                                Glide.with(this).load(user.getProfileImage()).into(profileAvatarIv);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }

    private void saveProfileChanges() {
        String displayName = Objects.requireNonNull(editDisplayName.getText()).toString().trim();
        String phoneNumber = Objects.requireNonNull(editPhoneNumber.getText()).toString().trim();

        if (displayName.isEmpty()) {
            editDisplayName.setError("Tên hiển thị không được để trống");
            editDisplayName.requestFocus();
            return;
        }

        progressDialog.show();

        // Kiểm tra xem người dùng có chọn ảnh mới không
        if (imageUri != null) {
            // Case 1: Có ảnh mới -> Tải ảnh lên trước, sau đó cập nhật thông tin
            uploadAvatarThenUpdateProfile(displayName, phoneNumber);
        } else {
            // Case 2: Không có ảnh mới -> Chỉ cập nhật thông tin text
            updateProfileInfo(displayName, phoneNumber, null);
        }
    }

    private void uploadAvatarThenUpdateProfile(String displayName, String phoneNumber) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference avatarRef = storageReference.child("avatar_users/" + userId);

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Sau khi có URL ảnh, cập nhật profile
                            String imageUrl = uri.toString();
                            updateProfileInfo(displayName, phoneNumber, imageUrl);
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Upload ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileInfo(String displayName, String phoneNumber, String imageUrl) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            progressDialog.dismiss();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        updates.put("phoneNumber", phoneNumber);
        // Chỉ cập nhật link ảnh nếu có link mới
        if (imageUrl != null) {
            updates.put("profileImage", imageUrl);
        }

        db.collection("users").document(firebaseUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng Activity sau khi cập nhật
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}