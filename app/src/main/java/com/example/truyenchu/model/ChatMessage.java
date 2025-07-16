package com.example.truyenchu.model;

public class ChatMessage {
    // Các loại tin nhắn
    public static final int TYPE_SENT_TEXT = 0;
    public static final int TYPE_RECEIVED_TEXT = 1;
    public static final int TYPE_RECEIVED_STORY = 2; // Tin nhắn chứa kết quả truyện
    public static final int TYPE_LOADING = 3;

    private String textMessage;
    private Truyen story; // Chứa object truyện nếu có
    private int viewType;

    // Constructor cho tin nhắn văn bản
    public ChatMessage(String textMessage, int viewType) {
        this.textMessage = textMessage;
        this.viewType = viewType;
    }

    // Constructor cho tin nhắn chứa truyện
    public ChatMessage(Truyen story) {
        this.story = story;
        this.viewType = TYPE_RECEIVED_STORY;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public Truyen getStory() {
        return story;
    }

    public int getViewType() {
        return viewType;
    }
}
