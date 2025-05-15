package com.example.studentmanagementapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;

public class UserDetailActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private Button btnChangeImage, btnSave, btnDelete;
    private TextView edtID;
    private EditText edtName, edtAge, edtPhone;
    private Spinner spinnerRole;
    private SwitchMaterial switchStatus;

    private DatabaseReference usersRef;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersRef = FirebaseHelper.getReference("Users");

        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        edtID = findViewById(R.id.edtID);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        switchStatus = findViewById(R.id.switchStatus);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "Không nhận được thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserData();

        btnSave.setOnClickListener(v -> saveUserChanges());
        btnDelete.setOnClickListener(v -> deleteUser());
        btnChangeImage.setOnClickListener(v -> Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void loadUserData() {
        edtID.setText(user.getId());
        edtName.setText(user.getName());
        edtAge.setText(String.valueOf(user.getAge()));
        edtPhone.setText(user.getPhone());
        switchStatus.setChecked(user.isStatus());

        if (user.getRole() != null) {
            String[] roles = getResources().getStringArray(R.array.roles_array);
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(user.getRole())) {
                    spinnerRole.setSelection(i);
                    break;
                }
            }
        }

        if (user.getImage() != null) {
            Glide.with(this).load(user.getImage()).into(imgAvatar);
        }
    }

    private void saveUserChanges() {
        String name = edtName.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        boolean status = switchStatus.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tuổi không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setName(name);
        user.setAge(age);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(status);

        usersRef.child(user.getId()).setValue(user)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        usersRef.child(user.getId()).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Xoá người dùng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}