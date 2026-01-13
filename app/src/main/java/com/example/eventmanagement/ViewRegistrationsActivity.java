package com.example.eventmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewRegistrationsActivity extends AppCompatActivity {

    private Spinner eventSpinner;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private android.widget.EditText etSearch;
    private com.google.android.material.chip.Chip chipAll, chipPaid, chipUnpaid;
    private TextView tvTotal, tvPaid, tvUnpaid;

    private DatabaseReference eventsRef;
    private DatabaseReference registrationsRef;

    private final List<String> eventNames = new ArrayList<>();
    private final Map<String, String> eventNameToId = new HashMap<>();

    private final List<Registration> registrationList = new ArrayList<>();
    private final List<Registration> sourceList = new ArrayList<>();
    private RegistrationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_registrations);

        eventSpinner = findViewById(R.id.eventSpinner);
        recyclerView = findViewById(R.id.registrationRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistrationAdapter(registrationList);
        recyclerView.setAdapter(adapter);

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        registrationsRef = FirebaseDatabase.getInstance().getReference("EventRegistrations");

        etSearch = findViewById(R.id.etSearch);
        chipAll = findViewById(R.id.chipAll);
        chipPaid = findViewById(R.id.chipPaid);
        chipUnpaid = findViewById(R.id.chipUnpaid);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaid = findViewById(R.id.tvPaid);
        tvUnpaid = findViewById(R.id.tvUnpaid);

        // Listeners
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        com.google.android.material.chip.ChipGroup filterGroup = findViewById(R.id.filterGroup);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());

        loadEvents();
    }

    private void loadEvents() {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                eventNames.clear();
                eventNameToId.clear();

                // Add 'All Events' option
                eventNames.add("All Events");
                eventNameToId.put("All Events", null);

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String eId = ds.getKey();
                    String eName = ds.child("eventName").getValue(String.class);
                    if (eId != null && eName != null) {
                        eventNames.add(eName);
                        eventNameToId.put(eName, eId);
                    }
                }

                if (eventNames.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("No events found");
                    return;
                }

                ArrayAdapter<String> spinnerAdapter =
                        new ArrayAdapter<>(ViewRegistrationsActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                eventNames);

                eventSpinner.setAdapter(spinnerAdapter);

                eventSpinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(
                                    AdapterView<?> parent, View view, int position, long id) {

                                String selectedEventName = eventNames.get(position);
                                String selectedEventId = eventNameToId.get(selectedEventName);

                                if (selectedEventId == null) {
                                    loadAllRegistrations();
                                } else {
                                    loadRegistrations(selectedEventId);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        }
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRegistrations(String eventId) {
        registrationsRef.child(eventId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        sourceList.clear();
                        registrationList.clear();

                        // Individual registrations at the root (by student roll)
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if ("groups".equals(ds.getKey())) continue;
                            Registration r = ds.getValue(Registration.class);
                            if (r != null) sourceList.add(r);
                        }

                        // Group registrations under 'groups'
                        DataSnapshot groups = snapshot.child("groups");
                        for (DataSnapshot g : groups.getChildren()) {
                            String groupName = g.child("groupName").getValue(String.class);
                            DataSnapshot membersSnap = g.child("members");
                            List<Registration> members = new ArrayList<>();
                            for (DataSnapshot m : membersSnap.getChildren()) {
                                Registration mr = m.getValue(Registration.class);
                                if (mr != null) members.add(mr);
                            }
                            Registration groupItem = new Registration();
                            try {
                                java.lang.reflect.Field f1 = Registration.class.getDeclaredField("groupName");
                                f1.setAccessible(true);
                                f1.set(groupItem, groupName);
                                java.lang.reflect.Field f2 = Registration.class.getDeclaredField("members");
                                f2.setAccessible(true);
                                f2.set(groupItem, members);
                            } catch (Exception ignored) {}
                            sourceList.add(groupItem);
                        }

                        applyFilters();

                        emptyText.setVisibility(
                                registrationList.isEmpty()
                                        ? View.VISIBLE
                                        : View.GONE
                        );

                        if (registrationList.isEmpty()) {
                            emptyText.setText("No registrations found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadAllRegistrations() {
        registrationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot root) {
                sourceList.clear();
                registrationList.clear();

                for (DataSnapshot eventNode : root.getChildren()) {
                    // Individuals under event
                    for (DataSnapshot ds : eventNode.getChildren()) {
                        if ("groups".equals(ds.getKey())) continue;
                        Registration r = ds.getValue(Registration.class);
                        if (r != null) sourceList.add(r);
                    }
                    // Groups under event
                    DataSnapshot groups = eventNode.child("groups");
                    for (DataSnapshot g : groups.getChildren()) {
                        String groupName = g.child("groupName").getValue(String.class);
                        DataSnapshot membersSnap = g.child("members");
                        List<Registration> members = new ArrayList<>();
                        for (DataSnapshot m : membersSnap.getChildren()) {
                            Registration mr = m.getValue(Registration.class);
                            if (mr != null) members.add(mr);
                        }
                        Registration groupItem = new Registration();
                        try {
                            java.lang.reflect.Field f1 = Registration.class.getDeclaredField("groupName");
                            f1.setAccessible(true);
                            f1.set(groupItem, groupName);
                            java.lang.reflect.Field f2 = Registration.class.getDeclaredField("members");
                            f2.setAccessible(true);
                            f2.set(groupItem, members);
                        } catch (Exception ignored) {}
                        sourceList.add(groupItem);
                    }
                }

                applyFilters();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void applyFilters() {
        String q = etSearch.getText() == null ? "" : etSearch.getText().toString().trim().toLowerCase();
        boolean wantPaid, wantUnpaid;
        int checkedId = ((com.google.android.material.chip.ChipGroup) findViewById(R.id.filterGroup)).getCheckedChipId();
        wantPaid = checkedId == R.id.chipPaid;
        wantUnpaid = checkedId == R.id.chipUnpaid;

        registrationList.clear();
        int total = 0, paid = 0, unpaid = 0;

        for (Registration item : sourceList) {
            if (item.getGroupName() == null) {
                // Individual
                boolean isPaid = false;
                try {
                    java.lang.reflect.Field f = item.getClass().getDeclaredField("paid");
                    f.setAccessible(true);
                    Object v = f.get(item);
                    isPaid = v instanceof Boolean && (Boolean) v;
                } catch (Exception ignored) {}

                String name = item.getFullName() == null ? "" : item.getFullName().toLowerCase();
                String email = item.getEmail() == null ? "" : item.getEmail().toLowerCase();
                boolean matchesSearch = q.isEmpty() || name.contains(q) || email.contains(q);
                boolean matchesPaid = !wantPaid && !wantUnpaid || (wantPaid && isPaid) || (wantUnpaid && !isPaid);

                if (matchesSearch && matchesPaid) {
                    registrationList.add(item);
                }

                total++;
                if (isPaid) paid++; else unpaid++;

            } else {
                // Group: filter members by search + paid
                java.util.List<Registration> filteredMembers = new java.util.ArrayList<>();
                java.util.List<Registration> members = item.getMembers();
                if (members != null) {
                    for (Registration m : members) {
                        boolean isPaidM = false;
                        try {
                            java.lang.reflect.Field f = m.getClass().getDeclaredField("paid");
                            f.setAccessible(true);
                            Object v = f.get(m);
                            isPaidM = v instanceof Boolean && (Boolean) v;
                        } catch (Exception ignored) {}

                        String name = m.getFullName() == null ? "" : m.getFullName().toLowerCase();
                        String email = m.getEmail() == null ? "" : m.getEmail().toLowerCase();
                        boolean matchesSearch = q.isEmpty() || name.contains(q) || email.contains(q);
                        boolean matchesPaid = !wantPaid && !wantUnpaid || (wantPaid && isPaidM) || (wantUnpaid && !isPaidM);

                        if (matchesSearch && matchesPaid) {
                            filteredMembers.add(m);
                        }

                        total++;
                        if (isPaidM) paid++; else unpaid++;
                    }
                }

                if (!filteredMembers.isEmpty() || (q.isEmpty() && !wantPaid && !wantUnpaid)) {
                    // Show group if any member visible or if no filters
                    Registration groupItem = new Registration();
                    try {
                        java.lang.reflect.Field f1 = Registration.class.getDeclaredField("groupName");
                        f1.setAccessible(true);
                        f1.set(groupItem, item.getGroupName());
                        java.lang.reflect.Field f2 = Registration.class.getDeclaredField("members");
                        f2.setAccessible(true);
                        f2.set(groupItem, filteredMembers.isEmpty() ? members : filteredMembers);
                    } catch (Exception ignored) {}
                    registrationList.add(groupItem);
                }
            }
        }

        tvTotal.setText("Total: " + total);
        tvPaid.setText("Paid: " + paid);
        tvUnpaid.setText("Unpaid: " + unpaid);

        adapter.notifyDataSetChanged();
        emptyText.setVisibility(registrationList.isEmpty() ? View.VISIBLE : View.GONE);
        if (registrationList.isEmpty()) emptyText.setText("No registrations found");
    }
}
