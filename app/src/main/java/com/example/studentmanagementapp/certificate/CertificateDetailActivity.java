package com.example.studentmanagementapp.certificate;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.Certificate;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.Locale;

public class CertificateDetailActivity extends AppCompatActivity {

    private EditText edtID, edtName, edtIssueDate;
    private Button btnSave, btnDelete;
    private DatabaseReference certRef;
    private String certId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);
        setupToolbar();

        // 1. Nhận extras
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        certId      = getIntent().getStringExtra("certificateId");
        if (currentUser == null || certId == null) {
            Toast.makeText(this, "Missing data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Ánh xạ view
        edtID        = findViewById(R.id.edtID);
        edtName      = findViewById(R.id.edtName);
        edtIssueDate = findViewById(R.id.edtIssueDate);
        btnSave      = findViewById(R.id.btnSave);
        btnDelete    = findViewById(R.id.btnDelete);

        // 3. Phân quyền
        String role = currentUser.getRole();
        if (!("admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role))) {
            btnSave.setVisibility(Button.GONE);
            btnDelete.setVisibility(Button.GONE);
            edtName.setEnabled(false);
            edtIssueDate.setEnabled(false);
        } else {
            btnSave.setVisibility(Button.VISIBLE);
            btnDelete.setVisibility(Button.VISIBLE);
            edtName.setEnabled(true);
            edtIssueDate.setEnabled(true);
        }

        // 4. DatePicker cho issueDate (DD/MM/YYYY)
        edtIssueDate.setFocusable(false);
        edtIssueDate.setClickable(true);
        edtIssueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        String formatted = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        edtIssueDate.setText(formatted);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 5. Khởi tạo ref và load dữ liệu
        certRef = FirebaseHelper.getReference("Certificates").child(certId);
        loadDetail();

        // 6. Lưu
        btnSave.setOnClickListener(v -> update());

        // 7. Xóa với dialog xác nhận
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this certificate?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            certRef.removeValue()
                                    .addOnSuccessListener(a -> {
                                        Toast.makeText(CertificateDetailActivity.this,
                                                "Deleted successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(CertificateDetailActivity.this,
                                                    "Delete failed: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void setupToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("Certificate Detail");
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem it) {
        if (it.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(it);
    }

    private void loadDetail() {
        certRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Certificate c = snap.getValue(Certificate.class);
                if (c != null) {
                    edtID.setText(snap.getKey());
                    edtID.setEnabled(false);
                    edtName.setText(c.getName());
                    edtIssueDate.setText(c.getIssueDate());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(CertificateDetailActivity.this,
                        "Failed to load: " + err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void update() {
        String name = edtName.getText().toString().trim();
        String date = edtIssueDate.getText().toString().trim();
        if (name.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        certRef.child("name").setValue(name);
        certRef.child("issueDate").setValue(date)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
