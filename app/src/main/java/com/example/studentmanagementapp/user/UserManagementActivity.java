package com.example.studentmanagementapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studentmanagementapp.BaseActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.*;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapter.UserAdapter;
import com.example.studentmanagementapp.model.User;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends BaseActivity {

    private RecyclerView recyclerViewUsers;
    private FloatingActionButton fabAddUser;

    private final List<User> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list); // layout bạn đã gửi

        setToolbar(R.id.toolbar);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        fabAddUser = findViewById(R.id.fabAddUser);

        usersRef = FirebaseHelper.getReference("Users");

        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditUserActivity.class);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        loadUsers();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    if (user != null) {
                        user.setId(snap.getKey()); // ⚠️ Quan trọng: gán ID từ key
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
