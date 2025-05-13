package com.example.studentmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.auth.LoginActivity;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
        } else {
            String email = currentUser.getEmail();
            if (email == null || email.isEmpty()) {
                logoutAndRedirect();
            } else {
                checkUserRole(email);
            }
        }
    }

    private void checkUserRole(String email) {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if (role != null && !role.isEmpty()) {
                            navigateToDashboard(role);
                        } else {
                            logoutAndRedirect();
                        }
                    } else {
                        logoutAndRedirect();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                    logoutAndRedirect();
                });
    }

    private void navigateToDashboard(String role) {
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
                Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                logoutAndRedirect();
                return;
        }
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void logoutAndRedirect() {
        auth.signOut();
        redirectToLogin();
    }
}
