package com.example.truyenchu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.activity.ChiTietTruyenActivity;
import com.example.truyenchu.activity.TimKiemActivity;
import com.example.truyenchu.adapter.TruyenTrendingAdapter;
import com.example.truyenchu.model.TheLoai;
import com.example.truyenchu.model.Truyen;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Đổi tên class thành CategoryFragment
public class CategoryFragment extends Fragment {

    private static final String TAG = "CategoryFragment"; // Đổi TAG cho dễ debug

    private RecyclerView rvTruyen;
    private TruyenTrendingAdapter truyenTrendingAdapter;
    private List<Truyen> displayedTruyenList = new ArrayList<>();
    private List<Truyen> allTruyenList = new ArrayList<>();
    private EditText etTimKiem;
    private DatabaseReference database;
    private TabLayout tabLayout;
    private TextView tvListTitle;
    private View view; // View gốc của Fragment

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tham chiếu đến layout đã được đổi tên: fragment_category.xml
        view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance().getReference();

        // Dùng view để tìm ID
        etTimKiem = view.findViewById(R.id.et_tim_kiem);
        tabLayout = view.findViewById(R.id.tab_layout_the_loai);
        tvListTitle = view.findViewById(R.id.tv_list_title);

        etTimKiem.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TimKiemActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
        loadAllTruyenData();
        setupTabs();
    }

    private void setupRecyclerView() {
        rvTruyen = view.findViewById(R.id.rv_truyen);
        rvTruyen.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTruyen.setNestedScrollingEnabled(false);
        truyenTrendingAdapter = new TruyenTrendingAdapter(getContext(), displayedTruyenList, truyen -> {
            Intent intent = new Intent(getActivity(), ChiTietTruyenActivity.class);
            intent.putExtra("TRUYEN_ID", truyen.getId());
            startActivity(intent);
        });
        rvTruyen.setAdapter(truyenTrendingAdapter);
    }

    // Các hàm setupTabs, loadAllTruyenData, filterAndDisplayTruyen giữ nguyên logic
    private void setupTabs() {
        database.child("the_loai").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        TheLoai theLoai = dataSnapshot.getValue(TheLoai.class);
                        if (theLoai != null) {
                            tabLayout.addTab(tabLayout.newTab().setText(theLoai.getTen()));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải thể loại: " + error.getMessage());
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    filterAndDisplayTruyen(tab.getText().toString());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllTruyenData() {
        database.child("truyen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    allTruyenList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Truyen truyen = snapshot.getValue(Truyen.class);
                        if (truyen != null) {
                            truyen.setId(snapshot.getKey());
                            allTruyenList.add(truyen);
                        }
                    }
                    filterAndDisplayTruyen("Tất cả");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải truyện: " + error.getMessage());
            }
        });
    }

    private void filterAndDisplayTruyen(String category) {
        displayedTruyenList.clear();
        List<Truyen> filteredList = new ArrayList<>();
        if (category.equalsIgnoreCase("Tất cả")) {
            tvListTitle.setText("Top Trending This Week");
            Collections.sort(allTruyenList, (t1, t2) -> Double.compare(t2.getDanhGia(), t1.getDanhGia()));
            filteredList.addAll(allTruyenList);
        } else {
            tvListTitle.setText("Top Trending " + category);
            for (Truyen truyen : allTruyenList) {
                if (truyen.getTheLoaiTags() != null && truyen.getTheLoaiTags().toLowerCase().contains(category.toLowerCase())) {
                    filteredList.add(truyen);
                }
            }
            Collections.sort(filteredList, (t1, t2) -> Double.compare(t2.getDanhGia(), t1.getDanhGia()));
        }

        int limit = Math.min(filteredList.size(), 5);
        for (int i = 0; i < limit; i++) {
            displayedTruyenList.add(filteredList.get(i));
        }

        truyenTrendingAdapter.notifyDataSetChanged();
    }
}
