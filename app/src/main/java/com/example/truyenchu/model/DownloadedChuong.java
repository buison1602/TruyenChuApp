package com.example.truyenchu.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloaded_chapters")
public class DownloadedChuong {
    @PrimaryKey
    @NonNull
    public String id;

    public String truyenId; // ID của truyện
    public String chuongId; // ID của chương
    public String noiDung;  // Nội dung chương
    public String ngayDang; // Ngày đăng

    public DownloadedChuong() {
    }

    public DownloadedChuong(@NonNull String truyenId, @NonNull String chuongId) {
        this.id = truyenId + "_" + chuongId; // Tạo khóa duy nhất
        this.truyenId = truyenId;
        this.chuongId = chuongId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTruyenId() {
        return truyenId;
    }

    public String getChuongId() {
        return chuongId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getNgayDang() {
        return ngayDang;
    }

    public void setTruyenId(String truyenId) {
        this.truyenId = truyenId;
    }

    public void setChuongId(String chuongId) {
        this.chuongId = chuongId;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void setNgayDang(String ngayDang) {
        this.ngayDang = ngayDang;
    }
}