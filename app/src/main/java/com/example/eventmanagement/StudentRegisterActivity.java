package com.example.eventmanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

public class StudentRegisterActivity extends AppCompatActivity {

    EditText etFullName, etRoll, etUniversity, etPassword;
    TextView tvPasswordMessage;
    Button btnRegister;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        etFullName = findViewById(R.id.etFullName);
        etRoll = findViewById(R.id.etRoll);
        etUniversity = findViewById(R.id.etUniversity);
        etPassword = findViewById(R.id.etPassword);
        tvPasswordMessage = findViewById(R.id.tvPasswordMessage);
        btnRegister = findViewById(R.id.btnRegister);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("StudentRegistrations");

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordMessage(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {

        String fullName = etFullName.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();
        String university = etUniversity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() || roll.isEmpty() || university.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> issues = getPasswordIssues(password);
        if (!issues.isEmpty()) {
            Toast.makeText(this, issues.get(0), Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordHash = hashPassword(roll, password);

        HashMap<String, String> data = new HashMap<>();
        data.put("fullName", fullName);
        data.put("roll", roll);
        data.put("university", university);
        data.put("passwordHash", passwordHash);

        databaseReference.child(roll)
                .setValue(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Password meets all requirements", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Sign up Successful", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updatePasswordMessage(String password) {
        List<String> issues = getPasswordIssues(password);
        if (issues.isEmpty()) {
            tvPasswordMessage.setText("Password meets all requirements");
            tvPasswordMessage.setTextColor(0xFF00C853); // green
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < issues.size(); i++) {
                if (i > 0) sb.append("\n");
                sb.append(issues.get(i));
            }
            tvPasswordMessage.setText(sb.toString());
            tvPasswordMessage.setTextColor(0xFFFF5252); // red
        }
    }

    private List<String> getPasswordIssues(String password) {
        List<String> issues = new ArrayList<>();
        if (password.length() < 8) {
            issues.add("Password must be at least 8 characters long");
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        if (!(hasUpper && hasLower)) {
            issues.add("Password must contain both uppercase and lowercase letters");
        }
        if (!hasDigit) {
            issues.add("Password must contain at least one number");
        }
        if (!hasSpecial) {
            issues.add("Password must contain at least one special character (@, #, &, etc.)");
        }
        return issues;
    }

    private String hashPassword(String roll, String password) {
        try {
            String input = roll + ":" + password;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            Formatter formatter = new Formatter();
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            String hash = formatter.toString();
            formatter.close();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
