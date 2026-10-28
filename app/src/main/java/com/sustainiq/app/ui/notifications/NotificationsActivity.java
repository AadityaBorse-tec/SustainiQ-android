package com.sustainiq.app.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.sustainiq.app.R;
import com.sustainiq.app.data.db.AppDatabase;
import com.sustainiq.app.data.db.NotificationEntity;
import com.sustainiq.app.ui.reports.ReportDetailActivity;

import java.util.concurrent.Executors;

public class NotificationsActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rv;
    private NotificationAdapter adapter;
    private LinearLayout layoutEmpty;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = AppDatabase.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_notifications);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setNavigationIconTint(getResources().getColor(R.color.text_primary, getTheme()));

        TextView btnMarkAll = findViewById(R.id.btn_mark_all_read);
        layoutEmpty = findViewById(R.id.layout_notif_empty);
        rv = findViewById(R.id.rv_notifications);

        adapter = new NotificationAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rv.setAdapter(adapter);

        btnMarkAll.setOnClickListener(v ->
                Executors.newSingleThreadExecutor().execute(() -> db.notificationDao().markAllRead()));

        observeNotifications();
    }

    private void observeNotifications() {
        db.notificationDao().getAll().observe(this, notifications -> {
            adapter.setItems(notifications);
            boolean empty = notifications == null || notifications.isEmpty();
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onClick(NotificationEntity n) {
        // Mark as read
        Executors.newSingleThreadExecutor().execute(() -> db.notificationDao().markRead(n.id));

        // Navigate to related report if applicable
        if (n.relatedReportId != null && !n.relatedReportId.isEmpty()) {
            Intent intent = new Intent(this, ReportDetailActivity.class);
            intent.putExtra(ReportDetailActivity.EXTRA_REPORT_ID, n.relatedReportId);
            startActivity(intent);
        }
    }
}
