package com.example.truyenchu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.truyenchu.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText editDisplayName, editEmail, editPassword;
    private Button btnSignUp, btnGoogle;
    private TextView textLoginLink;
    private String displayName, email, password;
    private FirebaseAuth mAuth;

    // used for SignIn by GG
    private GoogleSignInClient mGoogleSignInClient;

    // Sử dụng ActivityResultLauncher để xử lý kết quả trả về từ Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Đăng nhập Google thành công, bây giờ xác thực với Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Đăng nhập Google thất bại
                        Log.w("GoogleSignIn", "Google sign in failed", e);
                        Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editDisplayName = findViewById(R.id.editDisplayName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogle = findViewById(R.id.btnGoogle);
        textLoginLink = findViewById(R.id.textLoginLink);


        // sign-in with google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignUp.setOnClickListener(view -> {
            validateAndRegisterUser();
        });

        btnGoogle.setOnClickListener(view -> {
            signInWithGoogle();
        });

        textLoginLink.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    // SignUp by Email, password
    private void validateAndRegisterUser() {
        displayName = editDisplayName.getText().toString();
        email = editEmail.getText().toString();
        password = editPassword.getText().toString();

        if (displayName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên hiển thị", Toast.LENGTH_SHORT).show();
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập password", Toast.LENGTH_SHORT).show();
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        updateUserInfo(firebaseUser, this.displayName, this.email, false);
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo(FirebaseUser firebaseUser, String displayName, String email, boolean isFromSocialLogin) {
        String uid = firebaseUser.getUid();

        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", uid);
        userInfo.put("email", email);
        userInfo.put("displayName", displayName);
        userInfo.put("profileImage", "");
        userInfo.put("userType", "user");
        userInfo.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(uid).set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    if (isFromSocialLogin) {
                        goToMainActivity();
                    } else {
                        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finishAffinity();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Lưu thông tin thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // SignIn by Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        // Khởi chạy màn hình đăng nhập của Google
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập Firebase thành công
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Kiểm tra xem đây là người dùng mới hay người dùng cũ
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            // Nếu là người dùng mới, lưu thông tin vào Firestore
                            Toast.makeText(this, "Đăng ký bằng Google thành công!", Toast.LENGTH_SHORT).show();
                            String googleDisplayName = user.getDisplayName();
                            String googleEmail = user.getEmail();
                            updateUserInfo(user, googleDisplayName, googleEmail, true);
                        } else {
                            // Nếu là người dùng cũ, chỉ cần chuyển màn hình
                            Toast.makeText(this, "Đăng nhập bằng Google thành công!", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        }
                    } else {
                        // Đăng nhập Firebase thất bại
                        Toast.makeText(this, "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
        // Xóa các activity trước đó khỏi back stack để người dùng không thể quay lại
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}