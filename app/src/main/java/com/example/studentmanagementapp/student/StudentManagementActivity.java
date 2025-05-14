package com.example.studentmanagementapp.student;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagementapp.BaseActivity;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.StudentAdapter;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.CSVHelper;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class StudentManagementActivity extends BaseActivity {
    private static final int REQUEST_PICK_CSV = 1001;

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private Button btnImport, btnExport;
    private SearchView searchView;

    private final List<Student> masterList  = new ArrayList<>();
    private final List<Student> displayList = new ArrayList<>();
    private StudentAdapter adapter;

    private User currentUser;
    private DatabaseReference studentsRef, certRef;
    private int csvMode = 0; // 0 = student, 1 = certificate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        // Toolbar + nút back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Student Management");

        // Lấy currentUser
        currentUser = (User)getIntent().getSerializableExtra("currentUser");
        if (currentUser==null) {
            Toast.makeText(this,"Missing data!",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo refs & views
        studentsRef = FirebaseHelper.getReference("Students");
        certRef     = FirebaseHelper.getReference("Certificates");
        recyclerView= findViewById(R.id.recyclerViewStudents);
        fabAdd      = findViewById(R.id.fabAddStudent);
        btnImport   = findViewById(R.id.btnImportStudent);
        btnExport   = findViewById(R.id.btnExportStudent);
        searchView  = findViewById(R.id.searchView);

        // Phân quyền nút Add
        String role = currentUser.getRole();
        if ("admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role)) {
            fabAdd.setOnClickListener(v -> {
                Intent i = new Intent(this, StudentAddActivity.class);
                i.putExtra("mode","add");
                i.putExtra("currentUser",currentUser);
                startActivity(i);
            });
        } else {
            fabAdd.setVisibility(FloatingActionButton.GONE);
        }

        // Import/Export CSV
        btnImport.setOnClickListener(v -> showCsvDialog(true));
        btnExport.setOnClickListener(v -> showCsvDialog(false));

        // Setup Recycler + Adapter
        adapter = new StudentAdapter(displayList, currentUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Live search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override public boolean onQueryTextSubmit(String q){ return false; }
            @Override public boolean onQueryTextChange(String q){
                filter(q); return true;
            }
        });

        // Load dữ liệu
        loadStudentList();
    }

    private void showCsvDialog(boolean isImport) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(isImport ? "Import CSV" : "Export CSV")
                .setItems(new String[]{"Student","Certificate","Cancel"}, (d, which) -> {
                    if (which==0) {
                        csvMode = 0;
                        if (isImport) pickCsvFile();
                        else exportStudentsToDownloads();
                    } else if (which==1) {
                        csvMode = 1;
                        if (isImport) pickCsvFile();
                        else exportCertificatesToDownloads();
                    }
                })
                .show();
    }

    private void pickCsvFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("text/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, REQUEST_PICK_CSV);
    }

    @Override
    protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if (req==REQUEST_PICK_CSV && res==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri = data.getData();
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                if (csvMode==0) {
                    CSVHelper.importStudents(is, studentsRef, new CSVHelper.ImportCallback(){
                        @Override public void onSuccess(){
                            Toast.makeText(StudentManagementActivity.this,
                                    "Import student successfully",Toast.LENGTH_SHORT).show();
                            loadStudentList();
                        }
                        @Override public void onError(String msg){
                            Toast.makeText(StudentManagementActivity.this,
                                    "Import error: "+msg,Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Set<String> validIds = masterList.stream()
                            .map(Student::getId)
                            .collect(Collectors.toSet());
                    CSVHelper.importCertificates(is, certRef, validIds, new CSVHelper.ImportCallback(){
                        @Override public void onSuccess(){
                            Toast.makeText(StudentManagementActivity.this,
                                    "Import certificates successfully",Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String msg){
                            Toast.makeText(StudentManagementActivity.this,
                                    "Import error: "+msg,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch(Exception ex){
                Toast.makeText(this,
                        "Cannot open file: "+ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exportStudentsToDownloads() {
        try {
            File out = CSVHelper.exportStudentsToDownloads(this, masterList, "students.csv");
            Toast.makeText(this,
                    "Saved to: "+ out.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch(Exception ex){
            Toast.makeText(this,
                    "Export thất bại: "+ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void exportCertificatesToDownloads() {
        certRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot snap){
                List<com.example.studentmanagementapp.model.Certificate> list = new ArrayList<>();
                for (DataSnapshot c : snap.getChildren()) {
                    com.example.studentmanagementapp.model.Certificate cert =
                            c.getValue(com.example.studentmanagementapp.model.Certificate.class);
                    if (cert!=null) {
                        cert.setId(c.getKey());
                        list.add(cert);
                    }
                }
                try {
                    File out = CSVHelper.exportCertificatesToDownloads(
                            StudentManagementActivity.this, list, "certificates.csv");
                    Toast.makeText(StudentManagementActivity.this,
                            "Saved to: "+out.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                } catch(Exception ex){
                    Toast.makeText(StudentManagementActivity.this,
                            "Export thất bại: "+ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_sort_id) {
            sortBy(Comparator.comparing(Student::getId));
            return true;
        } else if (id == R.id.action_sort_name) {
            sortBy(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
            return true;
        } else if (id == R.id.action_sort_major) {
            sortBy(Comparator.comparing(Student::getMajor, String.CASE_INSENSITIVE_ORDER));
            return true;
        } else if (id == R.id.action_sort_class) {
            sortBy(Comparator.comparing(Student::getClassName, String.CASE_INSENSITIVE_ORDER));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadStudentList() {
        studentsRef.addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot snap){
                masterList.clear();
                for (DataSnapshot c : snap.getChildren()) {
                    Student s = c.getValue(Student.class);
                    if (s!=null) {
                        s.setId(c.getKey());
                        masterList.add(s);
                    }
                }
                displayList.clear();
                displayList.addAll(masterList);
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }

    private void filter(String q) {
        String query = q.trim().toLowerCase();
        displayList.clear();
        if (query.isEmpty()) {
            displayList.addAll(masterList);
        } else {
            for (Student s : masterList) {
                String combined = (s.getId()+" "+s.getName()+" "+
                        s.getMajor()+" "+s.getClassName())
                        .toLowerCase();
                if (combined.contains(query)) displayList.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void sortBy(Comparator<Student> cmp) {
        Collections.sort(displayList, cmp);
        adapter.notifyDataSetChanged();
    }
}
