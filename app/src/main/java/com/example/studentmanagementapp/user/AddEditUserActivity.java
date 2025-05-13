package com.example.studentmanagementapp.user;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class AddEditUserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView imgAvatar;
    private EditText edtName, edtAge, edtPhone, edtRole, edtUser, edtPassword;
    private Button btnChangeImage, btnSave;
    private Uri imageUri;

    private String mode = "add"; // or "edit"
    private String uidToEdit = null;
    private boolean imageUpdated = false;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View mapping
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        edtRole = findViewById(R.id.edtRole);
        edtUser = findViewById(R.id.edtUser);
        edtPassword = findViewById(R.id.edtPassword);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

        mode = getIntent().getStringExtra("mode");
        uidToEdit = getIntent().getStringExtra("userId"); // only used in "edit" mode

        if ("edit".equals(mode) && uidToEdit != null) {
            userRef = FirebaseHelper.getReference("Users").child(uidToEdit);
            loadUserData();
        }

        btnChangeImage.setOnClickListener(v -> openImageChooser());
        btnSave.setOnClickListener(v -> {
            if (!validate()) return;

            if ("edit".equals(mode)) {
                saveUser(uidToEdit); // update existing user
            } else {
                createNewUser(); // register with FirebaseAuth
            }
        });
    }

    private void createNewUser() {
        String email = edtUser.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String newUid = authResult.getUser().getUid();
                    userRef = FirebaseHelper.getReference("Users").child(newUid);
                    saveUser(newUid);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUser(String uid) {
        User user = new User(
                uid,
                edtUser.getText().toString().trim(),
                edtPassword.getText().toString().trim(),
                edtName.getText().toString().trim(),
                Integer.parseInt(edtAge.getText().toString().trim()),
                edtPhone.getText().toString().trim(),
                edtRole.getText().toString().trim(),
                true
        );

        userRef.setValue(user)
                .addOnSuccessListener(aVoid -> {
                    if (imageUpdated && imageUri != null) {
                        uploadAvatar(uid);
                    } else {
                        Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể lưu thông tin", Toast.LENGTH_SHORT).show());
    }

    private void uploadAvatar(String uid) {
        StorageReference avatarRef = storageRef.child(uid + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userRef.child("image").setValue(uri.toString());
                    Toast.makeText(this, "Ảnh đã cập nhật", Toast.LENGTH_SHORT).show();
                    finish();
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show());
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
                        Glide.with(AddEditUserActivity.this)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .into(imgAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditUserActivity.this, "Không thể tải thông tin", Toast.LENGTH_SHORT).show();
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
            imageUpdated = true;
        }
    }

    private boolean validate() {
        return !edtName.getText().toString().trim().isEmpty()
                && !edtAge.getText().toString().trim().isEmpty()
                && !edtPhone.getText().toString().trim().isEmpty()
                && !edtUser.getText().toString().trim().isEmpty()
                && !edtPassword.getText().toString().trim().isEmpty();
    }
}
