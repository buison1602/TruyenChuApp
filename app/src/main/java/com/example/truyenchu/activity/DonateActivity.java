package com.example.truyenchu.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.truyenchu.R;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;

public class DonateActivity extends AppCompatActivity {

    private ImageView qrCodeIv;
    private ImageView backButton;
    private MaterialButton btnDownloadQR; // Nút mới

    private final String qrCodeUrl = "https://firebasestorage.googleapis.com/v0/b/truyenchu-2f4fe.firebasestorage.app/o/avatar_users%2Fzzz.jpg?alt=media&token=639d442d-e1b3-4616-8818-3ae40a1d2bb4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        // Ánh xạ các views
        qrCodeIv = findViewById(R.id.qr_code_iv);
        backButton = findViewById(R.id.backToSignin);
        btnDownloadQR = findViewById(R.id.btnDownloadQR); // Ánh xạ nút mới

        // Gán sự kiện cho các nút
        backButton.setOnClickListener(v -> {
            Toast.makeText(this, "Cảm ơn bạn đã donate cho admin", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnDownloadQR.setOnClickListener(v -> downloadImageToGallery());

        // Tải ảnh từ URL vào ImageView
        Glide.with(this)
                .load(qrCodeUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_error_placeholder)
                .into(qrCodeIv);
    }

    private void downloadImageToGallery() {
        Toast.makeText(this, "Đang tải mã QR...", Toast.LENGTH_SHORT).show();

        // Dùng Glide để lấy ảnh về dưới dạng Bitmap
        Glide.with(this)
                .asBitmap()
                .load(qrCodeUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Khi đã có Bitmap, tiến hành lưu vào thư viện
                        saveBitmapToGallery(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Không cần xử lý
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Toast.makeText(DonateActivity.this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        // Lấy ContentResolver để tương tác với MediaStore
        ContentResolver resolver = getContentResolver();
        // Tạo các thông tin cho ảnh mới
        ContentValues contentValues = new ContentValues();
        String fileName = "QR_Donate_" + System.currentTimeMillis() + ".jpg";
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        // Chỉ định thư mục lưu là Pictures cho Android 10 (Q) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }

        Uri imageUri = null;
        OutputStream outputStream = null;

        try {
            // Chèn một record mới vào MediaStore và lấy về Uri của nó
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imageUri == null) {
                throw new IOException("Failed to create new MediaStore record.");
            }
            // Mở một luồng ghi vào Uri đó
            outputStream = resolver.openOutputStream(imageUri);
            // Nén và ghi bitmap vào luồng
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Toast.makeText(this, "Đã lưu mã QR vào thư viện ảnh!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // Nếu có lỗi, xóa record đã tạo (nếu có)
            if (imageUri != null) {
                resolver.delete(imageUri, null, null);
            }
            Toast.makeText(this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}