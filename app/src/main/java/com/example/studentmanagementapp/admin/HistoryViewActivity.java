package com.example.studentmanagementapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagementapp.BaseActivity;
import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.LoginHistoryAdapter;
import com.example.studentmanagementapp.model.LoginHistory;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.*;

import java.util.*;

public class HistoryViewActivity extends BaseActivity {

    private RecyclerView recyclerLoginHistory;
    private LoginHistoryAdapter adapter;
    private List<LoginHistory> historyList = new ArrayList<>();
    private DatabaseReference historyRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);
        setToolbar(R.id.toolbar);

        recyclerLoginHistory = findViewById(R.id.recyclerLoginHistory);
        recyclerLoginHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LoginHistoryAdapter(this, historyList);
        recyclerLoginHistory.setAdapter(adapter);

        historyRef = FirebaseHelper.getLoginHistoryReference();
        usersRef = FirebaseHelper.getUsersReference();

        loadLoginHistory();
    }

    private void loadLoginHistory() {
        historyRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    LoginHistory history = item.getValue(LoginHistory.class);
                    if (history != null) {
                        fetchUserData(history);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryViewActivity.this, "Lỗi tải lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserData(LoginHistory history) {
        usersRef.child(history.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    history.setName(user.getName());
                    history.setUserName(user.getUserName());
                    history.setRole(user.getRole());
                    history.setAvatarUrl(user.getImage());
                }
                historyList.add(0, history); // Thêm vào đầu danh sách
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}