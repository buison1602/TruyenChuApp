package com.example.truyenchu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.truyenchu.MainActivity;
import com.example.truyenchu.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private ImageView backToProfile;
    private TextInputEditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private Button btnUpdatePassword;

    // Khai báo các biến cần thiết
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setMessage("Đang cập nhật mật khẩu...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Ánh xạ views từ layout
        backToProfile = findViewById(R.id.backToProfile);
        editCurrentPassword = findViewById(R.id.editCurrentPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Gán sự kiện cho các nút
        setupClickListeners();
    }

    private void setupClickListeners() {
        backToProfile.setOnClickListener(v -> {
            onBackPressed(); // Quay lại màn hình trước đó
        });

        btnUpdatePassword.setOnClickListener(v -> {
            validateAndChangePassword();
        });
    }


    private void validateAndChangePassword() {
        String currentPassword = editCurrentPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // --- Bắt đầu kiểm tra dữ liệu ---
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu mọi thứ hợp lệ, tiến hành xác thực lại và đổi mật khẩu
        reauthenticateAndChangePassword(currentPassword, newPassword);
    }

    private void reauthenticateAndChangePassword(String currentPassword, String newPassword) {
        progressDialog.show();

        FirebaseUser user = mAuth.getCurrentUser();

        // Nếu không có user, không làm gì cả
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một "chứng chỉ" từ email và mật khẩu hiện tại của người dùng
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        // Yêu cầu người dùng xác thực lại bằng chứng chỉ trên
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Xác thực lại thành công, bây giờ tiến hành đổi mật khẩu mới
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    // Tắt dialog chờ
                                    progressDialog.dismiss();
                                    if (updateTask.isSuccessful()) {
                                        // Đổi mật khẩu thành công!
                                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                        // Đóng màn hình này và quay lại
                                        Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        // Đổi mật khẩu thất bại
                                        Toast.makeText(this, "Lỗi: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Xác thực lại thất bại (sai mật khẩu hiện tại)
                        progressDialog.dismiss();
                        Toast.makeText(this, "Mật khẩu hiện tại không chính xác.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}