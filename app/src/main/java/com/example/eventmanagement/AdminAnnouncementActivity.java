package com.example.eventmanagement;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminAnnouncementActivity extends AppCompatActivity {

    EditText etTitle, etMessage;
    Spinner spinnerEvent, spinnerType;
    ImageView btnBack;

    DatabaseReference eventsRef, registrationsRef, notificationsRef, announcementsRef;

    List<String> eventNames = new ArrayList<>();
    List<String> eventIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_announcement);

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        spinnerEvent = findViewById(R.id.spinnerEvent);
        spinnerType = findViewById(R.id.spinnerType);
        btnBack = findViewById(R.id.btnBack);

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        registrationsRef = FirebaseDatabase.getInstance().getReference("EventRegistrations");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        announcementsRef = FirebaseDatabase.getInstance().getReference("Announcements");

        btnBack.setOnClickListener(v -> finish());

        setupTypeSpinner();
        loadEvents();

        findViewById(R.id.btnPublish).setOnClickListener(v -> publish());
    }

    private void setupTypeSpinner() {
        String[] types = {
                "EVENT_UPDATE",
                "REGISTRATION_CONFIRMED",
                "EMAIL_SENT",
                "PAYMENT_SENT",
                "EVENT_REMINDER",
                "EVENT_CANCELLED"
        };

        spinnerType.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        types)
        );
    }

    private void loadEvents() {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventNames.clear();
                eventIds.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    eventIds.add(ds.getKey());
                    eventNames.add(ds.child("eventName").getValue(String.class));
                }

                spinnerEvent.setAdapter(
                        new ArrayAdapter<>(AdminAnnouncementActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                eventNames)
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void publish() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        int pos = spinnerEvent.getSelectedItemPosition();

        if (title.isEmpty() || message.isEmpty() || pos < 0) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = eventIds.get(pos);
        long time = System.currentTimeMillis();

        // 1) Save the announcement itself (for history/auditing)
        HashMap<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("type", type);
        announcement.put("eventId", eventId);
        announcement.put("timestamp", time);
        announcementsRef.push().setValue(announcement);

        // 2) Deliver notifications to all registered students (individuals + group leaders)
        registrationsRef.child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        java.util.HashSet<String> recipients = new java.util.HashSet<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String key = ds.getKey();
                            if (key == null) continue;

                            if ("groups".equals(key)) {
                                for (DataSnapshot g : ds.getChildren()) {
                                    String leaderRoll = g.child("leaderRoll").getValue(String.class);
                                    if (leaderRoll != null && !leaderRoll.trim().isEmpty()) {
                                        recipients.add(leaderRoll.trim());
                                    }

                                    // If group members have roll saved, include them as well
                                    DataSnapshot members = g.child("members");
                                    for (DataSnapshot m : members.getChildren()) {
                                        String memberRoll = m.child("roll").getValue(String.class);
                                        if (memberRoll != null && !memberRoll.trim().isEmpty()) {
                                            recipients.add(memberRoll.trim());
                                        }
                                    }
                                }
                            } else {
                                // Individual registration: key is the student's roll
                                recipients.add(key);
                            }
                        }

                        HashMap<String, Object> notif = new HashMap<>();
                        notif.put("title", title);
                        notif.put("message", message);
                        notif.put("type", type);
                        notif.put("eventId", eventId);
                        notif.put("timestamp", time);
                        notif.put("read", false);

                        for (String studentKey : recipients) {
                            notificationsRef.child(studentKey).push().setValue(notif);
                        }

                        Toast.makeText(AdminAnnouncementActivity.this,
                                "Announcement sent", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
