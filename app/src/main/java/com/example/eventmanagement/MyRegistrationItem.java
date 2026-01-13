package com.example.eventmanagement;

public class MyRegistrationItem {
    private final String eventId;
    private final String eventName;
    private final String eventDate;
    private final String status;
    private final boolean paid;
    private final boolean groupLeader;
    private final String groupId; // null for individual
    // Leader details (for group leader entries)
    private final String leaderName;
    private final String leaderEmail;
    private final String leaderContact;
    private final String leaderDepartment;
    private final String leaderBatch;
    private final String leaderUniversity;

    private MyRegistrationItem(String eventId, String eventName, String eventDate, String status, boolean paid, boolean groupLeader, String groupId,
                               String leaderName, String leaderEmail, String leaderContact, String leaderDepartment, String leaderBatch, String leaderUniversity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.status = status;
        this.paid = paid;
        this.groupLeader = groupLeader;
        this.groupId = groupId;
        this.leaderName = leaderName;
        this.leaderEmail = leaderEmail;
        this.leaderContact = leaderContact;
        this.leaderDepartment = leaderDepartment;
        this.leaderBatch = leaderBatch;
        this.leaderUniversity = leaderUniversity;
    }

    public static MyRegistrationItem individual(String eventId, String eventName, String eventDate, String status, boolean paid) {
        return new MyRegistrationItem(eventId, eventName, eventDate, status, paid, false, null, null, null, null, null, null, null);
    }

    public static MyRegistrationItem groupLeader(String eventId, String eventName, String eventDate, String status, String groupId, boolean paid,
                                                 String leaderName, String leaderEmail, String leaderContact, String leaderDepartment, String leaderBatch, String leaderUniversity) {
        return new MyRegistrationItem(eventId, eventName, eventDate, status, paid, true, groupId, leaderName, leaderEmail, leaderContact, leaderDepartment, leaderBatch, leaderUniversity);
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getEventDate() { return eventDate; }
    public String getStatus() { return status; }
    public boolean isPaid() { return paid; }
    public boolean isGroupLeader() { return groupLeader; }
    public String getGroupId() { return groupId; }
    public String getLeaderName() { return leaderName; }
    public String getLeaderEmail() { return leaderEmail; }
    public String getLeaderContact() { return leaderContact; }
    public String getLeaderDepartment() { return leaderDepartment; }
    public String getLeaderBatch() { return leaderBatch; }
    public String getLeaderUniversity() { return leaderUniversity; }
}
