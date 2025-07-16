package com.example.truyenchu.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.truyenchu.model.DownloadedChuong;

@Dao
public interface ChuongDao {
    @Insert
    void insertChuong(DownloadedChuong chuong);

    @Query("SELECT * FROM downloaded_chapters WHERE id = :id LIMIT 1")
    DownloadedChuong getChuong(String id);

    @Query("DELETE FROM downloaded_chapters WHERE id = :downloadedId")
    void deleteDownloadedChuong(String downloadedId); // Sửa tên phương thức và tham số
}