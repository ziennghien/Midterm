package com.example.studentmanagementapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.LoginActivity;
import com.example.studentmanagementapp.ProfileActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.student.StudentManagementActivity;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EmployeeActivity extends AppCompatActivity {

    private TextView tvStudentCount;
    private Button btnStudent, btnProfile, btnLogout;

    private DatabaseReference studentsRef;
    private FirebaseAuth mAuth;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_employee); // Đảm bảo bạn đặt tên XML đúng

        // Nhận currentUser từ LoginActivity
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null) {
            Toast.makeText(this, "Không nhận được user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        studentsRef = FirebaseHelper.getReference("Students");

        // Ánh xạ view
        tvStudentCount = findViewById(R.id.tvStudentCount);
        btnStudent = findViewById(R.id.btnStudent);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Đếm số lượng sinh viên
        countStudents();

        // Điều hướng
        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeActivity.this, StudentManagementActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeActivity.this, ProfileActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(EmployeeActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EmployeeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void countStudents() {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvStudentCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeeActivity.this, "Lỗi khi tải danh sách sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
