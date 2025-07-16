package com.example.truyenchu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.truyenchu.model.Chuong;
import com.example.truyenchu.R;
import java.util.List;

public class ListChapterAdapter extends RecyclerView.Adapter<ListChapterAdapter.ViewHolder> {
    private List<Chuong> chapterList;
    private final OnChapterClickListener listener; // Dòng thêm mới: Biến để lưu trữ listener

    /**
     * INTERFACE THÊM MỚI: Đây chính là phần bị thiếu.
     * Nó định nghĩa một "khuôn mẫu" cho hành động click.
     * ReadingFragment sẽ cung cấp phần thân cho phương thức onChapterClick này.
     */
    public interface OnChapterClickListener {
        void onChapterClick(Chuong chapter);
    }
    // Thêm listener để xử lý khi người dùng nhấn vào một chương
    public ListChapterAdapter(List<Chuong> chapterList, OnChapterClickListener listener) {
        this.chapterList = chapterList;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Chuong chapter = chapterList.get(position);
//        holder.title.setText(chapter.getTen());
        Chuong chapter = chapterList.get(position);
        holder.bind(chapter, listener);
    }
    @Override public int getItemCount() { return chapterList.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_chapter_item_title);
        }

        /**
         * HÀM BIND MỚI:
         * Hàm này nhận dữ liệu và gán sự kiện click cho mỗi item.
         * @param chapter Đối tượng chương để hiển thị tên.
         * @param listener Hành động sẽ được thực thi khi click.
         */
        public void bind(final Chuong chapter, final OnChapterClickListener listener) {
            title.setText(chapter.getTen());
            // Gán sự kiện click cho toàn bộ item (itemView)
            itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
        }
    }
}