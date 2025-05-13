package com.example.studentmanagementapp.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.*;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.StudentAdapter;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StudentManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private MaterialButton btnImport, btnExport;
    private StudentAdapter adapter;
    private List<Student> studentList = new ArrayList<>();

    private String role = "employee"; // mặc định
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list); // layout bạn đã gửi

        // Nhận role từ Intent
        role = getIntent().getStringExtra("role");
        if (role == null) role = "employee";

        // Khởi tạo Firebase
        studentsRef = FirebaseHelper.getReference("Students");

        // Ánh xạ view
        recyclerView = findViewById(R.id.recyclerStudents);
        fabAdd = findViewById(R.id.fabAdd);
        btnImport = findViewById(R.id.btnImport);
        btnExport = findViewById(R.id.btnExport);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Student Management");

        // Phân quyền: chỉ `admin` và `manager` được thêm, nhập/xuất
        if ("employee".equals(role)) {
            fabAdd.setVisibility(View.GONE);
            btnImport.setVisibility(View.GONE);
            btnExport.setVisibility(View.GONE);
        }

        // RecyclerView setup
        adapter = new StudentAdapter(studentList, role); // truyền role vào adapter để ẩn/hiện nút hành động
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Sự kiện nút thêm sinh viên
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(this, StudentAddEditActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        // Sự kiện nhập/xuất CSV
        btnImport.setOnClickListener(view -> {
            // TODO: viết logic import từ CSV
            Toast.makeText(this, "Import CSV (chưa xử lý)", Toast.LENGTH_SHORT).show();
        });

        btnExport.setOnClickListener(view -> {
            // TODO: viết logic export ra CSV
            Toast.makeText(this, "Export CSV (chưa xử lý)", Toast.LENGTH_SHORT).show();
        });

        loadStudentList();
    }

    private void loadStudentList() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Student student = snap.getValue(Student.class);
                    if (student != null) {
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
