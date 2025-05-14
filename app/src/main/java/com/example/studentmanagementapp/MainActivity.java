package com.example.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.dashboard.AdminActivity;
import com.example.studentmanagementapp.dashboard.EmployeeActivity;
import com.example.studentmanagementapp.dashboard.ManagerActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Chưa đăng nhập → LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // Đã đăng nhập → Lấy email để tìm user
            String email = currentUser.getEmail();
            usersRef = FirebaseHelper.getUsersReference();

            // Tìm user có userName == email
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userEmail = userSnapshot.child("userName").getValue(String.class);
                        if (email.equals(userEmail)) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setId(userSnapshot.getKey()); // set ID từ key Firebase
                                routeToRole(user); // truyền user thay vì chỉ role
                            }
                            return;
                        }
                    }

                    if (!found) {
                        Toast.makeText(MainActivity.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void routeToRole(User user) {
        if (user == null || user.getRole() == null) {
            Toast.makeText(this, "Vai trò không hợp lệ!", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
        }

        intent.putExtra("currentUser", user); // ✅ Truyền user qua intent
        startActivity(intent);
        finish();
    }

}
