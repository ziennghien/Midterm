package com.example.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.dashboard.AdminActivity;
import com.example.studentmanagementapp.dashboard.EmployeeActivity;
import com.example.studentmanagementapp.dashboard.ManagerActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, historyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseHelper.getUsersReference();
        historyRef = FirebaseHelper.getReference("LoginHistory");

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

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

                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(userSnapshot.getKey());
                            logLoginHistory(user.getId());
                            routeToRole(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Không đọc được dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
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

    private void logLoginHistory(String userId) {
        Map<String, Object> loginRecord = new HashMap<>();
        loginRecord.put("userId", userId);
        loginRecord.put("timestamp", System.currentTimeMillis());

        historyRef.push().setValue(loginRecord);
    }

    private void routeToRole(User user) {
        if (user == null || user.getRole() == null) {
            Toast.makeText(this, "Không xác định được vai trò!", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            return;
        }

        Intent intent;

        switch (user.getRole()) {
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

        intent.putExtra("currentUser", user);
        Log.d("CurrentUser (Login)", "ID: " + user.getId()
                + ", Name: " + user.getName()
                + ", Email: " + user.getUserName()
                + ", Role: " + user.getRole());

        startActivity(intent);
        finish();
    }
}