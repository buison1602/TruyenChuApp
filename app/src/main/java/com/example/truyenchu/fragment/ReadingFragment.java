package com.example.truyenchu.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.truyenchu.R;
import com.example.truyenchu.adapter.ListChapterAdapter;
import com.example.truyenchu.database.AppDatabase;
import com.example.truyenchu.database.ChuongDao;
import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.model.DownloadedChuong;
import com.example.truyenchu.model.Truyen;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadingFragment extends Fragment {

    private static final String TAG = "ReadingFragment";
    public static final String KEY_STORY_ID = "storyId";
    public static final String KEY_CHUONG_ID = "chuongId";

    private TextView tvStoryContent;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private String storyId;
    private String chuongId;
    private RecyclerView rvChapters;
    private ListChapterAdapter chapterAdapter;
    private final List<Chuong> chapterList = new ArrayList<>();
    private Truyen currentTruyen;

    private AppDatabase db;
    private ChuongDao chuongDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            storyId = getArguments().getString(KEY_STORY_ID);
            chuongId = getArguments().getString(KEY_CHUONG_ID);
            Log.d(TAG, "Received storyId: " + storyId + ", chuongId: " + chuongId);
        } else {
            Log.e(TAG, "No arguments received!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);
        tvStoryContent = view.findViewById(R.id.tv_noi_dung); // Ánh xạ TextView

        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "app-database")
                .allowMainThreadQueries() // Chỉ dùng tạm thời
                .fallbackToDestructiveMigration()
                .build();
        chuongDao = db.chuongDao();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawerLayout = view.findViewById(R.id.drawer_layout);
        tvStoryContent = view.findViewById(R.id.tv_story_content);
        toolbar = view.findViewById(R.id.toolbar);
        rvChapters = view.findViewById(R.id.rv_chapters);

        setupRecyclerView();
        setupEventListeners(view);

        if (storyId != null) {
            loadStoryInfo(storyId);
            loadChapterList(storyId);
        } else {
            Log.e(TAG, "storyId is null, cannot load data!");
            tvStoryContent.setText("Lỗi: Không tìm thấy truyện!");
        }
        // Ưu tiên tải nội dung từ chuongId, nếu không có thì tải chương đầu tiên sau khi danh sách sẵn sàng
        if (chuongId != null) {
            loadChapterContentById(chuongId);
        } else if (storyId != null) {
            // Chờ danh sách chương tải xong, sau đó tải chương đầu tiên
            loadFirstChapterIfAvailable();
        } else {
            Log.e(TAG, "chuongId and storyId are null, cannot load content!");
            tvStoryContent.setText("Lỗi: Không đủ dữ liệu để tải nội dung!");
        }
    }

    private void setupRecyclerView() {
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        chapterAdapter = new ListChapterAdapter(chapterList, new ListChapterAdapter.OnChapterClickListener() {
            @Override
            public void onChapterClick(Chuong chapter) {
                loadChapterContent(chapter);
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        rvChapters.setAdapter(chapterAdapter);
    }

    private void setupEventListeners(View view) {
        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            BottomNavigationView bottomNavigation = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_report_error) {
                new ReportErrorDialogFragment().show(getParentFragmentManager(), "ReportErrorDialog");
                return true;
            }
            return false;
        });
        view.findViewById(R.id.btn_chapters).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        view.findViewById(R.id.btn_settings).setOnClickListener(v -> {
            new SettingsBottomSheetFragment().show(getParentFragmentManager(), "SettingsBottomSheet");
        });

        view.findViewById(R.id.fab_tts).setOnClickListener(v -> {
            String currentText = tvStoryContent.getText().toString();
            if (currentText != null && !currentText.isEmpty()) {
                TtsBottomSheetFragment ttsBottomSheet = TtsBottomSheetFragment.newInstance(currentText);
                ttsBottomSheet.show(getParentFragmentManager(), ttsBottomSheet.getTag());
            } else {
                Toast.makeText(getContext(), "Không có nội dung để đọc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStoryInfo(String storyId) {
        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("truyen").child(storyId);
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentTruyen = snapshot.getValue(Truyen.class);
                if (currentTruyen != null) {
                    toolbar.setSubtitle(currentTruyen.getTen());
                } else {
                    Log.e(TAG, "Failed to load story info for storyId: " + storyId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load story info.", error.toException());
            }
        });
    }

    private void loadChapterList(String storyId) {
        DatabaseReference chaptersRef = FirebaseDatabase.getInstance()
                .getReference("chuong")
                .child(storyId);

        chaptersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chapterList.clear();
                for (DataSnapshot chapterSnapshot : dataSnapshot.getChildren()) {
                    Chuong chuong = chapterSnapshot.getValue(Chuong.class);
                    if (chuong != null) {
                        chuong.setId(chapterSnapshot.getKey());
                        chapterList.add(chuong);
                    }
                }
                Collections.sort(chapterList, (c1, c2) -> {
                    int num1 = extractChapterNumber(c1.getTen());
                    int num2 = extractChapterNumber(c2.getTen());
                    return Integer.compare(num1, num2);
                });
                chapterAdapter.notifyDataSetChanged();
                // Sau khi danh sách sẵn sàng, kiểm tra và tải chương đầu tiên nếu cần
                loadFirstChapterIfAvailable();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load chapter list.", databaseError.toException());
            }
        });
    }

    private int extractChapterNumber(String chapterName) {
        try {
            String[] parts = chapterName.split(" ");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].replace(":", ""));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing chapter number from: " + chapterName, e);
        }
        return 0;
    }

    private void loadChapterContentById(String chuongId) {
        if (chuongId == null || storyId == null) {
            Log.e(TAG, "chuongId or storyId is null: storyId=" + storyId + ", chuongId=" + chuongId);
            return;
        }

        new Thread(() -> {
            String downloadedId = storyId + "_" + chuongId;
            DownloadedChuong downloadedChuong = chuongDao.getChuong(downloadedId);
            if (downloadedChuong != null) {
                requireActivity().runOnUiThread(() -> {
                    tvStoryContent.setText(downloadedChuong.noiDung.replace("\\n", "\n"));
                    toolbar.setTitle(getChapterTitle(chuongId));
                    scrollToTop();
                });
            } else {
                DatabaseReference chuongRef = FirebaseDatabase.getInstance().getReference()
                        .child("chuong").child(storyId).child(chuongId);
                chuongRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String noiDung = snapshot.child("noiDung").getValue(String.class);
                        if (noiDung != null) {
                            requireActivity().runOnUiThread(() -> {
                                tvStoryContent.setText(noiDung.replace("\\n", "\n"));
                                toolbar.setTitle(getChapterTitle(chuongId));
                                scrollToTop();
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                tvStoryContent.setText("Không thể tải nội dung! (Dữ liệu null)");
                                Log.e(TAG, "Nội dung null cho chapter: " + storyId + " --- " + chuongId);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        requireActivity().runOnUiThread(() -> tvStoryContent.setText("Không thể tải nội dung."));
                    }
                });
            }
        }).start();
    }

    private String getChapterTitle(String chuongId) {
        for (Chuong chuong : chapterList) {
            if (chuong.getId().equals(chuongId)) {
                return chuong.getTen();
            }
        }
        return "Chương " + chuongId; // Giá trị mặc định
    }

    private void loadChapterContent(Chuong chapter) {
        if (chapter == null) return;

        new Thread(() -> {
            String downloadedId = storyId + "_" + chapter.getId();
            DownloadedChuong downloadedChuong = chuongDao.getChuong(downloadedId);
            if (downloadedChuong != null) {
                requireActivity().runOnUiThread(() -> {
                    tvStoryContent.setText(downloadedChuong.noiDung.replace("\\n", "\n"));
                    toolbar.setTitle(chapter.getTen());
                    scrollToTop();
                });
            } else {
                DatabaseReference chuongRef = FirebaseDatabase.getInstance().getReference()
                        .child("chuong").child(storyId).child(chapter.getId());
                chuongRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String noiDung = snapshot.child("noiDung").getValue(String.class);
                        if (noiDung != null) {
                            tvStoryContent.setText(noiDung.replace("\\n", "\n"));
                            toolbar.setTitle(chapter.getTen());
                            scrollToTop();
                        } else {
                            tvStoryContent.setText("Không thể tải nội dung! (Dữ liệu null)");
                            Log.e(TAG, "Nội dung null cho chapter: " + storyId + " --- " + chapter.getId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        requireActivity().runOnUiThread(() -> tvStoryContent.setText("Không thể tải nội dung."));
                    }
                });
            }
        }).start();
    }

    private void loadFirstChapterIfAvailable() {
        if (chuongId == null && !chapterList.isEmpty()) {
            Chuong firstChapter = chapterList.get(0);
            if (firstChapter != null) {
                chuongId = firstChapter.getId(); // Gán tạm chuongId để sử dụng
                loadChapterContent(firstChapter);
                Log.d(TAG, "Loaded first chapter: " + firstChapter.getId());
            }
        }
    }

    private void scrollToTop() {
        View nestedScrollView = getView().findViewById(R.id.nested_scroll_view_reading);
        if (nestedScrollView != null) {
            nestedScrollView.scrollTo(0, 0);
        }
    }
}