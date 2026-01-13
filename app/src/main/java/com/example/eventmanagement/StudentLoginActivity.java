package com.example.eventmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    EditText etRoll, etPassword;
    Button btnLogin;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        etRoll = findViewById(R.id.studentRoll);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.studentLoginBtn);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("StudentRegistrations");

        btnLogin.setOnClickListener(v -> loginStudent());
    }

    private void loginStudent() {

        String roll = etRoll.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (roll.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter roll and password", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(roll)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(StudentLoginActivity.this,
                                    "Student not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String dbPasswordHash = snapshot.child("passwordHash").getValue(String.class);
                        String inputHash = hashPassword(roll, password);

                        if (dbPasswordHash != null && dbPasswordHash.equals(inputHash)) {

                            Intent intent = new Intent(
                                    StudentLoginActivity.this,
                                    StudentDashboardActivity.class
                            );
                            intent.putExtra("studentKey", roll);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(StudentLoginActivity.this,
                                    "Wrong password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentLoginActivity.this,
                                "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String hashPassword(String roll, String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((roll + ":" + password).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
