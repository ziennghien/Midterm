package com.example.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.dashboard.AdminActivity;
import com.example.studentmanagementapp.dashboard.EmployeeActivity;
import com.example.studentmanagementapp.dashboard.ManagerActivity;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Đảm bảo tên file XML đúng

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();

        // Liên kết với layout
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Xử lý sự kiện login
        btnLogin.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(email);
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRole(String email) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userEmail = userSnapshot.child("userName").getValue(String.class);
                    if (email.equals(userEmail)) {
                        found = true;
                        String role = userSnapshot.child("role").getValue(String.class);
                        routeToRole(role);
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(LoginActivity.this, "Email không tồn tại trong CSDL!", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối CSDL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void routeToRole(String role) {
        if (role == null) {
            Toast.makeText(this, "Không xác định được vai trò!", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            return;
        }

        Intent intent = null;

        switch (role) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "employee":
                intent = new Intent(this, EmployeeActivity.class);
                break;
            case "manager":
                intent = new Intent(this, ManagerActivity.class);
                break;
            default:
                Toast.makeText(this, "Vai trò không hợp lệ!", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                return;
        }

        startActivity(intent);
        finish(); // Đóng LoginActivity
    }
}
