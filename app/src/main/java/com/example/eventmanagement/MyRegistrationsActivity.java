package com.example.eventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class MyRegistrationsActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private TextView emptyText;
	private MyRegistrationsAdapter adapter;
	private final List<MyRegistrationItem> items = new ArrayList<>();

	private String studentKey;

	private DatabaseReference eventsRef;
	private DatabaseReference registrationsRef;
	private DatabaseReference notificationsRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_registrations);

		studentKey = getIntent().getStringExtra("studentKey");
		if (studentKey == null || studentKey.trim().isEmpty()) {
			Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		recyclerView = findViewById(R.id.recyclerViewRegistrations);
		emptyText = findViewById(R.id.emptyText);
		ImageView back = findViewById(R.id.btnBack);

		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		adapter = new MyRegistrationsAdapter(items, new MyRegistrationsAdapter.ActionsListener() {
			@Override public void onOpen(MyRegistrationItem item) { openDetails(item); }
			@Override public void onPay(MyRegistrationItem item) { pay(item); }
			@Override public void onCancel(MyRegistrationItem item) { cancel(item); }
		});
		recyclerView.setAdapter(adapter);

		back.setOnClickListener(v -> finish());

		eventsRef = FirebaseDatabase.getInstance().getReference("Events");
		registrationsRef = FirebaseDatabase.getInstance().getReference("EventRegistrations");
		notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");

		loadRegistrations();
	}

	private void loadRegistrations() {
		eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override public void onDataChange(@NonNull DataSnapshot eventsSnap) {
				items.clear();

				for (DataSnapshot eSnap : eventsSnap.getChildren()) {
					String eventId = eSnap.getKey();
					Event event = eSnap.getValue(Event.class);
					if (eventId == null || event == null) continue;

					registrationsRef.child(eventId)
							.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override public void onDataChange(@NonNull DataSnapshot regSnap) {
									// Individual registration
									if (regSnap.child(studentKey).exists()) {
										Boolean paid = regSnap.child(studentKey).child("paid").getValue(Boolean.class);
										items.add(MyRegistrationItem.individual(
												event.getEventId(),
												event.getEventName(),
												event.getEventDate(),
												event.getStatus(),
												paid != null && paid
										));
									}

									// Group leader registration
									DataSnapshot groups = regSnap.child("groups");
									    for (DataSnapshot g : groups.getChildren()) {
										String leader = g.child("leaderRoll").getValue(String.class);
										if (leader != null && leader.equals(studentKey)) {
										    DataSnapshot leaderSnap = g.child("members").child("0");
										    Boolean paidLeader = leaderSnap.child("paid").getValue(Boolean.class);
										    String ln = val(leaderSnap.child("fullName").getValue(String.class));
										    String le = val(leaderSnap.child("email").getValue(String.class));
										    String lc = val(leaderSnap.child("contactNo").getValue(String.class));
										    String ld = val(leaderSnap.child("department").getValue(String.class));
										    String lb = val(leaderSnap.child("batch").getValue(String.class));
										    String lu = val(leaderSnap.child("university").getValue(String.class));

										    items.add(MyRegistrationItem.groupLeader(
											    event.getEventId(),
											    event.getEventName(),
											    event.getEventDate(),
											    event.getStatus(),
											    g.getKey(),
											    paidLeader != null && paidLeader,
											    ln, le, lc, ld, lb, lu
										    ));
										}
									}

									adapter.notifyDataSetChanged();
									emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
								}
								@Override public void onCancelled(@NonNull DatabaseError error) {}
							});
				}
			}
			@Override public void onCancelled(@NonNull DatabaseError error) {}
		});
	}

	private void openDetails(MyRegistrationItem item) {
		String paidText = item.isPaid() ? "Paid" : "Unpaid";
		String typeText = item.isGroupLeader() ? "Group Leader Registration" : "Individual Registration";
		String msg = "Event: " + item.getEventName() +
				"\nDate: " + safe(item.getEventDate()) +
				"\nStatus: " + safe(item.getStatus()) +
				"\nType: " + typeText +
				"\nPayment: " + paidText;

		new AlertDialog.Builder(this)
				.setTitle("Registration Details")
				.setMessage(msg)
				.setPositiveButton("Close", (d, w) -> d.dismiss())
				.show();
	}

	private void pay(MyRegistrationItem item) {
		// Set paid=true in registration
		if (item.isGroupLeader()) {
			registrationsRef.child(item.getEventId())
					.child("groups").child(item.getGroupId())
					.child("members").child("0").child("paid")
					.setValue(true);
		} else {
			registrationsRef.child(item.getEventId())
					.child(studentKey)
					.child("paid")
					.setValue(true);
		}

		// Push an EMAIL_SENT notification with billing info
		long ts = System.currentTimeMillis();
		java.util.HashMap<String, Object> notif = new java.util.HashMap<>();
		notif.put("title", "Billing Details Sent");
		notif.put("message", "Payment instructions have been sent for " + item.getEventName() + ".");
		notif.put("type", "EMAIL_SENT");
		notif.put("eventId", item.getEventId());
		notif.put("timestamp", ts);
		notif.put("read", false);
		notificationsRef.child(studentKey).push().setValue(notif);

		Toast.makeText(this, "Billing info sent. Opening notifications…", Toast.LENGTH_SHORT).show();
		Intent i = new Intent(this, StudentNotificationsActivity.class);
		i.putExtra("studentKey", studentKey);
		startActivity(i);
	}

	private void cancel(MyRegistrationItem item) {
		new AlertDialog.Builder(this)
				.setTitle("Cancel Registration")
				.setMessage(item.isGroupLeader()
						? "Cancel entire group registration for this event?"
						: "Cancel your registration for this event?")
				.setNegativeButton("Keep", (d, w) -> d.dismiss())
				.setPositiveButton("Cancel", (d, w) -> {
					if (item.isGroupLeader()) {
						registrationsRef.child(item.getEventId())
								.child("groups").child(item.getGroupId())
								.removeValue();
					} else {
						registrationsRef.child(item.getEventId())
								.child(studentKey)
								.removeValue();
					}

					// Push cancellation notification
					long ts = System.currentTimeMillis();
					java.util.HashMap<String, Object> notif = new java.util.HashMap<>();
					notif.put("title", "Registration Cancelled");
					notif.put("message", "Your registration for " + item.getEventName() + " has been cancelled.");
					notif.put("type", "REGISTRATION_CANCELLED");
					notif.put("eventId", item.getEventId());
					notif.put("timestamp", ts);
					notif.put("read", false);
					notificationsRef.child(studentKey).push().setValue(notif);

					Toast.makeText(MyRegistrationsActivity.this, "Cancelled. Opening notifications…", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(MyRegistrationsActivity.this, StudentNotificationsActivity.class);
					i.putExtra("studentKey", studentKey);
					startActivity(i);
					loadRegistrations();
				})
				.show();
	}

	private String safe(String s) { return s == null ? "" : s; }
	private String val(String s) { return s == null ? "" : s; }
}
