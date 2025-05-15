package com.example.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
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

    private static final long IDLE_TIMEOUT = 600000; // 10 phút
    private CountDownTimer idleTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        setupIdleTimer();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            String email = currentUser.getEmail();
            usersRef = FirebaseHelper.getUsersReference();

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userEmail = userSnapshot.child("userName").getValue(String.class);
                        if (email.equals(userEmail)) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setId(userSnapshot.getKey());
                                routeToRole(user);
                            }
                            return;
                        }
                    }

                    if (!found) {
                        Toast.makeText(MainActivity.this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                        logout();
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
            logout();
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
                logout();
                return;
        }

        intent.putExtra("currentUser", user);
        startActivity(intent);
        finish();
    }

    private void setupIdleTimer() {
        idleTimer = new CountDownTimer(IDLE_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, "Tự động đăng xuất do không hoạt động.", Toast.LENGTH_LONG).show();
                logout();
            }
        };

        resetIdleTimer();
    }

    private void resetIdleTimer() {
        idleTimer.cancel();
        idleTimer.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        resetIdleTimer();
        return super.dispatchTouchEvent(ev);
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (idleTimer != null) idleTimer.cancel();
    }
}