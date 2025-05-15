package com.example.studentmanagementapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.BaseActivity;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class ProfileActivity extends BaseActivity {

    private ImageView imgAvatar;
    private TextView tvUserId;
    private EditText edtName, edtAge, edtPhone, edtRole, edtUser;
    private Button btnChangeImage, btnSave;
    private Uri imageUri;

    private User currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setToolbar(R.id.toolbar);

        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        if (currentUser == null) {
            Toast.makeText(this, "Không nhận được người dùng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseHelper.getReference("Users").child(currentUser.getId());
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserId = findViewById(R.id.tvUserId);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        edtRole = findViewById(R.id.edtRole);
        edtUser = findViewById(R.id.edtUser);
        btnSave = findViewById(R.id.btnSave);

        tvUserId.setText(currentUser.getId());

        loadUserData();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgAvatar.setImageURI(imageUri);

                        if (currentUser != null && imageUri != null) {
                            StorageReference avatarRef = storageRef.child(currentUser.getId() + ".jpg");
                            avatarRef.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot ->
                                            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                String downloadUrl = uri.toString();
                                                userRef.child("image").setValue(downloadUrl)
                                                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ảnh đã cập nhật", Toast.LENGTH_SHORT).show())
                                                        .addOnFailureListener(e -> Toast.makeText(this, "Không thể lưu ảnh", Toast.LENGTH_SHORT).show());
                                            }))
                                    .addOnFailureListener(e -> Toast.makeText(this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show());
                        }
                    }
                });

        btnChangeImage.setOnClickListener(view -> openImageChooser());
        btnSave.setOnClickListener(view -> saveUserData());
        edtPhone.setText("+84");
        edtPhone.setSelection(edtPhone.getText().length());

        edtPhone.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                String input = s.toString();

                // Nếu không bắt đầu bằng +84, khôi phục lại
                if (!input.startsWith("+84")) {
                    edtPhone.setText("+84");
                    edtPhone.setSelection(edtPhone.getText().length());
                } else if (input.length() > 12) {
                    // Nếu quá 9 số sau +84 => cắt bớt
                    edtPhone.setText(input.substring(0, 12));
                    edtPhone.setSelection(edtPhone.getText().length());
                }

                editing = false;
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
        imagePickerLauncher.launch(intent);
    }

    private void saveUserData() {
        if (!validate()) return;

        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setName(edtName.getText().toString().trim());
        updatedUser.setAge(Integer.parseInt(edtAge.getText().toString().trim()));
        updatedUser.setPhone(edtPhone.getText().toString().trim());
        updatedUser.setRole(edtRole.getText().toString().trim());
        updatedUser.setUserName(edtUser.getText().toString().trim());
        updatedUser.setStatus(true);
        updatedUser.setImage(currentUser.getImage());

        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã lưu thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show());
    }

    private boolean validate() {
        if (edtName.getText().toString().trim().isEmpty()
                || edtAge.getText().toString().trim().isEmpty()
                || edtPhone.getText().toString().trim().isEmpty()
                || edtUser.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        String phone = edtPhone.getText().toString().trim();
        if (!phone.startsWith("+84") || phone.length() != 12) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng +84 và đủ 9 số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}