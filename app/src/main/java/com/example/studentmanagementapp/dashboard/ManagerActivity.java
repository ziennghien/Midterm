package com.example.studentmanagementapp.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.ProfileActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.example.studentmanagementapp.student.StudentManagementActivity;
import com.example.studentmanagementapp.LoginActivity;
import com.example.studentmanagementapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class ManagerActivity extends AppCompatActivity {

    private TextView tvStudentCount;
    private Button btnStudent, btnProfile, btnLogout;

    private DatabaseReference studentsRef;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_manager);

        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        if (currentUser == null) {
            Toast.makeText(this, "Không nhận được user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("CurrentUser (Manager)", "ID: " + currentUser.getId()
                + ", Name: " + currentUser.getName()
                + ", Email: " + currentUser.getUserName()
                + ", Role: " + currentUser.getRole());

        mAuth = FirebaseAuth.getInstance();
        studentsRef = FirebaseHelper.getStudentsReference();

        tvStudentCount = findViewById(R.id.tvStudentCount);
        btnStudent = findViewById(R.id.btnStudent);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        countStudents(tvStudentCount);

        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentManagementActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
                Toast.makeText(ManagerActivity.this, "Lỗi tải dữ liệu sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }
}