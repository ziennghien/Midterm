package com.example.studentmanagementapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

    /**
     * Gọi ở onCreate() của Activity con sau khi setContentView().
     * @param toolbarId id của Toolbar trong layout
     */
    protected void setToolbar(int toolbarId) {
        Toolbar toolbar = findViewById(toolbarId);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // Bật nút back nếu Activity không phải là Main
            if (shouldShowBackButton()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    /**
     * Mặc định true, override nếu muốn ẩn nút back.
     */
    protected boolean shouldShowBackButton() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Xử lý khi bấm nút back
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // hoặc finish()
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
