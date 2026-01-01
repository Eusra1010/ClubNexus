package com.example.eventmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    EditText etEventName, etEventDate, etVenue, etClubName;
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
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        databaseReference = FirebaseDatabase.getInstance().getReference("Events");

        etEventDate.setOnClickListener(v -> showDatePicker());

        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    String date = y + "-" + String.format("%02d", m + 1) + "-" + String.format("%02d", d);
                    etEventDate.setText(date);
                },
                year, month, day
        );

        dialog.show();
    }

    private void createEvent() {
        String eventName = etEventName.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String clubName = etClubName.getText().toString().trim();

        if (eventName.isEmpty() || eventDate.isEmpty() || venue.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = databaseReference.push().getKey();

        HashMap<String, String> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("eventName", eventName);
        eventData.put("eventDate", eventDate);
        eventData.put("venue", venue);
        eventData.put("clubName", clubName);

        databaseReference.child(eventId)
                .setValue(eventData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show()
                );
    }
}
