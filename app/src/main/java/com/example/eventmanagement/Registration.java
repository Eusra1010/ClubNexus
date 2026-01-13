package com.example.eventmanagement;

public class Registration {

    private String fullName;
    private String email;
    private String contactNo;

    // Optional for grouped registrations
    private String groupName;
    private java.util.List<Registration> members;

    public Registration() {
        // Required for Firebase
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String getGroupName() { return groupName; }
    public java.util.List<Registration> getMembers() { return members; }
}
