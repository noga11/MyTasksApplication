package com.example.mytasksapplication;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    private String title, details;
    private ArrayList<String> group, shareWithUsers;
    private Date start, end, remTime, date, remDate;
    private boolean reminder, important, started;
    private int progress, colour;

    public Task(String title, String details, ArrayList<String> group, ArrayList<String> shareWithUsers,
                Date start, Date end, Date remTime, Date date, Date remDate, boolean reminder,
                boolean important, boolean started, int progress, int colour) {
        this.title = title;
        this.details = details;
        this.group = group;
        this.shareWithUsers = shareWithUsers;
        this.start = start;
        this.end = end;
        this.date = date;
        this.remTime = remTime;
        this.remDate = remDate;
        this.reminder = reminder;
        this.important = important;
        this.started = started;
        this.progress = progress;
        this.colour = colour;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public ArrayList<String> getGroup() { return group; }
    public void setGroup(ArrayList<String> group) { this.group = group; }

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

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getColour() { return colour; }
    public void setColour(int colour) { this.colour = colour; }
}
