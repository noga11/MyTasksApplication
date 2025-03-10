package com.example.mytasksapplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Event {
    private String title, details, adress, id;
    private ArrayList<String> shareWithUsers;
    private Date start, end, remTime, date, remDate;
    private boolean reminder, important;
    private int colour, notificationId;

    public Event(String title, String details, String adress, ArrayList<String> shareWithUsers,
                 Date start, Date end, Date remTime, Date date, Date remDate, boolean reminder,
                 boolean important, int colour, int notificationId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.details = details;
        this.adress = adress;
        this.shareWithUsers = shareWithUsers;
        this.start = start;
        this.end = end;
        this.date = date;
        this.remTime = remTime;
        this.remDate = remDate;
        this.reminder = reminder;
        this.important = important;
        this.colour = colour;
        this.notificationId = notificationId;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public ArrayList<String> getShareWithUsers() { return shareWithUsers; }
    public void setShareWithUsers(ArrayList<String> shareWithUsers) { this.shareWithUsers = shareWithUsers; }

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }

    public Date getRemTime() { return remTime; }
    public void setRemTime(Date remTime) { this.remTime = remTime; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getRemDate() { return remDate; }
    public void setRemDate(Date remDate) { this.remDate = remDate; }

    public boolean isReminder() { return reminder; }
    public void setReminder(boolean reminder) { this.reminder = reminder; }

    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }

    public int getColour() { return colour; }
    public void setColour(int colour) { this.colour = colour; }
}
