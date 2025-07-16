package com.example.truyenchu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.example.truyenchu.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageView backToSignin;
    private TextInputEditText editEmailForgot;
    private Button btnSendInstructions;
    private String email;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi");
        progressDialog.setCanceledOnTouchOutside(false);

        backToSignin = findViewById(R.id.backToSignin);
        editEmailForgot = findViewById(R.id.editEmailForgot);
        btnSendInstructions = findViewById(R.id.btnSendInstructions);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            email = intent.getStringExtra("email");
            if (email != null) {
                editEmailForgot.setText(email);
            }
        }

        backToSignin.setOnClickListener(view -> {
            onBackPressed();
        });

        btnSendInstructions.setOnClickListener(view -> {
            validateAndSendResetEmail();
        });
    }

    private void validateAndSendResetEmail() {
        String email = editEmailForgot.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ...", Toast.LENGTH_SHORT).show();
            return;
        }

        sendPasswordResetEmail(email);
    }

    private void sendPasswordResetEmail(String email) {
        progressDialog.setMessage("Đang gửi email đến " + email);
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Email hướng dẫn đã được gửi, vui lòng kiểm tra hộp thư của bạn.", Toast.LENGTH_LONG).show();
                    // Tự động quay về màn hình đăng nhập
                    Intent intent = new Intent(ForgotPasswordActivity.this, SigninActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}