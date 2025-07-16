package com.example.truyenchu.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.truyenchu.R;
import com.example.truyenchu.activity.ChiTietTruyenActivity;
import com.example.truyenchu.model.ChatMessage;
import com.example.truyenchu.model.Truyen;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final Context context;

    public ChatMessageAdapter(Context context, List<ChatMessage> chatMessageList) {
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessageList.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ChatMessage.TYPE_SENT_TEXT:
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
                return new SentMessageHolder(view);
            case ChatMessage.TYPE_RECEIVED_STORY:
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_story_result, parent, false);
                return new StoryResultHolder(view);
            case ChatMessage.TYPE_LOADING:
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_loading, parent, false);
                return new LoadingMessageHolder(view);
            default: // TYPE_RECEIVED_TEXT
                view = LayoutInflater.from(context).inflate(R.layout.item_chat_received, parent, false);
                return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        ChatMessage message = chatMessageList.get(position);
        switch (viewType) {
            case ChatMessage.TYPE_SENT_TEXT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case ChatMessage.TYPE_RECEIVED_TEXT:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case ChatMessage.TYPE_RECEIVED_STORY:
                ((StoryResultHolder) holder).bind(message.getStory());
                break;
        }
    }

    @Override
    public int getItemCount() { return chatMessageList.size(); }

    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body_sent);
        }
        void bind(ChatMessage message) { messageText.setText(message.getTextMessage()); }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body_received);
        }
        void bind(ChatMessage message) { messageText.setText(message.getTextMessage()); }
    }

    private static class LoadingMessageHolder extends RecyclerView.ViewHolder {
        LoadingMessageHolder(View itemView) { super(itemView); }
    }

    private class StoryResultHolder extends RecyclerView.ViewHolder {
        ImageView ivAnhBia;
        TextView tvTenTruyen, tvTacGia;
        StoryResultHolder(View itemView) {
            super(itemView);
            ivAnhBia = itemView.findViewById(R.id.iv_anh_bia_chat);
            tvTenTruyen = itemView.findViewById(R.id.tv_ten_truyen_chat);
            tvTacGia = itemView.findViewById(R.id.tv_tac_gia_chat);
        }
        void bind(Truyen truyen) {
            if (truyen == null) return;
            tvTenTruyen.setText(truyen.getTen());
            tvTacGia.setText("Tác giả: " + truyen.getTacGia());
            Glide.with(context).load(truyen.getAnhBia()).into(ivAnhBia);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChiTietTruyenActivity.class);
                intent.putExtra("TRUYEN_ID", truyen.getId());
                context.startActivity(intent);
            });
        }
    }
}
