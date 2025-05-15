package com.example.studentmanagementapp.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.studentmanagementapp.model.Certificate;
import com.example.studentmanagementapp.model.Student;
import com.example.studentmanagementapp.utils.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CSVHelper hỗ trợ:
 *  - importStudents: đọc CSV sinh viên, validate, replace/add từng bản ghi.
 *  - importCertificates: đọc CSV chứng chỉ, validate, replace/add từng bản ghi.
 *  - exportStudentsToDownloads / exportCertificatesToDownloads: tạo file CSV trong thư mục app-specific Downloads.
 */
public class CSVHelper {
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    public interface ImportCallback {
        void onSuccess();
        void onError(String message);
    }

    // ==== IMPORT SINH VIÊN: chỉ replace/add, không xóa tất cả ====
    public static void importStudents(@NonNull InputStream is,
                                      @NonNull DatabaseReference studentsRef,
                                      @NonNull ImportCallback callback) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Map<String, Student> fileMap = new LinkedHashMap<>();
            String line; int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                String[] cols = line.split(",", -1);
                if (cols.length != 4) {
                    callback.onError("Dòng " + row + " không đủ 4 cột");
                    return;
                }
                String id    = cols[0].trim();
                String name  = cols[1].trim();
                String major = cols[2].trim();
                String cls   = cols[3].trim();

                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)
                        || TextUtils.isEmpty(major) || TextUtils.isEmpty(cls)) {
                    callback.onError("Dòng " + row + " có trường rỗng");
                    return;
                }
                if (!id.matches("^student\\d+$")) {
                    callback.onError("Dòng " + row + " ID không đúng định dạng studentX: " + id);
                    return;
                }
                if (fileMap.containsKey(id)) {
                    callback.onError("ID trùng dòng " + row + ": " + id);
                    return;
                }

                fileMap.put(id, new Student(id, name, major, cls));
            }

            // Với mỗi bản ghi trong fileMap, replace hoặc thêm mới
            for (Student s : fileMap.values()) {
                studentsRef.child(s.getId()).setValue(s);
            }
            callback.onSuccess();

        } catch (Exception ex) {
            callback.onError("Lỗi đọc CSV: " + ex.getMessage());
        }
    }

    // ==== IMPORT CHỨNG CHỈ: chỉ replace/add, không xóa tất cả ====
    public static void importCertificates(@NonNull InputStream is,
                                          @NonNull DatabaseReference certRef,
                                          @NonNull Set<String> validStudentIds,
                                          @NonNull ImportCallback callback) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        sdf.setLenient(false);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Map<String, Certificate> fileMap = new LinkedHashMap<>();
            String line; int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                String[] cols = line.split(",", -1);
                if (cols.length != 4) {
                    callback.onError("Dòng " + row + " không đủ 4 cột");
                    return;
                }
                String id        = cols[0].trim();
                String name      = cols[1].trim();
                String dateStr   = cols[2].trim();
                String studentId = cols[3].trim();

                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)
                        || TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(studentId)) {
                    callback.onError("Dòng " + row + " có trường rỗng");
                    return;
                }
                if (!id.matches("^certificate\\d+$")) {
                    callback.onError("Dòng " + row + " ID không đúng định dạng certificateX: " + id);
                    return;
                }
                if (fileMap.containsKey(id)) {
                    callback.onError("ID trùng dòng " + row + ": " + id);
                    return;
                }
                try {
                    sdf.parse(dateStr);
                } catch (ParseException pe) {
                    callback.onError("Dòng " + row + " ngày không đúng định dạng DD/MM/YYYY");
                    return;
                }
                if (!validStudentIds.contains(studentId)) {
                    callback.onError("Dòng " + row + " studentId không tồn tại: " + studentId);
                    return;
                }

                Certificate c = new Certificate(id, name, studentId, dateStr);
                fileMap.put(id, c);
            }

            // Với mỗi chứng chỉ trong fileMap, replace hoặc thêm mới
            for (Certificate c : fileMap.values()) {
                certRef.child(c.getId()).setValue(c);
            }
            callback.onSuccess();

        } catch (Exception ex) {
            callback.onError("Lỗi đọc CSV: " + ex.getMessage());
        }
    }

    // ==== EXPORT SINH VIÊN → Thư mục app-specific Downloads ====
    public static File exportStudentsToDownloads(@NonNull Context ctx,
                                                 @NonNull List<Student> students,
                                                 @NonNull String fileName) throws Exception {
        File downloads = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloads != null && !downloads.exists()) downloads.mkdirs();
        File csv = new File(downloads, fileName);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(csv))) {
            for (Student s : students) {
                w.write(String.join(",",
                        s.getId(),
                        s.getName(),
                        s.getMajor(),
                        s.getClassName()));
                w.newLine();
            }
        }
        return csv;
    }

    // ==== EXPORT CHỨNG CHỈ → Thư mục app-specific Downloads ====
    public static File exportCertificatesToDownloads(@NonNull Context ctx,
                                                     @NonNull List<Certificate> certs,
                                                     @NonNull String fileName) throws Exception {
        File downloads = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloads != null && !downloads.exists()) downloads.mkdirs();
        File csv = new File(downloads, fileName);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(csv))) {
            for (Certificate c : certs) {
                w.write(String.join(",",
                        c.getId(),
                        c.getName(),
                        c.getIssueDate(),
                        c.getStudentId()));
                w.newLine();
            }
        }
        return csv;
    }
}
