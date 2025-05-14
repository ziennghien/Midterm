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

        // Gán dữ liệu
        holder.tvID.setText(student.getId());
        holder.tvName.setText(" - " + student.getName());
        holder.tvMajor.setText("Major: " + student.getMajor());
        holder.tvClass.setText("Class: " + student.getClassName());

        // Xử lý click item
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent;
            if ("employee".equalsIgnoreCase(role)) {
                intent = new Intent(context, StudentDetailActivity.class);
            } else {
                intent = new Intent(context, StudentAddEditActivity.class);
                intent.putExtra("mode", "edit");
            }
            intent.putExtra("studentId", student.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvID, tvName, tvMajor, tvClass;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvID);
            tvName = itemView.findViewById(R.id.tvName);
            tvMajor = itemView.findViewById(R.id.tvMajor);
            tvClass = itemView.findViewById(R.id.tvClass);
        }
    }
}
