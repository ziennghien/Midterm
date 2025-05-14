package com.example.studentmanagementapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.Certificate;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.certificate.CertificateDetailActivity;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {

    private final List<Certificate> list;
    private final User currentUser;

    public CertificateAdapter(List<Certificate> list, User currentUser) {
        this.list = list;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public CertificateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_certificate, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateAdapter.ViewHolder holder, int pos) {
        Certificate cert = list.get(pos);
        holder.tvID.setText(cert.getId());
        holder.tvName.setText(cert.getName());
        holder.tvIssueDate.setText(cert.getIssueDate());

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), CertificateDetailActivity.class);
            i.putExtra("currentUser", currentUser);
            i.putExtra("certificateId", cert.getId());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvID, tvName, tvIssueDate;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID         = itemView.findViewById(R.id.tvID);
            tvName       = itemView.findViewById(R.id.tvName);
            tvIssueDate  = itemView.findViewById(R.id.tvIssueDate);
        }
    }
}
