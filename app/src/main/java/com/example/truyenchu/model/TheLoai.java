package com.example.truyenchu.model;

public class TheLoai {
    private String id;
    private String ten;

    public TheLoai() {
        // Firebase cần constructor rỗng
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
}
