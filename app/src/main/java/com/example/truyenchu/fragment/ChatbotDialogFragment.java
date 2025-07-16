// fragment/ChatbotDialogFragment.java
package com.example.truyenchu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.truyenchu.R;
import com.example.truyenchu.adapter.ChatMessageAdapter;
import com.example.truyenchu.helpers.GeminiHelper;
import com.example.truyenchu.model.ChatMessage;
import com.example.truyenchu.model.Truyen;
import java.util.ArrayList;
import java.util.List;

public class ChatbotDialogFragment extends DialogFragment implements GeminiHelper.GeminiHelperCallback {

    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private ImageButton btnSendChat;
    private ChatMessageAdapter adapter;
    private final List<ChatMessage> chatMessageList = new ArrayList<>();
    private GeminiHelper geminiHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Khởi tạo GeminiHelper
        geminiHelper = new GeminiHelper(this);

        // Ánh xạ View
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etChatMessage = view.findViewById(R.id.et_chat_message);
        btnSendChat = view.findViewById(R.id.btn_send_chat);

        // Thiết lập RecyclerView
        adapter = new ChatMessageAdapter(getContext(), chatMessageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatMessages.setAdapter(adapter);

        // Tin nhắn chào mừng từ bot
        addBotMessage(new ChatMessage("Xin chào! Tôi có thể giúp bạn tìm truyện. Hãy thử hỏi tôi nhé!", ChatMessage.TYPE_RECEIVED_TEXT));

        // Xử lý sự kiện click nút gửi
        btnSendChat.setOnClickListener(v -> handleSendMessage());
    }

    private void handleSendMessage() {
        String message = etChatMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            addUserMessage(new ChatMessage(message, ChatMessage.TYPE_SENT_TEXT));
            etChatMessage.setText("");
            addLoadingMessage(); // Hiển thị icon loading
            // ---- SỬA LỖI Ở ĐÂY ----
            // Truyền thêm lịch sử chat (chatMessageList) để AI có ngữ cảnh
            geminiHelper.getResponse(message, chatMessageList);
        }
    }

    // Các hàm callback từ GeminiHelper
    @Override
    public void onTextResponse(String response) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                removeLoadingMessage();
                addBotMessage(new ChatMessage(response, ChatMessage.TYPE_RECEIVED_TEXT));
            });
        }
    }

    @Override
    public void onStoryResponse(Truyen truyen) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                removeLoadingMessage();
                addBotMessage(new ChatMessage(truyen));
            });
        }
    }

    @Override
    public void onMultipleStoriesResponse(List<Truyen> truyenList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                removeLoadingMessage();
                for (Truyen truyen : truyenList) {
                    addBotMessage(new ChatMessage(truyen));
                }
            });
        }
    }

    @Override
    public void onError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                removeLoadingMessage();
                addBotMessage(new ChatMessage("Đã có lỗi xảy ra: " + error, ChatMessage.TYPE_RECEIVED_TEXT));
            });
        }
    }

    // Các hàm tiện ích để thêm/xóa tin nhắn khỏi danh sách
    private void addUserMessage(ChatMessage message) {
        chatMessageList.add(message);
        adapter.notifyItemInserted(chatMessageList.size() - 1);
        rvChatMessages.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addBotMessage(ChatMessage message) {
        chatMessageList.add(message);
        adapter.notifyItemInserted(chatMessageList.size() - 1);
        rvChatMessages.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addLoadingMessage() {
        chatMessageList.add(new ChatMessage(null, ChatMessage.TYPE_LOADING));
        adapter.notifyItemInserted(chatMessageList.size() - 1);
        rvChatMessages.scrollToPosition(chatMessageList.size() - 1);
    }

    private void removeLoadingMessage() {
        if (!chatMessageList.isEmpty() && chatMessageList.get(chatMessageList.size() - 1).getViewType() == ChatMessage.TYPE_LOADING) {
            chatMessageList.remove(chatMessageList.size() - 1);
            adapter.notifyItemRemoved(chatMessageList.size());
        }
    }
}