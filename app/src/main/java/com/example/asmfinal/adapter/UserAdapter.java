package com.example.asmfinal.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asmfinal.R;
import com.example.asmfinal.model.User;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị danh sách người dùng
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;

    /**
     * Constructor của UserAdapter
     * @param userList Danh sách người dùng cần hiển thị
     */
    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho mỗi item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Lấy user tại vị trí position
        User user = userList.get(position);

        // Hiển thị thông tin user
        holder.textViewUsername.setText("Username: " + user.getUsername());
        holder.textViewFullName.setText("Họ tên: " + user.getFullName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder giữ các view của mỗi item trong RecyclerView
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername, textViewFullName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewFullName = itemView.findViewById(R.id.textViewFullName);
        }
    }
}
