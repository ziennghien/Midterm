package com.example.studentmanagementapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.LoginHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.LoginHistoryViewHolder> {

    private final Context context;
    private final List<LoginHistory> historyList;

    public LoginHistoryAdapter(Context context, List<LoginHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public LoginHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_login_history, parent, false);
        return new LoginHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoginHistoryViewHolder holder, int position) {
        LoginHistory history = historyList.get(position);

        holder.tvID.setText("ID: " + history.getUserId());
        holder.tvName.setText(history.getName());
        holder.tvRole.setText(history.getRole());

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(history.getTimestamp()));
        holder.tvLoginTime.setText(formattedDate);

        Glide.with(context)
                .load(history.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class LoginHistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvID, tvName, tvLoginTime, tvRole;

        public LoginHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvID = itemView.findViewById(R.id.tvID);
            tvName = itemView.findViewById(R.id.tvName);
            tvLoginTime = itemView.findViewById(R.id.tvLoginTime);
            tvRole = itemView.findViewById(R.id.tvRole);
        }
    }
}
