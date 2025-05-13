package com.example.studentmanagementapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgAvatar;
    private EditText edtName, edtAge, edtPhone, edtRole, edtUser, edtPassword;
    private Button btnChangeImage, btnSave;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private String uid;
    private String userKey; // = user1, user2,...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

        // UI mapping
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        edtRole = findViewById(R.id.edtRole);
        edtUser = findViewById(R.id.edtUser);
        edtPassword = findViewById(R.id.edtPassword);
        btnSave = findViewById(R.id.btnSave);

        if (uid == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy key người dùng (user1, user2...) từ UIDMapping
        FirebaseHelper.getReference("UIDMapping").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userKey = snapshot.getValue(String.class);
                        if (userKey == null) {
                            Toast.makeText(ProfileActivity.this, "Không tìm thấy ánh xạ người dùng", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        userRef = FirebaseHelper.getReference("Users").child(userKey);
                        loadUserData();

                        btnChangeImage.setOnClickListener(view -> openImageChooser());
                        btnSave.setOnClickListener(view -> saveUserData());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Lỗi khi tải ánh xạ UID", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    edtName.setText(user.getName());
                    edtAge.setText(String.valueOf(user.getAge()));
                    edtPhone.setText(user.getPhone());
                    edtRole.setText(user.getRole());
                    edtUser.setText(user.getUserName());
                    edtPassword.setText(user.getPassword());

                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .into(imgAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);

            if (userKey != null) {
                StorageReference avatarRef = storageRef.child(userKey + ".jpg");

                avatarRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot ->
                                avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    userRef.child("image").setValue(downloadUrl)
                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ảnh đại diện đã cập nhật", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(this, "Không thể lưu URL", Toast.LENGTH_SHORT).show());
                                }))
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveUserData() {
        if (!validate()) return;

        User updatedUser = new User(
                userKey,
                edtUser.getText().toString().trim(),
                edtPassword.getText().toString().trim(),
                edtName.getText().toString().trim(),
                Integer.parseInt(edtAge.getText().toString().trim()),
                edtPhone.getText().toString().trim(),
                edtRole.getText().toString().trim(),
                true
        );

        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show());
    }

    private boolean validate() {
        if (edtName.getText().toString().trim().isEmpty()
                || edtAge.getText().toString().trim().isEmpty()
                || edtPhone.getText().toString().trim().isEmpty()
                || edtUser.getText().toString().trim().isEmpty()
                || edtPassword.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
