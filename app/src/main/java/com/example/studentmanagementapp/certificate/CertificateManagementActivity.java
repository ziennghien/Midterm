package com.example.studentmanagementapp.certificate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagementapp.BaseActivity;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.CertificateAdapter;
import com.example.studentmanagementapp.model.Certificate;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CertificateManagementActivity extends BaseActivity {
    private TextView tvCertCount;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private final List<Certificate> list = new ArrayList<>();
    private CertificateAdapter adapter;
    private User currentUser;
    private String studentId;
    private DatabaseReference certRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_list);
        setToolbar(R.id.toolbar);

        // 1. Nhận extras từ Intent
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        studentId   = getIntent().getStringExtra("studentId");
        if (currentUser == null || studentId == null) {
            Toast.makeText(this, "Missing data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Khởi tạo Firebase ref và ánh xạ view
        certRef      = FirebaseHelper.getReference("Certificates");
        tvCertCount  = findViewById(R.id.tvCertCount);
        recyclerView = findViewById(R.id.recyclerViewCertificates);
        fabAdd       = findViewById(R.id.fabAddStudent);

        // 3. Phân quyền: chỉ admin/manager mới thấy Add/Import/Export
        String role = currentUser.getRole();
        if ("admin".equalsIgnoreCase(role) || "manager".equalsIgnoreCase(role)) {
            fabAdd.setVisibility(View.VISIBLE);

            fabAdd.setOnClickListener(v -> {
                Intent i = new Intent(this, CertificateAddActivity.class);
                i.putExtra("currentUser", currentUser);
                i.putExtra("studentId",   studentId);
                startActivity(i);
            });
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // 4. Setup RecyclerView + Adapter
        adapter = new CertificateAdapter(list, currentUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 5. Query chỉ những chứng chỉ có studentId này
        certRef
                .orderByChild("studentId")
                .equalTo(studentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        list.clear();
                        for (DataSnapshot c : snap.getChildren()) {
                            Certificate cert = c.getValue(Certificate.class);
                            if (cert != null) {
                                cert.setId(c.getKey());
                                list.add(cert);
                            }
                        }
                        // Cập nhật số lượng
                        tvCertCount.setText("Certificates: " + list.size());
                        // Cập nhật danh sách
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        Toast.makeText(CertificateManagementActivity.this,
                                "Failed to load: " + err.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
