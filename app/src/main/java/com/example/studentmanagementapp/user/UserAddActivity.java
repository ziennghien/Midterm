package com.example.studentmanagementapp.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class UserAddActivity extends AppCompatActivity {

    private TextView edtID;
    private EditText edtUserName, edtName, edtAge, edtPhone;
    private Spinner spinnerRole;
    private SwitchMaterial switchStatus;
    private Button btnSave;

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private int userCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersRef = FirebaseHelper.getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        edtID = findViewById(R.id.edtID);
        edtUserName = findViewById(R.id.edtUserName);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        edtPhone = findViewById(R.id.edtPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        switchStatus = findViewById(R.id.switchStatus);
        btnSave = findViewById(R.id.btnSave);

        generateUserId();

        btnSave.setOnClickListener(v -> saveUser());
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

    private void generateUserId() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userCount = (int) snapshot.getChildrenCount() + 1;
                String newId = "user" + userCount;
                edtID.setText(newId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserAddActivity.this, "Không thể tạo ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUser() {
        String id = edtID.getText().toString();
        String userName = edtUserName.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        boolean status = switchStatus.isChecked();

        if (TextUtils.isEmpty(userName)) {
            edtUserName.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()) {
            edtUserName.setError("Tên đăng nhập phải là email hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            edtName.setError("Vui lòng nhập tên");
            return;
        }

        if (TextUtils.isEmpty(ageStr)) {
            edtAge.setError("Vui lòng nhập tuổi");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (!phone.startsWith("+84") || phone.length() != 12) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng +84 và đủ 9 số", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 0) {
                edtAge.setError("Tuổi phải >= 0");
                return;
            }
        } catch (NumberFormatException e) {
            edtAge.setError("Tuổi không hợp lệ");
            return;
        }

        // Dùng userName làm email và password
        mAuth.createUserWithEmailAndPassword(userName, userName)
                .addOnSuccessListener(result -> {
                    User user = new User(id, userName, name, age, phone, role, status);
                    usersRef.child(id).setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
