package com.example.truyenchu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.R;
import com.example.truyenchu.activity.ChiTietTruyenActivity;
import com.example.truyenchu.activity.SigninActivity;
import com.example.truyenchu.adapter.LibraryAdapter;
import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.model.Truyen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private LinearLayout libraryContentLayout;
    private LinearLayout loggedOutLayout;
    private MaterialButton btnLogin;
    private RecyclerView recyclerView;
    private LibraryAdapter adapter;
    private List<Object> itemList;
    private Chip chipFollowed, chipReading, chipDownloaded;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        libraryContentLayout = view.findViewById(R.id.library_content_layout);
        loggedOutLayout = view.findViewById(R.id.logged_out_layout);
        btnLogin = view.findViewById(R.id.btn_login_from_library);
        recyclerView = view.findViewById(R.id.library_recycler_view);
        chipFollowed = view.findViewById(R.id.chip_followed);
        chipReading = view.findViewById(R.id.chip_reading);
        chipDownloaded = view.findViewById(R.id.chip_downloaded);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        itemList = new ArrayList<>();
        adapter = new LibraryAdapter(getContext(), itemList, "followed");
        recyclerView.setAdapter(adapter);

        chipFollowed.setOnClickListener(v -> loadLibraryData(mAuth.getUid(), "followed"));
        chipReading.setOnClickListener(v -> loadLibraryData(mAuth.getUid(), "reading"));
        chipDownloaded.setOnClickListener(v -> loadLibraryData(mAuth.getUid(), "downloaded"));

        // Đăng ký listener
        adapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(Object item) {
        if (item == null) {
            Toast.makeText(getContext(), "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item instanceof Truyen) {
            Truyen truyen = (Truyen) item;
            String truyenId = truyen.getId();
            if (truyenId != null) {
                Intent intent = new Intent(getActivity(), ChiTietTruyenActivity.class);
                intent.putExtra("TRUYEN_ID", truyenId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            }
        } else if (item instanceof Chuong) {
            Chuong chuong = (Chuong) item;
            String truyenId = chuong.getTruyenId();
            String chuongId = chuong.getId();
            if (truyenId != null && chuongId != null) {
                ReadingFragment readingFragment = new ReadingFragment();

                Bundle bundle = new Bundle();
                bundle.putString(ReadingFragment.KEY_STORY_ID, truyenId);
                bundle.putString(ReadingFragment.KEY_CHUONG_ID, chuongId);
                readingFragment.setArguments(bundle);
                // Thay thế trong container của MainActivity
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, readingFragment)
                        .addToBackStack(null)
                        .commit();
                // Ẩn BottomNavigationView
                BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigation != null) {
                    bottomNavigation.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserStatus();
        // Khôi phục BottomNavigationView nếu quay lại
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.VISIBLE);
        }
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loggedOutLayout.setVisibility(View.GONE);
            libraryContentLayout.setVisibility(View.VISIBLE);
            loadLibraryData(currentUser.getUid(), "followed");
        } else {
            loggedOutLayout.setVisibility(View.VISIBLE);
            libraryContentLayout.setVisibility(View.GONE);

            btnLogin.setOnClickListener(v -> {
                Log.d("LibraryFragment123123", "Button login clicked");
                Intent intent123 = new Intent(getContext(), SigninActivity.class);
                startActivity(intent123);
            });
        }
    }

    private void loadLibraryData(String userId, String category) {
        DatabaseReference libraryRef = dbRef.child("user_library").child(userId).child(category);

        libraryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "Tủ sách của bạn trống", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot itemIdSnapshot : snapshot.getChildren()) {
                    String itemId = itemIdSnapshot.getKey();
                    if (itemId != null) {
                        String table = category.equals("downloaded") ? "chuong" : "truyen";
                        dbRef.child(table).child(itemId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot itemSnapshot) {
                                if (category.equals("downloaded")) {
                                    for (DataSnapshot childSnapshot : itemSnapshot.getChildren()) {
                                        Chuong chuong = childSnapshot.getValue(Chuong.class);
                                        if (chuong != null) {
                                            chuong.setId(childSnapshot.getKey()); // Gán id
                                            itemList.add(chuong);
                                        }
                                    }
                                } else {
                                    Truyen truyen = itemSnapshot.getValue(Truyen.class);
                                    if (truyen != null) {
                                        truyen.setId(itemSnapshot.getKey()); // Gán id
                                        itemList.add(truyen);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), "Failed to load item details.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load library.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideBottomNavigation() {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.GONE);
        }
    }

    private void showBottomNavigation() {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setVisibility(View.VISIBLE);
        }
    }
}