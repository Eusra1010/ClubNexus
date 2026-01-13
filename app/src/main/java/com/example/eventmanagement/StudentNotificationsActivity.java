package com.example.eventmanagement;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private com.google.android.material.chip.ChipGroup filterGroup;
    private com.google.android.material.chip.Chip chipAll, chipUnread;
    private TextView btnMarkAllRead;
    private NotificationAdapter adapter;
    private final List<Notification> notifications = new ArrayList<>();
    private final List<Notification> sourceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_notifications);

        recyclerView = findViewById(R.id.notificationRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        filterGroup = findViewById(R.id.filterGroup);
        chipAll = findViewById(R.id.chipAll);
        chipUnread = findViewById(R.id.chipUnread);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        ImageView back = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications, this::markRead);
        recyclerView.setAdapter(adapter);

        back.setOnClickListener(v -> finish());

        String studentKey = getIntent().getStringExtra("studentKey");

        if (studentKey == null || studentKey.trim().isEmpty()) {
            Toast.makeText(this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("NOTIF", "Reading: Notifications/" + studentKey);
        loadNotifications(studentKey);

        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());
        btnMarkAllRead.setOnClickListener(v -> markAllRead(studentKey));
    }

    private void loadNotifications(String studentKey) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(studentKey);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                sourceList.clear();
                notifications.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notification n = ds.getValue(Notification.class);
                    if (n != null) {
                        n.setNotificationId(ds.getKey());
                        sourceList.add(n);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyFilter() {
        notifications.clear();
        boolean unreadOnly = filterGroup.getCheckedChipId() == R.id.chipUnread;
        for (Notification n : sourceList) {
            if (!unreadOnly || !n.isRead()) {
                notifications.add(n);
            }
        }
        adapter.notifyDataSetChanged();
        emptyText.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void markRead(Notification n) {
        String studentKey = getIntent().getStringExtra("studentKey");
        if (studentKey == null || n.getNotificationId() == null) return;
        FirebaseDatabase.getInstance().getReference("Notifications")
                .child(studentKey)
                .child(n.getNotificationId())
                .child("read")
                .setValue(true);
    }

    private void markAllRead(String studentKey) {
        FirebaseDatabase.getInstance().getReference("Notifications")
                .child(studentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().child("read").setValue(true);
                        }
                        Toast.makeText(StudentNotificationsActivity.this, "All marked read", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
