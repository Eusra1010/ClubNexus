package com.example.eventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    LinearLayout btnCreateEvent, btnManageEvents, btnViewRegistrations;
    Button btnLogout;
    String clubName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        clubName = getIntent().getStringExtra("clubName");

        btnCreateEvent = findViewById(R.id.cardCreateEvent);
        btnManageEvents = findViewById(R.id.cardManageEvents);
        btnViewRegistrations = findViewById(R.id.cardViewRegistrations);
        btnLogout = findViewById(R.id.btnAdminLogout);

        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("clubName", clubName);
            startActivity(intent);
        });

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageEventsActivity.class);
            intent.putExtra("clubName", clubName);
            startActivity(intent);
        });


        btnViewRegistrations.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewRegistrationsActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.cardAdminAnnouncements)
                .setOnClickListener(v ->
                        startActivity(new Intent(
                                this, AdminAnnouncementActivity.class
                        )));

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
