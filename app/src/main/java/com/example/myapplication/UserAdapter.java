package com.example.myapplication;

// UserAdapter.java
import static android.provider.Settings.System.getString;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(user.getName());
        holder.userAgeTextView.setText(user.getAge());

        holder.itemView.setOnClickListener(v -> {
            // ユーザーIDを取得
            String userId = user.getId();
            Log.d("UserAdapter", "userId: " + userId);

            // Intentを使用して遷移先のアクティビティにユーザーIDを渡す
            Intent intent = new Intent(holder.itemView.getContext(), UserDetailActivity.class);
            intent.putExtra("userId", userId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView userAgeTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userAgeTextView = itemView.findViewById(R.id.userAgeTextView);
        }
    }
}
