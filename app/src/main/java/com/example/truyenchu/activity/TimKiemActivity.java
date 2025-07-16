package com.example.truyenchu.activity;

import android.content.Intent; // Thêm import cho Intent
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.adapter.TruyenTimKiemAdapter;
import com.example.truyenchu.model.Truyen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimKiemActivity extends AppCompatActivity {

    private static final String TAG = "TimKiemActivity";

    private EditText etTimKiem;
    private RecyclerView rvKetQua;
    private TruyenTimKiemAdapter adapter;
    private TextView tvThongBao;

    // Danh sách để hiển thị kết quả đã lọc
    private List<Truyen> filteredTruyenList;
    // Danh sách chứa TẤT CẢ truyện để lọc
    private List<Truyen> allTruyenList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem);

        // Ánh xạ các view từ layout
        etTimKiem = findViewById(R.id.et_tim_kiem_activity);
        rvKetQua = findViewById(R.id.rv_ket_qua_tim_kiem);
        tvThongBao = findViewById(R.id.tv_thong_bao_khong_tim_thay);

        // Khởi tạo RecyclerView
        rvKetQua.setLayoutManager(new LinearLayoutManager(this));
        filteredTruyenList = new ArrayList<>();
        allTruyenList = new ArrayList<>();

        // **THAY ĐỔI CỐT LÕI NẰM Ở ĐÂY**
        // Khởi tạo adapter và truyền vào một OnItemClickListener
        adapter = new TruyenTimKiemAdapter(this, filteredTruyenList, truyen -> {
            // Logic để mở trang chi tiết khi một truyện được click
            Intent intent = new Intent(TimKiemActivity.this, ChiTietTruyenActivity.class);
            // Gửi ID của truyện được chọn qua Intent
            intent.putExtra("TRUYEN_ID", truyen.getId());
            startActivity(intent);
        });

        rvKetQua.setAdapter(adapter);

        // Tải toàn bộ dữ liệu truyện về ở chế độ nền
        fetchAllTruyen();

        // Lắng nghe sự kiện người dùng gõ phím
        etTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!allTruyenList.isEmpty()) {
                    filterTruyen(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Tải toàn bộ danh sách truyện từ Firebase về và lưu vào allTruyenList.
     */
    private void fetchAllTruyen() {
        Log.d(TAG, "Bắt đầu tải dữ liệu tìm kiếm...");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("truyen");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTruyenList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Truyen truyen = dataSnapshot.getValue(Truyen.class);
                        if (truyen != null) {
                            truyen.setId(dataSnapshot.getKey());
                            allTruyenList.add(truyen);
                        }
                    }
                    Log.d(TAG, "Đã tải xong " + allTruyenList.size() + " truyện. Sẵn sàng tìm kiếm.");
                } else {
                    Log.w(TAG, "Không có dữ liệu truyện nào trên Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải dữ liệu tìm kiếm: " + error.getMessage());
            }
        });
    }

    /**
     * Lọc danh sách truyện dựa trên từ khóa người dùng nhập.
     * @param keyword Từ khóa tìm kiếm.
     */
    private void filterTruyen(String keyword) {
        filteredTruyenList.clear();

        if (keyword.isEmpty()) {
            tvThongBao.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();

        for (Truyen truyen : allTruyenList) {
            // Kiểm tra xem từ khóa có trong TÊN TRUYỆN hoặc TÊN TÁC GIẢ không.
            boolean tenTruyenKhop = truyen.getTen() != null && truyen.getTen().toLowerCase().contains(lowerCaseKeyword);
            boolean tacGiaKhop = truyen.getTacGia() != null && truyen.getTacGia().toLowerCase().contains(lowerCaseKeyword);

            if (tenTruyenKhop || tacGiaKhop) {
                // Nếu một trong hai điều kiện đúng, thêm truyện vào danh sách kết quả
                filteredTruyenList.add(truyen);
            }
        }

        // Cập nhật giao diện dựa trên kết quả lọc
        if (filteredTruyenList.isEmpty()) {
            tvThongBao.setVisibility(View.VISIBLE);
        } else {
            tvThongBao.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
}
