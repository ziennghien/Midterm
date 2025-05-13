package com.example.studentmanagementapp.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CSVHelper {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // ========== STUDENT ==========
    public static void importStudents(Context context, Uri fileUri, ImportCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Student> studentList = new ArrayList<>();
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                // Bỏ dòng header
                if (isFirstLine) {
                    isFirstLine = false;
                    // Kiểm tra đúng định dạng cột
                    if (columns.length != 4 ||
                            !columns[0].trim().equalsIgnoreCase("id") ||
                            !columns[1].trim().equalsIgnoreCase("name") ||
                            !columns[2].trim().equalsIgnoreCase("birthdate") ||
                            !columns[3].trim().equalsIgnoreCase("major")) {
                        callback.onFailure("Invalid CSV format for Students!");
                        return;
                    }
                    continue;
                }

                if (columns.length != 4) {
                    callback.onFailure("Each student row must have exactly 4 columns!");
                    return;
                }

                String id = columns[0].trim();
                String name = columns[1].trim();
                Date birthdate = sdf.parse(columns[2].trim());
                String major = columns[3].trim();

                studentList.add(new Student(id, name, birthdate, major));
            }

            reader.close();

            // Thay thế toàn bộ danh sách
            FirebaseFirestore.getInstance()
                    .collection("students")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (var doc : querySnapshot.getDocuments()) {
                            doc.getReference().delete();
                        }

                        for (Student student : studentList) {
                            FirebaseFirestore.getInstance()
                                    .collection("students")
                                    .document(student.getId())
                                    .set(student);
                        }

                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));

        } catch (Exception e) {
            callback.onFailure("Import error: " + e.getMessage());
        }
    }

    public static void exportStudents(Context context, Uri fileUri) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    context.getContentResolver().openOutputStream(fileUri)));

            writer.write("id,name,birthdate,major\n");

            FirebaseFirestore.getInstance()
                    .collection("students")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        try {
                            for (var doc : querySnapshot.getDocuments()) {
                                Student student = doc.toObject(Student.class);
                                if (student != null) {
                                    writer.write(student.getId() + "," +
                                            student.getName() + "," +
                                            sdf.format(student.getBirthdate()) + "," +
                                            student.getMajor() + "\n");
                                }
                            }
                            writer.flush();
                            writer.close();
                            Toast.makeText(context, "Export students successful!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ========== CERTIFICATE ==========
    public static void importCertificates(Context context, Uri fileUri, ImportCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            List<Certificate> certificateList = new ArrayList<>();
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                // Bỏ dòng header
                if (isFirstLine) {
                    isFirstLine = false;
                    if (columns.length != 6 ||
                            !columns[0].trim().equalsIgnoreCase("certificateNumber") ||
                            !columns[1].trim().equalsIgnoreCase("certificateName") ||
                            !columns[2].trim().equalsIgnoreCase("issueDate") ||
                            !columns[3].trim().equalsIgnoreCase("issuer") ||
                            !columns[4].trim().equalsIgnoreCase("place") ||
                            !columns[5].trim().equalsIgnoreCase("studentId")) {
                        callback.onFailure("Invalid CSV format for Certificates!");
                        return;
                    }
                    continue;
                }

                if (columns.length != 6) {
                    callback.onFailure("Each certificate row must have exactly 6 columns!");
                    return;
                }

                String certNum = columns[0].trim();
                String certName = columns[1].trim();
                Date issueDate = sdf.parse(columns[2].trim());
                String issuer = columns[3].trim();
                String place = columns[4].trim();
                String studentId = columns[5].trim();

                certificateList.add(new Certificate(certNum, certName, issueDate, issuer, place, studentId));
            }

            reader.close();

            FirebaseFirestore.getInstance()
                    .collection("certificates")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (var doc : querySnapshot.getDocuments()) {
                            doc.getReference().delete();
                        }

                        for (Certificate cert : certificateList) {
                            FirebaseFirestore.getInstance()
                                    .collection("certificates")
                                    .document(cert.getCertificateNumber())
                                    .set(cert);
                        }

                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));

        } catch (Exception e) {
            callback.onFailure("Import error: " + e.getMessage());
        }
    }

    public static void exportCertificates(Context context, Uri fileUri) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    context.getContentResolver().openOutputStream(fileUri)));

            writer.write("certificateNumber,certificateName,issueDate,issuer,place,studentId\n");

            FirebaseFirestore.getInstance()
                    .collection("certificates")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        try {
                            for (var doc : querySnapshot.getDocuments()) {
                                Certificate cert = doc.toObject(Certificate.class);
                                if (cert != null) {
                                    writer.write(cert.getCertificateNumber() + "," +
                                            cert.getCertificateName() + "," +
                                            sdf.format(cert.getIssueDate()) + "," +
                                            cert.getIssuer() + "," +
                                            cert.getPlace() + "," +
                                            cert.getStudentId() + "\n");
                                }
                            }
                            writer.flush();
                            writer.close();
                            Toast.makeText(context, "Export certificates successful!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Callback cho Import
    public interface ImportCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
