package com.example.studentmanagementapp.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagementapp.BaseActivity;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.StudentAdapter;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddStudent;
    private Button btnImportStudent, btnExportStudent;
    private StudentAdapter adapter;
    private final List<Student> studentList = new ArrayList<>();

    private String role = "employee"; // mặc định
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list); // layout chính
        setToolbar(R.id.toolbar);

        // Nhận role từ Intent nếu có
        role = getIntent().getStringExtra("role");
        if (role == null) role = "employee";

        // Firebase reference
        studentsRef = FirebaseHelper.getReference("Students");

        // Ánh xạ view
        recyclerView = findViewById(R.id.recyclerViewStudents);
        fabAddStudent = findViewById(R.id.fabAddStudent);
        btnImportStudent = findViewById(R.id.btnImportStudent);
        btnExportStudent = findViewById(R.id.btnExportStudent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Student Management");

        // Phân quyền hiển thị nút
        if ("employee".equalsIgnoreCase(role)) {
            fabAddStudent.setVisibility(View.GONE);
            btnImportStudent.setVisibility(View.GONE);
            btnExportStudent.setVisibility(View.GONE);
        }

        // Setup RecyclerView
        adapter = new StudentAdapter(studentList, role);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Thêm sinh viên
        fabAddStudent.setOnClickListener(view -> {
            Intent intent = new Intent(this, StudentAddEditActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        // Import CSV
        btnImportStudent.setOnClickListener(view ->
                Toast.makeText(this, "Import CSV (chưa xử lý)", Toast.LENGTH_SHORT).show());

        // Export CSV
        btnExportStudent.setOnClickListener(view ->
                Toast.makeText(this, "Export CSV (chưa xử lý)", Toast.LENGTH_SHORT).show());

        // Load danh sách từ Firebase
        loadStudentList();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadStudentList() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Student student = snap.getValue(Student.class);
                    if (student != null) {
                        student.setId(snap.getKey()); // gán id = key của node
                        // Ghi ra log
                        Log.d("StudentID", "Loaded student ID: " + student.getId());
                        studentList.add(student);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentManagementActivity.this, "Lỗi tải dữ liệu sinh viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
