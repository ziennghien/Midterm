package com.example.studentmanagementapp.user;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.LoginHistoryAdapter;
import com.example.studentmanagementapp.model.LoginHistory;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserDetailActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private Button btnChangeImage, btnSave, btnDelete;
    private TextView edtID;
    private EditText edtName, edtAge, edtPhone;
    private Spinner spinnerRole;
    private SwitchMaterial switchStatus;
    private RecyclerView recyclerLoginHistory;

    private DatabaseReference usersRef, historyRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri imageUri;

    private User user;
    private final List<LoginHistory> historyList = new ArrayList<>();
    private LoginHistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersRef = FirebaseHelper.getReference("Users");
        historyRef = FirebaseHelper.getReference("LoginHistory");
        storageRef = FirebaseStorage.getInstance().getReference("avatars");

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
        recyclerLoginHistory = findViewById(R.id.recyclerLoginHistory);

        historyAdapter = new LoginHistoryAdapter(this, historyList);
        recyclerLoginHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerLoginHistory.setAdapter(historyAdapter);

        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "Không nhận được thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgAvatar.setImageURI(imageUri);

                        if (user != null && imageUri != null) {
                            StorageReference avatarRef = storageRef.child(user.getId() + ".jpg");
                            avatarRef.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot ->
                                            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                String downloadUrl = uri.toString();
                                                usersRef.child(user.getId()).child("image").setValue(downloadUrl)
                                                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ảnh đã cập nhật", Toast.LENGTH_SHORT).show())
                                                        .addOnFailureListener(e -> Toast.makeText(this, "Không thể lưu ảnh", Toast.LENGTH_SHORT).show());
                                            }))
                                    .addOnFailureListener(e -> Toast.makeText(this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
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
        loadUserData();
        if ("admin".equalsIgnoreCase(user.getRole())) {
            btnDelete.setVisibility(Button.GONE);
            btnSave.setVisibility(Button.GONE);
            btnChangeImage.setEnabled(false);

            edtName.setEnabled(false);
            edtAge.setEnabled(false);
            edtPhone.setEnabled(false);
            switchStatus.setEnabled(false);
            spinnerRole.setEnabled(false);
        }

        loadLoginHistory();

        btnSave.setOnClickListener(v -> saveUserChanges());
        btnDelete.setOnClickListener(v -> deleteUser());
        btnChangeImage.setOnClickListener(v -> openImageChooser());

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
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
            Glide.with(this).load(user.getImage()).placeholder(R.drawable.ic_avatar_placeholder).into(imgAvatar);
        }
    }

    private void loadLoginHistory() {
        historyRef.orderByChild("userId").equalTo(user.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            LoginHistory entry = snap.getValue(LoginHistory.class);
                            if (entry != null) {
                                historyList.add(entry);
                            }
                        }
                        historyAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDetailActivity.this, "Không tải được lịch sử đăng nhập", Toast.LENGTH_SHORT).show();
                    }
                });
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
        if (!phone.startsWith("+84") || phone.length() != 12) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng +84 và đủ 9 số", Toast.LENGTH_SHORT).show();
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
