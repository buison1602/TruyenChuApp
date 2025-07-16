package com.example.truyenchu;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.truyenchu.R;
import com.example.truyenchu.fragment.CategoryFragment;
import com.example.truyenchu.fragment.CommunityFragment; // Đảm bảo import đúng tên mới
import com.example.truyenchu.fragment.HomeFragment;
import com.example.truyenchu.fragment.LibraryFragment;
import com.example.truyenchu.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            loadFragment(new HomeFragment());
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_categories) {
                    selectedFragment = new CategoryFragment();
                } else if (itemId == R.id.nav_community) {
                    // THAY ĐỔI CỐT LÕI: Gọi đến CommunityFragment
                    selectedFragment = new CommunityFragment();
                } else if (itemId == R.id.nav_library) {
                    // THAY ĐỔI CỐT LÕI: Gọi đến CommunityFragment
                    selectedFragment = new LibraryFragment();
                } else if (itemId == R.id.nav_profile) {
                    // THAY ĐỔI CỐT LÕI: Gọi đến CommunityFragment
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            };

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}