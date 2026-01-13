package com.example.eventmanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import android.view.View;
import java.util.ArrayList;

public class RegisterEventActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etDepartment, etEventName, etClubName, etContactNo, etUniversity;
    EditText etGroupName;
    Spinner spinnerGroupSize;
    LinearLayout membersContainer;
    AutoCompleteTextView actvBatch;
    Button btnRegisterEvent;

    DatabaseReference eventsRef, registrationsRef;
    String eventId;
    String studentKey;   // 🔑 ROLL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_event);

        eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");
        String clubName = getIntent().getStringExtra("clubName");


        studentKey = getIntent().getStringExtra("studentKey");

        if (eventId == null || eventName == null || clubName == null || studentKey == null) {
            Toast.makeText(this, "Invalid event/session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etContactNo = findViewById(R.id.etContactNo);
        etDepartment = findViewById(R.id.etDepartment);
        actvBatch = findViewById(R.id.actvBatch);
        etEventName = findViewById(R.id.etEventName);
        etClubName = findViewById(R.id.etClubName);
        etUniversity = findViewById(R.id.etUniversity);
        etGroupName = findViewById(R.id.etGroupName);
        spinnerGroupSize = findViewById(R.id.spinnerGroupSize);
        membersContainer = findViewById(R.id.membersContainer);
        btnRegisterEvent = findViewById(R.id.btnRegisterEvent);

        etEventName.setText(eventName);
        etClubName.setText(clubName);
        etEventName.setEnabled(false);
        etClubName.setEnabled(false);

        setupBatchDropdown();

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        registrationsRef = FirebaseDatabase.getInstance().getReference("EventRegistrations");

        // Auto-fill university from student profile
        FirebaseDatabase.getInstance().getReference("StudentRegistrations")
                .child(studentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String university = snapshot.child("university").getValue(String.class);
                        if (university != null) etUniversity.setText(university);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Check if the event is a group event and configure UI
        eventsRef.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isGroup = snapshot.child("isGroupEvent").getValue(Boolean.class);
                Integer min = snapshot.child("minGroupMembers").getValue(Integer.class);
                Integer max = snapshot.child("maxGroupMembers").getValue(Integer.class);

                if (isGroup != null && isGroup) {
                    findViewById(R.id.tvGroupRegistration).setVisibility(android.view.View.VISIBLE);
                    etGroupName.setVisibility(android.view.View.VISIBLE);
                    findViewById(R.id.groupSizeRow).setVisibility(android.view.View.VISIBLE);
                    membersContainer.setVisibility(android.view.View.VISIBLE);

                    int minVal = (min == null || min < 1) ? 2 : min;
                    int maxVal = (max == null || max < minVal) ? minVal : max;
                    ArrayList<String> sizes = new ArrayList<>();
                    for (int i = minVal; i <= maxVal; i++) sizes.add(String.valueOf(i));
                    ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(RegisterEventActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, sizes);
                    spinnerGroupSize.setAdapter(sizeAdapter);
                    spinnerGroupSize.setSelection(0);

                    spinnerGroupSize.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            int totalMembers = Integer.parseInt((String) parent.getItemAtPosition(position));
                            buildMemberForms(totalMembers - 1); // exclude leader
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnRegisterEvent.setOnClickListener(v -> register());
    }

    private void setupBatchDropdown() {
        String[] batches = {"2K20", "2K21", "2K22", "2K23", "2K24"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        batches);
        actvBatch.setAdapter(adapter);
        actvBatch.setOnClickListener(v -> actvBatch.showDropDown());
    }

    private void register() {

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String batch = actvBatch.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || contactNo.isEmpty() || department.isEmpty() || batch.isEmpty()) {
            Toast.makeText(this, "Please complete your details", Toast.LENGTH_SHORT).show();
            return;
        }

        eventsRef.child(eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Boolean open = snapshot.child("registrationOpen")
                                .getValue(Boolean.class);

                        if (open == null || !open) {
                            Toast.makeText(RegisterEventActivity.this,
                                    "Registration is closed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Boolean isGroup = snapshot.child("isGroupEvent").getValue(Boolean.class);

                        registrationsRef.child(eventId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snap) {

                                        if (isGroup != null && isGroup) {
                                            // Validate group inputs
                                            String groupName = etGroupName.getText().toString().trim();
                                            if (TextUtils.isEmpty(groupName)) {
                                                Toast.makeText(RegisterEventActivity.this, "Enter group name", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Collect member forms
                                            int childCount = membersContainer.getChildCount();
                                            if (childCount == 0) {
                                                Toast.makeText(RegisterEventActivity.this, "Select group size", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Prepare group object
                                            String groupId = registrationsRef.child(eventId).child("groups").push().getKey();
                                            if (groupId == null) {
                                                Toast.makeText(RegisterEventActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Check duplicate registration for leader
                                            if (snap.child(studentKey).exists()) {
                                                Toast.makeText(RegisterEventActivity.this, "Already registered individually", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            HashMap<String, Object> groupData = new HashMap<>();
                                            groupData.put("groupName", groupName);
                                            groupData.put("leaderRoll", studentKey);

                                            // Leader as first member
                                            HashMap<String, Object> leader = new HashMap<>();
                                            leader.put("fullName", fullName);
                                            leader.put("email", email);
                                            leader.put("contactNo", contactNo);
                                            leader.put("department", department);
                                            leader.put("batch", batch);
                                            leader.put("university", etUniversity.getText().toString().trim());
                                            leader.put("paid", false);

                                            HashMap<String, Object> members = new HashMap<>();
                                            members.put("0", leader);

                                            // Other members
                                            int index = 1;
                                            for (int i = 0; i < childCount; i++) {
                                                LinearLayout section = (LinearLayout) membersContainer.getChildAt(i);
                                                EditText mName = section.findViewWithTag("name");
                                                EditText mContact = section.findViewWithTag("contact");
                                                EditText mEmail = section.findViewWithTag("email");
                                                TextView mBatch = section.findViewWithTag("batch");
                                                EditText mDept = section.findViewWithTag("dept");

                                                String mn = mName.getText().toString().trim();
                                                String mc = mContact.getText().toString().trim();
                                                String me = mEmail.getText().toString().trim();
                                                String mb = mBatch.getText().toString().trim();
                                                String md = mDept.getText().toString().trim();

                                                if (mn.isEmpty() || mc.isEmpty() || me.isEmpty() || mb.isEmpty() || md.isEmpty()) {
                                                    Toast.makeText(RegisterEventActivity.this, "Fill all group member fields", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                HashMap<String, Object> mem = new HashMap<>();
                                                mem.put("fullName", mn);
                                                mem.put("email", me);
                                                mem.put("contactNo", mc);
                                                mem.put("department", md);
                                                mem.put("batch", mb);
                                                mem.put("paid", false);
                                                members.put(String.valueOf(index++), mem);
                                            }

                                            groupData.put("members", members);

                                            registrationsRef.child(eventId)
                                                    .child("groups")
                                                    .child(groupId)
                                                    .setValue(groupData)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(RegisterEventActivity.this, "Group registered", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    });

                                        } else {
                                            // Individual registration (prevent duplicate)
                                            if (snap.child(studentKey).exists()) {
                                                Toast.makeText(RegisterEventActivity.this,
                                                        "Already registered", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            HashMap<String, Object> data = new HashMap<>();
                                            data.put("fullName", fullName);
                                            data.put("email", email);
                                            data.put("contactNo", contactNo);
                                            data.put("department", department);
                                            data.put("batch", batch);
                                            data.put("university", etUniversity.getText().toString().trim());
                                            data.put("paid", false);

                                            registrationsRef.child(eventId)
                                                    .child(studentKey)
                                                    .setValue(data)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(RegisterEventActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    });
                                        }
                                    }

                                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void buildMemberForms(int count) {
        membersContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            LinearLayout block = new LinearLayout(this);
            block.setOrientation(LinearLayout.VERTICAL);
            block.setPadding(0, dp(8), 0, dp(8));

            addLabeledField(block, "Member " + (i + 2) + " - Full Name", "Enter full name", "name");
            addLabeledField(block, "Contact Number", "Enter contact no", "contact");
            addLabeledField(block, "Email Address", "Enter email", "email");
            addBatchDropdown(block);
            addLabeledField(block, "Department", "Enter department", "dept");

            // Spacer divider between members
            View divider = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lp.setMargins(0, dp(8), 0, dp(8));
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(0xFF000000);
            block.addView(divider);

            membersContainer.addView(block);
        }
    }

    private void addLabeledField(LinearLayout parent, String label, String hint, String tag) {
        // Label TextView (match leader style)
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(label);
        tv.setTextColor(0xFF000000);
        tv.setBackgroundColor(0xFFFFFFFF);
        tv.setPadding(dp(6), dp(6), dp(6), dp(6));
        parent.addView(tv);

        // EditText (match leader style)
        EditText et = new EditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        et.setHint(hint);
        et.setTag(tag);
        et.setBackground(null);
        et.setTextColor(0xFFFFFFFF);
        et.setHintTextColor(0xF2F2F2);
        et.setPadding(0, dp(10), 0, dp(10));
        parent.addView(et);

        // Black underline (match leader underline)
        View underline = new View(this);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        up.setMargins(0, 0, 0, dp(20));
        underline.setLayoutParams(up);
        underline.setBackgroundColor(0xFF000000);
        parent.addView(underline);
    }

    private void addBatchDropdown(LinearLayout parent) {
        // Label
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Batch");
        tv.setTextColor(0xFF000000);
        tv.setBackgroundColor(0xFFFFFFFF);
        tv.setPadding(dp(6), dp(6), dp(6), dp(6));
        parent.addView(tv);

        // Dropdown
        AutoCompleteTextView actv = new AutoCompleteTextView(this);
        actv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        actv.setHint("Select batch");
        actv.setTag("batch");
        actv.setBackground(null);
        actv.setTextColor(0xFFFFFFFF);
        actv.setHintTextColor(0xF2F2F2);
        actv.setPadding(0, dp(10), 0, dp(10));
        actv.setInputType(android.text.InputType.TYPE_NULL);
        actv.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
        parent.addView(actv);

        String[] batches = {"2K20", "2K21", "2K22", "2K23", "2K24"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, batches);
        actv.setAdapter(adapter);
        actv.setOnClickListener(v -> actv.showDropDown());

        // Underline
        View underline = new View(this);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        up.setMargins(0, 0, 0, dp(20));
        underline.setLayoutParams(up);
        underline.setBackgroundColor(0xFF000000);
        parent.addView(underline);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
