package com.example.studentmanagementapp.certificate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.Certificate;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

public class CertificateAddActivity extends AppCompatActivity {

    private EditText edtID, edtName, edtIssueDate;
    private Button btnSave;
    private DatabaseReference certRef;
    private String newId;
    private String studentId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_add);
        setupToolbar();

        // 1. Nhận extras
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        studentId   = getIntent().getStringExtra("studentId");
        if (currentUser == null || studentId == null) {
            Toast.makeText(this, "Missing data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Ánh xạ view
        edtID        = findViewById(R.id.edtID);
        edtName      = findViewById(R.id.edtName);
        edtIssueDate = findViewById(R.id.edtIssueDate);
        btnSave      = findViewById(R.id.btnSave);

        // 3. Disable ID và nút Save tạm thời
        edtID.setEnabled(false);
        btnSave.setEnabled(false);

        // 4. Thiết lập DatePicker cho issueDate (DD/MM/YYYY)
        edtIssueDate.setFocusable(false);
        edtIssueDate.setClickable(true);
        edtIssueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (DatePicker view, int year, int month, int day) -> {
                        String formatted = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", day, month + 1, year);
                        edtIssueDate.setText(formatted);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 5. Sinh ID: tìm lỗ hổng trong các key certificateX
        certRef = FirebaseHelper.getReference("Certificates");
        certRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Set<Integer> used = new HashSet<>();
                for (DataSnapshot child : snap.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith("certificate")) {
                        try {
                            int num = Integer.parseInt(key.substring("certificate".length()));
                            used.add(num);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                int idx = 1;
                while (used.contains(idx)) idx++;
                newId = "certificate" + idx;
                edtID.setText(newId);
                btnSave.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CertificateAddActivity.this,
                        "Cannot create ID: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 6. Xử lý nút Add
        btnSave.setOnClickListener(v -> {
            String name  = edtName.getText().toString().trim();
            String date = edtIssueDate.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(date)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo object và lưu lên Firebase
            Certificate certificate = new Certificate(newId, name, studentId, date);
            certRef.child(newId)
                    .setValue(certificate)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Add certificate successfully!", Toast.LENGTH_SHORT).show();
                        finish();  // quay lại list
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Add certificate failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    private void setupToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("Add Certificate");
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
