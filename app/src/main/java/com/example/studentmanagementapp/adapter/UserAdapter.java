package com.example.studentmanagementapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.user.UserDetailActivity;

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

        holder.tvID.setText("ID: " + user.getId());
        holder.tvName.setText(user.getName());
        holder.tvRole.setText(user.getRole());
        holder.tvPhone.setText(user.getPhone());


        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .into(holder.imgAvatar);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("user", user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvID, tvName,tvPhone, tvRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvID = itemView.findViewById(R.id.tvID);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }
}