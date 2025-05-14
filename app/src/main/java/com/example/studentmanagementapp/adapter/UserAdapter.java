package com.example.studentmanagementapp.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvID.setText(user.getId()); // hiển thị ID (Firebase key)
        holder.tvName.setText(user.getName());
        holder.tvPhone.setText("Phone: " + user.getPhone());
        holder.tvRole.setText(user.getRole());

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            Glide.with(context)
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // Nếu cần xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + user.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar;
        TextView tvID, tvName, tvPhone, tvRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvID = itemView.findViewById(R.id.tvID);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}
