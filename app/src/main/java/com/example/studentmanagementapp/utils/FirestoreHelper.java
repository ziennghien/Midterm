package com.example.studentmanagementapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirestoreHelper {

    public static void deleteAllDocuments(String collectionName, FirestoreDeleteListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }

                    }
                    listener.onDeleteComplete();
                })
                .addOnFailureListener(listener::onDeleteFailed);
    }

    public interface FirestoreDeleteListener {
        void onDeleteComplete();
        void onDeleteFailed(Exception e);
    }
}
