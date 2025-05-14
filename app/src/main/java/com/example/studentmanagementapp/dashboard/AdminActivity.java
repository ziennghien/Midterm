package com.example.studentmanagementapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.admin.HistoryViewActivity;
import com.example.studentmanagementapp.ProfileActivity;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.example.studentmanagementapp.student.StudentManagementActivity;
import com.example.studentmanagementapp.user.UserManagementActivity;

import com.example.studentmanagementapp.LoginActivity;
import com.example.studentmanagementapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdminActivity extends AppCompatActivity {

    private TextView tvManagerCount, tvEmployeeCount, tvStudentCount;
    private Button btnUser, btnStudent, btnHistory, btnProfile, btnLogout;

    private DatabaseReference usersRef, studentsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.studentmanagementapp.R.layout.activity_dashboard_admin); // Gắn layout XML bạn đã cung cấp

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();
        studentsRef = FirebaseHelper.getStudentsReference();

        // Ánh xạ view
        tvManagerCount = findViewById(com.example.studentmanagementapp.R.id.tvManagerCount);
        tvEmployeeCount = findViewById(com.example.studentmanagementapp.R.id.tvEmployeeCount);
        tvStudentCount = findViewById(com.example.studentmanagementapp.R.id.tvStudentCount);

        btnUser = findViewById(com.example.studentmanagementapp.R.id.btnUser);
        btnStudent = findViewById(com.example.studentmanagementapp.R.id.btnStudent);
        btnHistory = findViewById(com.example.studentmanagementapp.R.id.btnHistory);
        btnProfile = findViewById(com.example.studentmanagementapp.R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Đếm số lượng theo vai trò
        countUsersByRole("manager", tvManagerCount);
        countUsersByRole("employee", tvEmployeeCount);
        countStudents(tvStudentCount);

        // Xử lý các nút
        btnUser.setOnClickListener(view -> startActivity(new Intent(this, UserManagementActivity.class)));
        btnStudent.setOnClickListener(view -> startActivity(new Intent(this, StudentManagementActivity.class)));
        btnHistory.setOnClickListener(view -> startActivity(new Intent(this, HistoryViewActivity.class)));
        btnProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void countUsersByRole(String role, TextView targetView) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userRole = userSnap.child("role").getValue(String.class);
                    if (role.equals(userRole)) {
                        count++;
                    }
                }
                targetView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Lỗi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countStudents(TextView targetView) {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long studentCount = snapshot.getChildrenCount();
                targetView.setText(String.valueOf(studentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Lỗi tải dữ liệu sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
