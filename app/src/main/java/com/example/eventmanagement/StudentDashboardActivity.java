package com.example.eventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class StudentDashboardActivity extends AppCompatActivity {

    RecyclerView rvEvents;
    TextView btnHome, tvViewAll, tvBadgeCount, tvBellText;
    EditText etSearch;
    ImageView btnNotifications;
    LinearLayout cardMyRegistrations;

    DatabaseReference eventRef;
    ArrayList<Event> eventList;
    ArrayList<Event> allEvents;
    EventAdapter eventAdapter;

    String studentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // 🔑 SESSION
        studentKey = getIntent().getStringExtra("studentKey");

        if (studentKey == null || studentKey.isEmpty()) {
            Toast.makeText(this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d("DASH", "studentKey=" + studentKey);

        // UI
        rvEvents = findViewById(R.id.rvEvents);
        btnHome = findViewById(R.id.btnHome);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvViewAll = findViewById(R.id.tvViewAll);
        etSearch = findViewById(R.id.etSearch);
        cardMyRegistrations = findViewById(R.id.cardMyRegistrations);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        tvBellText = findViewById(R.id.tvBellText);

        // EVENTS (HORIZONTAL)
        rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        eventList = new ArrayList<>();
        allEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList, studentKey);
        rvEvents.setAdapter(eventAdapter);

        eventRef = FirebaseDatabase.getInstance().getReference("Events");

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                eventList.clear();
                int count = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (count == 8) break;
                    Event e = ds.getValue(Event.class);
                    if (e != null) {
                        allEvents.add(e);
                        eventList.add(e);
                        count++;
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });


        listenForUnreadNotifications();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });


        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });


        btnNotifications.setOnClickListener(v -> {
            Intent i = new Intent(this, StudentNotificationsActivity.class);
            i.putExtra("studentKey", studentKey);
            startActivity(i);
        });

        // VIEW ALL EVENTS
        tvViewAll.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewEventsActivity.class);
            i.putExtra("studentKey", studentKey);
            startActivity(i);
        });

        // MY REGISTRATIONS
        cardMyRegistrations.setOnClickListener(v -> {
            Intent i = new Intent(this, MyRegistrationsActivity.class);
            i.putExtra("studentKey", studentKey);
            startActivity(i);
        });
    }

    private void listenForUnreadNotifications() {

        FirebaseDatabase.getInstance()
                .getReference("Notifications")
                .child(studentKey)
                .orderByChild("read")
                .equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int count = (int) snapshot.getChildrenCount();

                        if (count > 0) {
                            tvBadgeCount.setText(String.valueOf(count));
                            tvBadgeCount.setVisibility(View.VISIBLE);
                            tvBellText.setText("\uD83D\uDD14 " + count);
                        } else {
                            tvBadgeCount.setVisibility(View.GONE);
                            tvBellText.setText("");
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterEvents(String query) {
        String q = query.trim().toLowerCase();
        eventList.clear();
        if (q.isEmpty()) {
            eventList.addAll(allEvents);
        } else {
            for (Event e : allEvents) {
                String name = e.getEventName() == null ? "" : e.getEventName().toLowerCase();
                String club = e.getClubName() == null ? "" : e.getClubName().toLowerCase();
                if (name.contains(q) || club.contains(q)) {
                    eventList.add(e);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }
}
