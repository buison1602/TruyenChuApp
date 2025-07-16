package com.example.truyenchu.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.truyenchu.MainActivity;
import com.example.truyenchu.R;
import com.example.truyenchu.activity.ChiTietTruyenActivity;
import com.example.truyenchu.activity.SigninActivity;
import com.example.truyenchu.activity.TimKiemActivity;
import com.example.truyenchu.adapter.FeaturedTruyenAdapter;
import com.example.truyenchu.adapter.RecentUpdatesAdapter;
import com.example.truyenchu.adapter.StorySliderAdapter;
import com.example.truyenchu.model.Truyen;
import com.example.truyenchu.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    // Views
    private RecyclerView featuredRecyclerView;
    private RecyclerView recentUpdatesRecyclerView;
    private EditText searchEditText;
    private ViewPager2 viewPager;
    private TabLayout tabLayoutIndicator;
    private FloatingActionButton fabChat;
    private CircleImageView profileImage;
    private ImageView btnNotifications;
    private Button btnLogin;

    // Adapters
    private FeaturedTruyenAdapter featuredAdapter;
    private RecentUpdatesAdapter recentAdapter;
    private StorySliderAdapter sliderAdapter;

    // Data Lists
    private List<Truyen> featuredTruyenList;
    private List<Truyen> recentTruyenList;
    private List<Truyen> sliderTruyenList;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    // Auto-slide handler
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các views
        featuredRecyclerView = view.findViewById(R.id.featured_recycler_view);
        recentUpdatesRecyclerView = view.findViewById(R.id.recent_updates_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        viewPager = view.findViewById(R.id.view_pager_hero_section);
        tabLayoutIndicator = view.findViewById(R.id.tab_layout_indicator);
        fabChat = view.findViewById(R.id.fab_chat);
        profileImage = view.findViewById(R.id.profile_image);
        btnNotifications = view.findViewById(R.id.btn_notifications);
        btnLogin = view.findViewById(R.id.btn_login_home);

        databaseReference = FirebaseDatabase.getInstance().getReference("truyen");

        // Thiết lập các view không phụ thuộc vào trạng thái đăng nhập
        setupSlider();
        setupFeaturedRecyclerView();
        setupRecentUpdatesRecyclerView();
        setupStaticClickListeners(); // Cài đặt các listener tĩnh

        // Tải dữ liệu truyện
        loadFeaturedStories();
        loadRecentUpdates();
    }

    /**
     * Cài đặt các listener tĩnh, không thay đổi theo trạng thái đăng nhập
     */
    private void setupStaticClickListeners() {
        searchEditText.setOnClickListener(v -> startActivity(new Intent(getActivity(), TimKiemActivity.class)));

        fabChat.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                ChatbotDialogFragment chatbotDialog = new ChatbotDialogFragment();
                chatbotDialog.show(getParentFragmentManager(), "ChatbotDialogFragment_Tag");
            }
        });

        btnNotifications.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng thông báo sẽ được cập nhật sau.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * PHƯƠNG THỨC QUAN TRỌNG NHẤT:
     * Kiểm tra trạng thái người dùng và cập nhật toàn bộ UI và listener liên quan.
     */
    private void updateUIBasedOnUserStatus() {
        if (getContext() == null || !isAdded()) return; // Đảm bảo fragment còn tồn tại

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // TRƯỜNG HỢP: ĐÃ ĐĂNG NHẬP
            btnLogin.setVisibility(View.GONE);
            profileImage.setVisibility(View.VISIBLE);

            // Tải ảnh đại diện từ Firestore
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (getContext() == null || !isAdded()) return;
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                                Glide.with(getContext()).load(user.getProfileImage()).into(profileImage);
                            } else {
                                profileImage.setImageResource(R.drawable.ic_avatar_placeholder); // Ảnh mặc định nếu user chưa có ảnh
                            }
                        } else {
                            profileImage.setImageResource(R.drawable.ic_avatar_placeholder);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded())
                            profileImage.setImageResource(R.drawable.ic_avatar_placeholder); // Ảnh mặc định nếu lỗi
                    });

            // Gán sự kiện click cho ảnh đại diện
            profileImage.setOnClickListener(v -> navigateToProfileTab());
            btnLogin.setOnClickListener(null); // Xóa listener của nút đăng nhập

        } else {
            // TRƯỜNG HỢP: CHƯA ĐĂNG NHẬP
            btnLogin.setVisibility(View.VISIBLE);
            profileImage.setVisibility(View.GONE);

            // Gán sự kiện click cho nút đăng nhập
            btnLogin.setOnClickListener(v -> navigateToSignIn());
            profileImage.setOnClickListener(null); // Xóa listener của ảnh đại diện
        }
    }


    private void navigateToProfileTab() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_profile);
            }
        }
    }

    private void navigateToSignIn() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), SigninActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUIBasedOnUserStatus(); // Cập nhật UI mỗi khi quay lại fragment
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    // --------------------------------------------------------------------------------------
    // CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN
    // --------------------------------------------------------------------------------------

    private void setupSlider() {
        sliderTruyenList = new ArrayList<>();
        sliderAdapter = new StorySliderAdapter(getContext(), sliderTruyenList, this::onItemClick);
        viewPager.setAdapter(sliderAdapter);
        new TabLayoutMediator(tabLayoutIndicator, viewPager, (tab, position) -> {}).attach();
        sliderRunnable = () -> {
            if (sliderAdapter != null && sliderAdapter.getItemCount() > 0) {
                int currentItem = viewPager.getCurrentItem();
                viewPager.setCurrentItem((currentItem + 1) % sliderAdapter.getItemCount(), true);
            }
        };
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void setupFeaturedRecyclerView() {
        featuredRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredTruyenList = new ArrayList<>();
        featuredAdapter = new FeaturedTruyenAdapter(getContext(), featuredTruyenList);
        featuredRecyclerView.setAdapter(featuredAdapter);
        featuredAdapter.setOnItemClickListener(this::onItemClick);
    }

    private void setupRecentUpdatesRecyclerView() {
        recentUpdatesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentTruyenList = new ArrayList<>();
        recentAdapter = new RecentUpdatesAdapter(getContext(), recentTruyenList);
        recentUpdatesRecyclerView.setAdapter(recentAdapter);
        recentAdapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(Truyen truyen) {
        if (truyen == null || truyen.getId() == null || getActivity() == null) return;
        Intent intent = new Intent(getActivity(), ChiTietTruyenActivity.class);
        intent.putExtra("TRUYEN_ID", truyen.getId());
        startActivity(intent);
    }

    private void loadFeaturedStories() {
        Query featuredQuery = databaseReference.orderByChild("danhGia").limitToLast(5);
        featuredQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                featuredTruyenList.clear();
                sliderTruyenList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Truyen truyen = dataSnapshot.getValue(Truyen.class);
                    if (truyen != null) {
                        truyen.setId(dataSnapshot.getKey());
                        featuredTruyenList.add(truyen);
                        if (truyen.getBannerImage() != null && !truyen.getBannerImage().isEmpty()) {
                            sliderTruyenList.add(truyen);
                        }
                    }
                }
                Collections.reverse(featuredTruyenList);
                Collections.reverse(sliderTruyenList);

                if (featuredAdapter != null) featuredAdapter.notifyDataSetChanged();
                if (sliderAdapter != null) sliderAdapter.notifyDataSetChanged();

                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadFeaturedStories onCancelled: ", error.toException());
            }
        });
    }

    private void loadRecentUpdates() {
        Query recentQuery = databaseReference.limitToLast(10);
        recentQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                recentTruyenList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Truyen truyen = dataSnapshot.getValue(Truyen.class);
                    if (truyen != null) {
                        truyen.setId(dataSnapshot.getKey());
                        recentTruyenList.add(truyen);
                    }
                }
                Collections.reverse(recentTruyenList);
                if (recentAdapter != null) recentAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadRecentUpdates onCancelled: ", error.toException());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}