package com.example.studentmanagementapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.admin.HistoryViewActivity;
import com.example.studentmanagementapp.ProfileActivity;
import com.example.studentmanagementapp.model.User;
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
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        // Nhận currentUser từ LoginActivity
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null) {
            Toast.makeText(this, "Không nhận được user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("CurrentUser (Admin)", "ID: " + currentUser.getId()
                + ", Name: " + currentUser.getName()
                + ", Email: " + currentUser.getUserName()
                + ", Role: " + currentUser.getRole());

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();
        studentsRef = FirebaseHelper.getStudentsReference();

        tvManagerCount = findViewById(R.id.tvManagerCount);
        tvEmployeeCount = findViewById(R.id.tvEmployeeCount);
        tvStudentCount = findViewById(R.id.tvStudentCount);

        btnUser = findViewById(R.id.btnUser);
        btnStudent = findViewById(R.id.btnStudent);
        btnHistory = findViewById(R.id.btnHistory);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Đếm số lượng người dùng
        countUsersByRole("manager", tvManagerCount);
        countUsersByRole("employee", tvEmployeeCount);
        countStudents(tvStudentCount);

        // Mở User Management
        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserManagementActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // Mở Student Management và truyền currentUser
        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentManagementActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // Mở lịch sử
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryViewActivity.class)));

        // Mở hồ sơ cá nhân
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        // Đăng xuất
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
