package com.example.eventmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    EditText etEventName, etEventDate, etVenue, etClubName, etFees, etDeadline, etMinGroupMembers, etMaxGroupMembers;
    CheckBox cbGroupEvent;
    Button btnCreateEvent;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etEventName = findViewById(R.id.etEventName);
        etEventDate = findViewById(R.id.etEventDate);
        etVenue = findViewById(R.id.etVenue);
        etClubName = findViewById(R.id.etClubName);
        etFees = findViewById(R.id.etFees);
        etDeadline = findViewById(R.id.etDeadline);
        cbGroupEvent = findViewById(R.id.cbGroupEvent);
        etMinGroupMembers = findViewById(R.id.etMinGroupMembers);
        etMaxGroupMembers = findViewById(R.id.etMaxGroupMembers);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        databaseReference = FirebaseDatabase.getInstance().getReference("Events");

        String clubName = getIntent().getStringExtra("clubName");
        if (clubName != null) etClubName.setText(clubName);

        etEventDate.setOnClickListener(v -> showDatePicker(etEventDate));
        etDeadline.setOnClickListener(v -> showDatePicker(etDeadline));

        // Enable/disable group member fields based on checkbox
        setGroupFieldsEnabled(false);
        cbGroupEvent.setOnCheckedChangeListener((buttonView, isChecked) -> setGroupFieldsEnabled(isChecked));

        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String date = y + "-"
                            + String.format("%02d", m + 1) + "-"
                            + String.format("%02d", d);
                    target.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void createEvent() {

        String eventName = etEventName.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String clubName = etClubName.getText().toString().trim();
        String fees = etFees.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();
        boolean isGroupEvent = cbGroupEvent.isChecked();
        String minStr = etMinGroupMembers.getText().toString().trim();
        String maxStr = etMaxGroupMembers.getText().toString().trim();

        if (fees.isEmpty()) fees = "0";

        if (eventName.isEmpty() || eventDate.isEmpty() || venue.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer minGroup = null, maxGroup = null;
        if (isGroupEvent) {
            if (minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Enter min and max group members", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                minGroup = Integer.parseInt(minStr);
                maxGroup = Integer.parseInt(maxStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Group member values must be numbers", Toast.LENGTH_SHORT).show();
                return;
            }
            if (minGroup < 1) {
                Toast.makeText(this, "Minimum group members must be at least 1", Toast.LENGTH_SHORT).show();
                return;
            }
            if (maxGroup < minGroup) {
                Toast.makeText(this, "Maximum group members must be >= minimum", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        checkDateAndSave(eventDate, eventName, venue, clubName, fees, deadline, isGroupEvent, minGroup, maxGroup);
    }

    private void checkDateAndSave(String date,
                                  String eventName,
                                  String venue,
                                  String clubName,
                                  String fees,
                                  String deadline,
                                  boolean isGroupEvent,
                                  Integer minGroup,
                                  Integer maxGroup) {

        databaseReference.orderByChild("eventDate")
                .equalTo(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            saveEvent(date, eventName, venue, clubName, fees, deadline, isGroupEvent, minGroup, maxGroup);
                        } else {
                            Toast.makeText(
                                    CreateEventActivity.this,
                                    "Date booked. Choose another date",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(CreateEventActivity.this,
                                "Database error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEvent(String date,
                           String eventName,
                           String venue,
                           String clubName,
                           String fees,
                           String deadline,
                           boolean isGroupEvent,
                           Integer minGroup,
                           Integer maxGroup) {

        String eventId = databaseReference.push().getKey();

        HashMap<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("eventName", eventName);
        eventData.put("eventDate", date);
        eventData.put("venue", venue);
        eventData.put("clubName", clubName);
        eventData.put("fees", fees);
        eventData.put("registrationOpen", true);
        eventData.put("registrationCount", 0);
        eventData.put("status", "ACTIVE");
        eventData.put("registrationDeadline", deadline);
        eventData.put("isGroupEvent", isGroupEvent);
        if (isGroupEvent) {
            eventData.put("minGroupMembers", minGroup);
            eventData.put("maxGroupMembers", maxGroup);
        }

        databaseReference.child(eventId)
                .setValue(eventData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Event created successfully",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to create event",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void setGroupFieldsEnabled(boolean enabled) {
        etMinGroupMembers.setEnabled(enabled);
        etMaxGroupMembers.setEnabled(enabled);
        if (!enabled) {
            etMinGroupMembers.setText("");
            etMaxGroupMembers.setText("");
        }
    }
}
