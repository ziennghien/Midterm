package com.example.studentmanagementapp.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagementapp.R;
import com.example.studentmanagementapp.adapters.UserAdapter;
import com.example.studentmanagementapp.models.User;
import com.example.studentmanagementapp.utils.AppConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private FloatingActionButton fabAdd;
    private List<User> userList;
    private UserAdapter adapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        fabAdd = findViewById(R.id.fabAdd);

        firestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, userList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Click user ➔ xem chi tiết
                Intent intent = new Intent(UserListActivity.this, UserDetailActivity.class);
                intent.putExtra("userEmail", user.getEmail());
                startActivity(intent);
            }

            @Override
            public void onUserLongClick(User user) {
                // Long click user ➔ hỏi xóa
                confirmDeleteUser(user);
            }
        });

        recyclerUsers.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            // Click nút + ➔ thêm user mới
            Intent intent = new Intent(UserListActivity.this, UserAddEditActivity.class);
            startActivity(intent);
        });

        loadUsers();
    }

    private void loadUsers() {
        CollectionReference usersRef = firestore.collection(AppConstants.COLLECTION_USERS);

        usersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(UserListActivity.this, "Error loading users", Toast.LENGTH_SHORT).show();
                    return;
                }

                userList.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user " + user.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteUser(User user) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting user...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(user.getEmail())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to delete user!", Toast.LENGTH_SHORT).show();
                });
    }
}
