package com.example.truyenchu.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.truyenchu.model.DownloadedChuong;

@Database(entities = {DownloadedChuong.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract ChuongDao chuongDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), // Sử dụng getApplicationContext() từ Context
                            AppDatabase.class, "app-database")
                    .allowMainThreadQueries() // Chỉ dùng tạm thời, nên dùng thread riêng trong thực tế
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}