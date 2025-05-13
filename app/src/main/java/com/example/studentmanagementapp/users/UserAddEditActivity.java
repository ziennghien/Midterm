package com.example.studentmanagementapp.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.models.User;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserAddEditActivity extends AppCompatActivity {

    private EditText edtName, edtAge, edtPhone, edtEmail;
    private Spinner spinnerStatus, spinnerRole;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    private String userEmail; // Nếu khác null => đang sửa user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add_edit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userEmail = getIntent().getStringExtra("userEmail");

        setupSpinners();

        if (userEmail != null) {
            loadUserData();
        }

        btnSave.setOnClickListener(v -> {
            if (userEmail == null) {
                addUser();
            } else {
                updateUser();
            }
        });
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinners() {
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Normal", "Locked"}));
        spinnerRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"employee", "manager", "admin"}));
    }

    private void loadUserData() {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userEmail)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            edtName.setText(user.getName());
                            edtAge.setText(String.valueOf(user.getAge())); // 🔥 Convert int ➔ String
                            edtPhone.setText(user.getPhone());
                            edtEmail.setText(user.getEmail());
                            spinnerStatus.setSelection("Locked".equalsIgnoreCase(user.getStatus()) ? 1 : 0);
                            spinnerRole.setSelection(getRolePosition(user.getRole()));
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load user data"));
    }

    private int getRolePosition(String role) {
        if ("admin".equalsIgnoreCase(role)) return 2;
        if ("manager".equalsIgnoreCase(role)) return 1;
        return 0;
    }

    private void addUser() {
        String name = edtName.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
            showToast("Please fill all fields");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            showToast("Invalid age format");
            return;
        }

        progressDialog = ProgressDialog.show(this, null, "Adding user...", true, false);

        CollectionReference usersRef = firestore.collection(AppConstants.COLLECTION_USERS);

        usersRef.whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(phoneSnap -> {
                    if (!phoneSnap.isEmpty()) {
                        dismissLoading("Phone number already exists!");
                        return;
                    }
                    usersRef.whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener(emailSnap -> {
                                if (!emailSnap.isEmpty()) {
                                    dismissLoading("Email already exists!");
                                } else {
                                    auth.createUserWithEmailAndPassword(email, "user123")
                                            .addOnSuccessListener(authResult -> saveUserToFirestore(name, age, phone, email, status, role))
                                            .addOnFailureListener(e -> dismissLoading("Auth error: " + e.getMessage()));
                                }
                            })
                            .addOnFailureListener(e -> dismissLoading("Error checking email"));
                })
                .addOnFailureListener(e -> dismissLoading("Error checking phone"));
    }

    private void saveUserToFirestore(String name, int age, String phone, String email, String status, String role) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("age", age);
        userMap.put("phone", phone);
        userMap.put("email", email);
        userMap.put("status", status);
        userMap.put("role", role);
        userMap.put("avatarUrl", "");

        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(email)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    dismissLoading("User added successfully!");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> dismissLoading("Failed to add user!"));
    }

    private void updateUser() {
        String name = edtName.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
            showToast("Please fill all fields");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            showToast("Invalid age format");
            return;
        }

        progressDialog = ProgressDialog.show(this, null, "Updating user...", true, false);

        CollectionReference usersRef = firestore.collection(AppConstants.COLLECTION_USERS);

        usersRef.whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(phoneSnap -> {
                    boolean phoneConflict = phoneSnap.getDocuments().stream().anyMatch(doc -> !doc.getId().equals(userEmail));
                    if (phoneConflict) {
                        dismissLoading("Phone number already exists!");
                        return;
                    }

                    if (!email.equals(userEmail)) {
                        usersRef.whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener(emailSnap -> {
                                    if (!emailSnap.isEmpty()) {
                                        dismissLoading("Email already exists!");
                                    } else {
                                        performUserUpdate(name, age, phone, email, status, role);
                                    }
                                })
                                .addOnFailureListener(e -> dismissLoading("Error checking email"));
                    } else {
                        performUserUpdate(name, age, phone, email, status, role);
                    }
                })
                .addOnFailureListener(e -> dismissLoading("Error checking phone"));
    }

    private void performUserUpdate(String name, int age, String phone, String email, String status, String role) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("status", status);
        updates.put("role", role);

        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userEmail)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!email.equals(userEmail)) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            currentUser.updateEmail(email);
                        }
                    }
                    dismissLoading("User updated successfully!");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> dismissLoading("Failed to update user!"));
    }

    private void dismissLoading(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}