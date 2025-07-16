package com.example.truyenchu.model;

public class Truyen {
    private String id;
    private String ten;
    private String tacGia;
    private String anhBia;
    private String bannerImage;
    private String moTa;
    private String theLoaiTags;
    private double danhGia;
    private int soLuongDanhGia;
    private String trangThai;

    public Truyen() {
        // Firebase cần constructor rỗng
    }
    public Truyen(String id, String ten, String tacGia, String anhBia, String trangThai, String moTa) {
        this.id = id;
        this.ten = ten;
        this.tacGia = tacGia;
        this.anhBia = anhBia;
        this.trangThai = trangThai;
        this.moTa = moTa;
        // Gán giá trị mặc định cho các trường khác nếu cần
        this.bannerImage = "";
        this.theLoaiTags = "Tiên hiệp";
        this.danhGia = 4.5;
        this.soLuongDanhGia = 100;
    }

    // Getters và Setters cho tất cả các trường
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getTacGia() { return tacGia; }
    public void setTacGia(String tacGia) { this.tacGia = tacGia; }
    public String getAnhBia() { return anhBia; }
    public void setAnhBia(String anhBia) { this.anhBia = anhBia; }
    public String getBannerImage() { return bannerImage; }
    public void setBannerImage(String bannerImage) { this.bannerImage = bannerImage; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getTheLoaiTags() { return theLoaiTags; }
    public void setTheLoaiTags(String theLoaiTags) { this.theLoaiTags = theLoaiTags; }
    public double getDanhGia() { return danhGia; }
    public void setDanhGia(double danhGia) { this.danhGia = danhGia; }
    public int getSoLuongDanhGia() { return soLuongDanhGia; }
    public void setSoLuongDanhGia(int soLuongDanhGia) { this.soLuongDanhGia = soLuongDanhGia; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
