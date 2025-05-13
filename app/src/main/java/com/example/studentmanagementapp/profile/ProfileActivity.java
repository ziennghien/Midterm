package com.example.studentmanagementapp.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText edtName, edtAge, edtPhone, edtEmail, edtPassword, edtStatus, edtRole;
    private ImageView imgAvatar;
    private Button btnChangeAvatar, btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private Uri avatarUri = null;
    private ProgressDialog progressDialog;

    private String oldEmail, oldPhone, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupFirebase();

        if (currentUser == null) {
            showToast("User not logged in");
            finish();
            return;
        }

        loadUserInfo();

        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> validateAndSaveProfile());
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnSave = findViewById(R.id.btnSave);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtStatus = findViewById(R.id.edtStatus);
        edtRole = findViewById(R.id.edtRole);
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void loadUserInfo() {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(currentUser.getEmail())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtName.setText(doc.getString("name"));
                        edtAge.setText(String.valueOf(doc.getLong("age")));
                        edtPhone.setText(doc.getString("phone"));
                        edtStatus.setText(doc.getString("status"));
                        edtRole.setText(doc.getString("role"));
                        edtEmail.setText(currentUser.getEmail());

                        role = doc.getString("role");
                        oldEmail = currentUser.getEmail();
                        oldPhone = doc.getString("phone");

                        String avatarUrl = doc.getString("avatarUrl");
                        if (!TextUtils.isEmpty(avatarUrl)) {
                            Glide.with(this).load(avatarUrl).into(imgAvatar);
                        }

                        setupEditableFields();
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to load profile"));
    }

    private void setupEditableFields() {
        boolean isEmployee = "employee".equalsIgnoreCase(role);
        edtName.setEnabled(!isEmployee);
        edtAge.setEnabled(!isEmployee);
        edtPhone.setEnabled(!isEmployee);
        edtEmail.setEnabled(!isEmployee);
        edtPassword.setEnabled(!isEmployee);
        btnChangeAvatar.setVisibility(Button.VISIBLE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), avatarUri);
                imgAvatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateAndSaveProfile() {
        String name = edtName.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!"employee".equalsIgnoreCase(role) &&
                (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(phone) ||
                        TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {
            showToast("Please fill all fields");
            return;
        }

        showLoading("Saving...");
        checkUniqueAndSave(name, ageStr, phone, email, password);
    }

    private void checkUniqueAndSave(String name, String age, String phone, String email, String password) {
        CollectionReference usersRef = firestore.collection(AppConstants.COLLECTION_USERS);

        usersRef.whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(phoneSnapshot -> {
                    if (isConflict(phoneSnapshot)) {
                        dismissLoading("Phone number already exists!");
                        return;
                    }

                    usersRef.whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener(emailSnapshot -> {
                                if (isConflict(emailSnapshot)) {
                                    dismissLoading("Email already exists!");
                                } else {
                                    updateUserProfile(name, age, phone, email, password);
                                }
                            });
                });
    }

    private boolean isConflict(Iterable<? extends QueryDocumentSnapshot> snapshots) {
        for (var doc : snapshots) {
            if (!doc.getId().equals(oldEmail)) {
                return true;
            }
        }
        return false;
    }

    private void updateUserProfile(String name, String age, String phone, String email, String password) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", Integer.parseInt(age));
        updates.put("phone", phone);
        updates.put("email", email);

        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(oldEmail)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!email.equals(oldEmail)) currentUser.updateEmail(email);
                    if (!TextUtils.isEmpty(password)) currentUser.updatePassword(password);

                    if (avatarUri != null) {
                        uploadAvatar(email);
                    } else {
                        dismissLoading("Profile updated successfully!");
                        finish();
                    }
                })
                .addOnFailureListener(e -> dismissLoading("Failed to update profile!"));
    }

    private void uploadAvatar(String email) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatars/" + email + ".jpg");
        storageRef.putFile(avatarUri)
                .addOnSuccessListener(task -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            firestore.collection(AppConstants.COLLECTION_USERS)
                                    .document(email)
                                    .update("avatarUrl", uri.toString());
                            dismissLoading("Profile updated successfully!");
                            finish();
                        }))
                .addOnFailureListener(e -> dismissLoading("Failed to upload avatar"));
    }

    private void showLoading(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
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
}
