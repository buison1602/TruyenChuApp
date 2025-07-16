package com.example.truyenchu.fragment;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.truyenchu.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class TtsBottomSheetFragment extends BottomSheetDialogFragment implements TextToSpeech.OnInitListener {

    private static final String TAG = "TtsBottomSheet";
    private static final String ARG_TEXT_TO_READ = "textToRead";

    private TextToSpeech tts;
    private ImageButton btnPlayPause;
    private SeekBar seekBarSpeed, seekBarPitch;
    private String textToRead;
    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;
    private float currentPitch = 1.0f;

    public static TtsBottomSheetFragment newInstance(String text) {
        TtsBottomSheetFragment fragment = new TtsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT_TO_READ, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            textToRead = getArguments().getString(ARG_TEXT_TO_READ);
        }
        tts = new TextToSpeech(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_tts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnPlayPause = view.findViewById(R.id.btn_tts_play_pause);
        seekBarSpeed = view.findViewById(R.id.seekBarSpeed);
        seekBarPitch = view.findViewById(R.id.seekBarPitch);
        setupEventListeners();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("vi", "VN");
            int result = tts.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getContext(), "Thiết bị không hỗ trợ đọc Tiếng Việt.", Toast.LENGTH_SHORT).show();
            } else {
                tts.setSpeechRate(currentSpeed);
                tts.setPitch(currentPitch);
                setupProgressListener();
            }
        } else {
            Log.e(TAG, "Initialization failed. Status: " + status);
            Toast.makeText(getContext(), "Khởi tạo TextToSpeech thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupProgressListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isPlaying = true;
                        btnPlayPause.setImageResource(R.drawable.ic_pause);
                    });
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (tts.isSpeaking()) return;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isPlaying = false;
                        btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    });
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi khi đọc văn bản", Toast.LENGTH_SHORT).show();
                        isPlaying = false;
                        btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    });
                }
            }
        });
    }

    private void setupEventListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSpeed = 0.5f + (progress / 100f);
                tts.setSpeechRate(currentSpeed);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentPitch = 0.5f + (progress / 100f);
                tts.setPitch(currentPitch);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void togglePlayPause() {
        if (isPlaying) {
            tts.stop();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        } else {
            if (textToRead != null && !textToRead.isEmpty()) {
                speakInChunks(textToRead);
            }
        }
    }

    private void speakInChunks(String longText) {
        // Tách văn bản thành các câu dựa trên dấu chấm, hỏi, than và xuống dòng.
        String[] chunks = longText.split("(?<=[.?!\\n])\\s*");

        if (chunks.length > 0) {
            Bundle params = new Bundle();
            String utteranceIdFirst = "chunk_0";
            tts.speak(chunks[0], TextToSpeech.QUEUE_FLUSH, params, utteranceIdFirst);
        }

        for (int i = 1; i < chunks.length; i++) {
            Bundle params = new Bundle();
            String utteranceIdNext = "chunk_" + i;
            tts.speak(chunks[i], TextToSpeech.QUEUE_ADD, params, utteranceIdNext);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}