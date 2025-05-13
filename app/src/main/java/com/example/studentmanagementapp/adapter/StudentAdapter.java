package com.example.studentmanagementapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.student.StudentAddEditActivity;
import com.example.studentmanagementapp.student.StudentDetailActivity;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;
    private String role;

    public StudentAdapter(List<Student> studentList, String role) {
        this.studentList = studentList;
        this.role = role;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.tvId.setText("ID: " + student.getId());
        holder.tvName.setText("Name: " + student.getName());
        holder.tvMajor.setText("Major: " + student.getAddress()); // Bạn có thể thay đổi thành student.getMajor() nếu có

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            if ("employee".equals(role)) {
                // Chỉ xem chi tiết
                Intent intent = new Intent(context, StudentDetailActivity.class);
                intent.putExtra("studentId", student.getId());
                context.startActivity(intent);
            } else {
                // Admin và Manager được chỉnh sửa
                Intent intent = new Intent(context, StudentAddEditActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("studentId", student.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvMajor;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvStudentId);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvMajor = itemView.findViewById(R.id.tvStudentMajor);
        }
    }
}
