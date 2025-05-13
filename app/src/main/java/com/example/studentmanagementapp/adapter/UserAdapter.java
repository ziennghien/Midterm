package com.example.studentmanagementapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.user.AddEditUserActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false); // layout từng dòng
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText("Email: " + user.getUserName());
        holder.tvPhone.setText("Phone: " + user.getPhone());
        holder.tvRole.setText("Role: " + user.getRole());

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            Glide.with(context)
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }

        // Xử lý nút sửa
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditUserActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("userId", user.getId()); // user1, user2,...
            context.startActivity(intent);
        });

        // Xử lý nút xoá
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xoá người dùng")
                    .setMessage("Bạn có chắc chắn muốn xoá " + user.getName() + "?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        DatabaseReference ref = FirebaseHelper.getReference("Users").child(user.getId());
                        ref.removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xoá người dùng", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi xoá", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar;
        TextView tvName, tvEmail, tvPhone, tvRole;
        ImageButton btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            //btnEdit = itemView.findViewById(R.id.btnEdit);
            //btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
