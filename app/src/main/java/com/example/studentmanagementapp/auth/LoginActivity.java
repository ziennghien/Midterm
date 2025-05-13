package com.example.studentmanagementapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please enter email and password");
            return;
        }

        if (email.equalsIgnoreCase("admin@gmail.com")) {
            loginOrCreateAdmin(email, password);
        } else {
            normalLogin(email, password);
        }
    }

    private void loginOrCreateAdmin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkUserStatus(email))
                .addOnFailureListener(e -> {
                    // Nếu chưa có admin → Tạo mới
                    auth.createUserWithEmailAndPassword(email, "admin123")
                            .addOnSuccessListener(result -> {
                                // Sau khi tạo thành công → Ghi document users
                                createAdminDocument(email);
                            })
                            .addOnFailureListener(error -> showToast("Admin creation failed: " + error.getMessage()));
                });
    }

    private void createAdminDocument(String email) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("name", "Admin");
        adminData.put("age", 30);
        adminData.put("phone", "0123456789");
        adminData.put("email", email);
        adminData.put("status", "Normal");
        adminData.put("role", "admin");
        adminData.put("avatarUrl", "");

        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(email)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Admin account created successfully!");
                    redirectToDashboard("admin");
                })
                .addOnFailureListener(e -> showToast("Failed to create admin user document!"));
    }

    private void normalLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkUserStatus(email))
                .addOnFailureListener(e -> showToast("Login failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
    }

    private void checkUserStatus(String email) {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(email)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String status = document.getString("status");
                        String role = document.getString("role");

                        if ("Locked".equalsIgnoreCase(status)) {
                            showToast("Your account has been locked.");
                            auth.signOut();
                        } else {
                            saveLoginHistory(email);
                            redirectToDashboard(role);
                        }
                    } else {
                        showToast("User information not found!");
                        auth.signOut();
                    }
                })
                .addOnFailureListener(e -> showToast("Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
    }

    private void saveLoginHistory(String email) {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("timestamp", Timestamp.now());

        firestore.collection(AppConstants.COLLECTION_LOGIN_HISTORY)
                .add(loginData)
                .addOnFailureListener(e -> showToast("Failed to save login history"));
    }

    private void redirectToDashboard(String role) {
        if (role == null) {
            showToast("User role is missing!");
            return;
        }

        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "manager":
                intent = new Intent(this, ManagerDashboardActivity.class);
                break;
            case "employee":
                intent = new Intent(this, EmployeeDashboardActivity.class);
                break;
            default:
                showToast("Unknown role: " + role);
                auth.signOut();
                intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
