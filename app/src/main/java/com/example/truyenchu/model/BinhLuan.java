package com.example.truyenchu.model;

public class BinhLuan {
    private String id;
    private String tenNguoiDung;
    private String avatarUrl;
    private String noiDung;
    private String thoiGian;
    private int soLike;

    public BinhLuan() {
        // Firebase cần constructor rỗng
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenNguoiDung() { return tenNguoiDung; }
    public void setTenNguoiDung(String tenNguoiDung) { this.tenNguoiDung = tenNguoiDung; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }
    public int getSoLike() { return soLike; }
    public void setSoLike(int soLike) { this.soLike = soLike; }
}