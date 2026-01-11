package com.example.eventmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ManageEventAdapter adapter;

    List<Event> allEvents = new ArrayList<>();
    List<Event> filteredEvents = new ArrayList<>();

    DatabaseReference eventsRef;
    String clubName;
    String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        clubName = getIntent().getStringExtra("clubName");

        recyclerView = findViewById(R.id.recyclerManageEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ManageEventAdapter(this, filteredEvents);
        recyclerView.setAdapter(adapter);

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        setupSearch();
        setupFilters();
        loadClubEvents();
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }
        });
    }

    private void setupFilters() {
        ChipGroup group = findViewById(R.id.filterGroup);
        Chip chipAll = findViewById(R.id.chipAll);
        chipAll.setChecked(true);

        group.setOnCheckedChangeListener((g, checkedId) -> {
            if (checkedId == R.id.chipActive) currentFilter = "ACTIVE";
            else if (checkedId == R.id.chipPast) currentFilter = "PAST";
            else if (checkedId == R.id.chipCancelled) currentFilter = "CANCELLED";
            else currentFilter = "ALL";
            applyFilters(null);
        });
    }

    private void loadClubEvents() {

        if (clubName == null || clubName.trim().isEmpty()) {
            Toast.makeText(this, "Club not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventsRef.orderByChild("clubName")
                .equalTo(clubName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        allEvents.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Event event = child.getValue(Event.class);
                            if (event != null) {
                                event.setEventId(child.getKey());
                                allEvents.add(event);
                            }
                        }

                        applyFilters(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                ManageEventsActivity.this,
                                "Failed to load events",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void applyFilters(String query) {

        filteredEvents.clear();

        long today = System.currentTimeMillis();

        for (Event e : allEvents) {

            String name = e.getEventName() == null ? "" : e.getEventName().toLowerCase();

            String status = e.getStatus() == null
                    ? "ACTIVE"
                    : e.getStatus().trim().toUpperCase();

            if (query != null && !name.contains(query.toLowerCase())) continue;

            boolean isPast = false;
            if (e.getEventDate() != null) {
                try {
                    long eventTime = new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(e.getEventDate()).getTime();
                    isPast = eventTime < today;
                } catch (Exception ignored) {}
            }

            if ("ACTIVE".equals(currentFilter)) {
                if (!"ACTIVE".equals(status) || isPast) continue;
            }

            if ("CANCELLED".equals(currentFilter)) {
                if (!"CANCELLED".equals(status)) continue;
            }

            if ("PAST".equals(currentFilter)) {
                if (!isPast) continue;
            }

            filteredEvents.add(e);
        }

        adapter.notifyDataSetChanged();
    }

}
