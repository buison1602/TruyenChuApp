package com.example.truyenchu.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.truyenchu.R;

public class ReportErrorDialogFragment extends DialogFragment { // Sửa ở đây

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_report_error, null); // Sẽ tạo file XML này ở phần B

        RadioGroup radioGroupErrors = view.findViewById(R.id.rg_errors);

        builder.setView(view)
                .setPositiveButton("Báo lỗi", (dialog, id) -> {
                    int selectedId = radioGroupErrors.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        // Xử lý logic báo lỗi ở đây
                    }
                })
                .setNegativeButton("Hủy", (dialog, id) -> {
                    getDialog().cancel();
                });

        return builder.create();
    }
}