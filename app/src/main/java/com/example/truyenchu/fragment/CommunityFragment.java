package com.example.truyenchu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.activity.ChiTietTruyenActivity;
import com.example.truyenchu.activity.TaoBaiDangActivity;
import com.example.truyenchu.adapter.BaiDangAdapter;
import com.example.truyenchu.model.BaiDang;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvBaiDang;
    private FloatingActionButton fabTaoBaiDang;
    private BaiDangAdapter adapter;
    private List<BaiDang> baiDangList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo nó đang sử dụng đúng file layout đã được đổi tên
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBaiDang = view.findViewById(R.id.rv_bai_dang);
        fabTaoBaiDang = view.findViewById(R.id.fab_tao_bai_dang);

        setupRecyclerView();
        loadBaiDang();

        fabTaoBaiDang.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TaoBaiDangActivity.class));
        });
    }

    private void setupRecyclerView() {
        rvBaiDang.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter với listener để xử lý sự kiện click vào truyện được đính kèm
        adapter = new BaiDangAdapter(getContext(), baiDangList, storyId -> {
            Intent intent = new Intent(getActivity(), ChiTietTruyenActivity.class);
            intent.putExtra("TRUYEN_ID", storyId);
            startActivity(intent);
        });
        rvBaiDang.setAdapter(adapter);
    }

    private void loadBaiDang() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("bai_dang");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                baiDangList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BaiDang baiDang = dataSnapshot.getValue(BaiDang.class);
                    if (baiDang != null) {
                        baiDangList.add(baiDang);
                    }
                }
                // Sắp xếp để bài mới nhất lên đầu
                Collections.sort(baiDangList, (post1, post2) -> Long.compare(post2.getTimestamp(), post1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
}
