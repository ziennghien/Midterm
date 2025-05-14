// StudentAddActivity.java
package com.example.studentmanagementapp.student;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StudentAddActivity extends AppCompatActivity {

    private EditText edtID, edtName, edtMajor, edtClass;
    private Button btnAdd;
    private DatabaseReference studentsRef;
    private String newId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_add);
        setupToolbar();

        // 1. Tham chiếu tới node "Students"
        studentsRef = FirebaseHelper.getReference("Students");

        // 2. Ánh xạ view
        edtID    = findViewById(R.id.edtID);
        edtName  = findViewById(R.id.edtName);
        edtMajor = findViewById(R.id.edtMajor);
        edtClass = findViewById(R.id.edtClass);
        btnAdd   = findViewById(R.id.btnSave);

        // 3. Ban đầu disable nút thêm cho đến khi có newId
        btnAdd.setEnabled(false);

        // 4. Lấy số lượng con của "Students" để sinh newId = "student"+(count+1)
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 1. Thu thập tất cả số thứ tự đã dùng
                Set<Integer> used = new HashSet<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String key = snap.getKey();            // ví dụ "student2"
                    if (key != null && key.startsWith("student")) {
                        try {
                            int num = Integer.parseInt(key.substring("student".length()));
                            used.add(num);
                        } catch (NumberFormatException ignore) {}
                    }
                }

                // 2. Tìm smallest missing positive
                int next = 1;
                while (used.contains(next)) {
                    next++;
                }

                // 3. Gán newId và hiển thị
                newId = "student" + next;
                edtID.setText(newId);
                edtID.setEnabled(false);
                btnAdd.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentAddActivity.this,
                        "Không thể sinh ID: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        // 5. Xử lý nút Save
        btnAdd.setOnClickListener(v -> {
            String name  = edtName.getText().toString().trim();
            String major = edtMajor.getText().toString().trim();
            String cls   = edtClass.getText().toString().trim();

            // Validate
            if (TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(major) ||
                    TextUtils.isEmpty(cls)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo object và lưu lên Firebase
            Student student = new Student(newId, name, major, cls);
            studentsRef.child(newId)
                    .setValue(student)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Add student successfully!", Toast.LENGTH_SHORT).show();
                        finish();  // quay lại list
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Add student failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Student");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
