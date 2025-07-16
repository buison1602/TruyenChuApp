package com.example.truyenchu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.truyenchu.R; // Thay bằng package của bạn

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho bottom sheet
        return inflater.inflate(R.layout.bottom_sheet_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view từ layout
        SeekBar seekBarFontSize = view.findViewById(R.id.seekbar_font_size);
        Spinner spinnerFontFamily = view.findViewById(R.id.spinner_font_family);
        RadioGroup radioGroupTheme = view.findViewById(R.id.rg_theme);

        // --- Cấu hình cho Spinner Font chữ ---
        // Tạo một danh sách font chữ (ví dụ)
        String[] fontFamilies = new String[]{"Roboto", "Merriweather", "Lora", "Open Sans"};
        // Tạo một ArrayAdapter
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, fontFamilies);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set adapter cho Spinner
        spinnerFontFamily.setAdapter(fontAdapter);

        // --- Thiết lập Listener cho các control ---
        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO: Gọi một interface để thay đổi cỡ chữ trong ReadingFragment
                // Ví dụ: readingFragment.updateFontSize(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String theme = "";
            if (checkedId == R.id.rb_theme_light) {
                theme = "Sáng";
            } else if (checkedId == R.id.rb_theme_sepia) {
                theme = "Vàng";
            } else if (checkedId == R.id.rb_theme_dark) {
                theme = "Tối";
            }
            // TODO: Gọi một interface để thay đổi chủ đề trong ReadingFragment
            Toast.makeText(getContext(), "Đã chọn chủ đề: " + theme, Toast.LENGTH_SHORT).show();
        });
    }
}