package com.example.studentmanagementapp.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.models.User;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDetailActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvPhone, tvEmail, tvStatus, tvRole;
    private Button btnEdit;

    private String userEmail;
    private FirebaseFirestore firestore;
    private static final int REQUEST_EDIT_USER = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // ➡️ Thêm nút quay lại ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        firestore = FirebaseFirestore.getInstance();
        userEmail = getIntent().getStringExtra("userEmail");

        if (userEmail != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "User email not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(UserDetailActivity.this, UserAddEditActivity.class);
            intent.putExtra("userEmail", userEmail);
            startActivityForResult(intent, REQUEST_EDIT_USER);
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvRole = findViewById(R.id.tvRole);
        btnEdit = findViewById(R.id.btnEdit);
    }

    private void loadUserData() {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvName.setText(user.getName());
                            tvAge.setText(user.getAge());
                            tvPhone.setText(user.getPhone());
                            tvEmail.setText(user.getEmail());
                            tvStatus.setText(user.getStatus());
                            tvRole.setText(user.getRole());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_USER && resultCode == Activity.RESULT_OK) {
            // ➡️ Reload thông tin user nếu vừa sửa xong
            loadUserData();
        }
    }

    // ➡️ Xử lý khi bấm nút back trên ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}