package com.example.truyenchu.model;

public class BaiDang {
    private String postId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String postContent;
    private String postImage;       // <-- Trường còn thiếu đã được thêm lại

    // Các trường liên quan đến truyện được đính kèm
    private String storyId;
    private String storyName;
    private String storyAuthor;
    private String storyGenreTags;
    private String storyCoverImage;

    private long timestamp;

    public BaiDang() {
        // Firebase cần constructor rỗng
    }

    // Getters and Setters cho tất cả các trường
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public String getStoryName() { return storyName; }
    public void setStoryName(String storyName) { this.storyName = storyName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStoryAuthor() { return storyAuthor; }
    public void setStoryAuthor(String storyAuthor) { this.storyAuthor = storyAuthor; }
    public String getStoryGenreTags() { return storyGenreTags; }
    public void setStoryGenreTags(String storyGenreTags) { this.storyGenreTags = storyGenreTags; }
    public String getStoryCoverImage() { return storyCoverImage; }
    public void setStoryCoverImage(String storyCoverImage) { this.storyCoverImage = storyCoverImage; }

    // Getter và Setter cho trường postImage
    public String getPostImage() { return postImage; }
    public void setPostImage(String postImage) { this.postImage = postImage; }
}
