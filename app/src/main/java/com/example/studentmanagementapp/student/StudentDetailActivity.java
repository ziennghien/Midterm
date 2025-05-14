package com.example.studentmanagementapp.student;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.certificate.CertificateManagementActivity;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.*;

public class StudentDetailActivity extends AppCompatActivity {

    private EditText edtID, edtName, edtMajor, edtClass;
    private Button btnSave, btnDelete, btnCertificate;
    private DatabaseReference studentRef;
    private String studentId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        setupToolbar();

        // 1. Nhận extras
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        studentId   = getIntent().getStringExtra("studentId");
        if (currentUser == null || studentId == null) {
            Toast.makeText(this, "Thiếu thông tin user hoặc studentId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Ánh xạ view
        edtID         = findViewById(R.id.edtID);
        edtName       = findViewById(R.id.edtName);
        edtMajor      = findViewById(R.id.edtMajor);
        edtClass      = findViewById(R.id.edtClass);
        btnSave       = findViewById(R.id.btnSave);
        btnDelete     = findViewById(R.id.btnDelete);
        btnCertificate= findViewById(R.id.btnCertificate);

        // 3. Phân quyền
        String role = currentUser.getRole();
        if (!("admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role))) {
            btnSave.setVisibility(Button.GONE);
            btnDelete.setVisibility(Button.GONE);
            edtName.setEnabled(false);
            edtMajor.setEnabled(false);
            edtClass.setEnabled(false);
        }

        // 4. Load student & thiết lập ref
        studentRef = FirebaseHelper.getReference("Students").child(studentId);
        loadStudentDetail();

        // 5. Save
        btnSave.setOnClickListener(v -> updateStudent());

        // 6. Delete (cascade) with confirmation dialog
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa sinh viên này và tất cả chứng chỉ liên quan không?")
                    .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xóa sinh viên
                            studentRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        // Cascade delete chứng chỉ
                                        DatabaseReference certRef = FirebaseHelper.getReference("Certificates");
                                        certRef.orderByChild("studentID")
                                                .equalTo(studentId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snap) {
                                                        for (DataSnapshot c : snap.getChildren()) {
                                                            certRef.child(c.getKey()).removeValue();
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError err) { /* ignore */ }
                                                });

                                        Toast.makeText(StudentDetailActivity.this,
                                                "Xóa sinh viên và chứng chỉ liên quan thành công",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(StudentDetailActivity.this,
                                                    "Xóa thất bại: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // 7. Xem Certificate của student này
        btnCertificate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CertificateManagementActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("studentId",   studentId);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Student Detail");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStudentDetail() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Student s = snap.getValue(Student.class);
                if (s != null) {
                    edtID.setText(snap.getKey());
                    edtID.setEnabled(false);
                    edtName.setText(s.getName());
                    edtMajor.setText(s.getMajor());
                    edtClass.setText(s.getClassName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDetailActivity.this,
                        "Lỗi tải dữ liệu: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudent() {
        String name  = edtName.getText().toString().trim();
        String major = edtMajor.getText().toString().trim();
        String cls   = edtClass.getText().toString().trim();
        if (name.isEmpty() || major.isEmpty() || cls.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        studentRef.child("name").setValue(name);
        studentRef.child("major").setValue(major);
        studentRef.child("className").setValue(cls)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
