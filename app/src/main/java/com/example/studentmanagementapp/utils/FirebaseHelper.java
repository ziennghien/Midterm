package com.example.studentmanagementapp.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    // URL đúng với vùng của Realtime Database
    private static final String DATABASE_URL = "https://studentmanagementapp-e34ff-default-rtdb.asia-southeast1.firebasedatabase.app";

    // Trả về Database instance
    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
    }

    // Trả về reference đến "Users"
    public static DatabaseReference getUsersReference() {
        return getDatabase().getReference("Users");
    }

    // Trả về reference đến "Students"
    public static DatabaseReference getStudentsReference() {
        return getDatabase().getReference("Students");
    }

    // Tùy chọn: ref đến bất kỳ nhánh nào
    public static DatabaseReference getReference(String path) {
        return getDatabase().getReference(path);
    }
}
