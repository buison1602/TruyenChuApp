package com.example.truyenchu.model;

public class Chuong {
    private String id;
    private String ten;
    private String noiDung;
    private String ngayDang;
    private String truyenId;

    public String getTruyenId() {
        return truyenId;
    }

    public void setTruyenId(String truyenId) {
        this.truyenId = truyenId;
    }

    public Chuong() {
        // Firebase cần constructor rỗng
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getNgayDang() { return ngayDang; }
    public void setNgayDang(String ngayDang) { this.ngayDang = ngayDang; }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNoiDung() {
        return noiDung;
    }
}