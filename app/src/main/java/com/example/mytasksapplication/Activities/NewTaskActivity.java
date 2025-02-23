package com.example.mytasksapplication.Activities;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;

import com.example.mytasksapplication.ReminderBroadcastReceiver;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

public class NewTaskActivity extends AppCompatActivity implements View.OnClickListener {

    private Model model;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private String[] otherUsers = {"user1", "user2", "user3", "user4"};// need to connect to the model (temporary)
    private EditText etTitle, etDetails, auetShare;
    private TextView tvStartTime, tvEndTime, tvDate, tvReminderDate, tvReminderTime;
    private Button btbAddTask;
    private Switch swchReminder;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        model = Model.getInstance(this);
        createNotificationChannel();

        etTitle = findViewById(R.id.etTitle);
        etDetails = findViewById(R.id.etDetails);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvDate = findViewById(R.id.tvDate);
        tvReminderDate = findViewById(R.id.tvReminderDate);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        auetShare = findViewById(R.id.auetShare);
        chipGroup = findViewById(R.id.cgUsers);
        swchReminder = findViewById(R.id.swchReminder);
        btbAddTask = findViewById(R.id.btbAddTask);

        tvReminderDate.setVisibility(View.GONE);
        tvReminderTime.setVisibility(View.GONE);

        btbAddTask.setOnClickListener(this);

        tvDate.setOnClickListener(view -> showDatePicker(1));
        tvReminderDate.setOnClickListener(view -> showDatePicker(2));
        tvStartTime.setOnClickListener(view -> showTimePicker(1));
        tvEndTime.setOnClickListener(view -> showTimePicker(2));
        tvReminderTime.setOnClickListener(view -> showTimePicker(3));

        swchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                    swchReminder.setChecked(false);
                    return;
                }
                tvReminderDate.setVisibility(View.VISIBLE);
                tvReminderTime.setVisibility(View.VISIBLE);
            } else {
                tvReminderDate.setVisibility(View.GONE);
                tvReminderTime.setVisibility(View.GONE);
                cancelNotification();
                Toast.makeText(this, "Reminder canceled", Toast.LENGTH_SHORT).show();
            }
        });

        auetShare.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // You can add functionality to filter usernames or show suggestions based on what the user types
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String typedUsername = editable.toString().trim();
                if (!typedUsername.isEmpty() && isValidUsername(typedUsername)) {
                    addUserChip(typedUsername);
                    auetShare.setText("");  // Clear the input field
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        // set notification
        if (view.getId() == R.id.btbAddTask) {
            String taskTitle = etTitle.getText().toString().trim();

            if (taskTitle.isEmpty()) {
                Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (swchReminder.isChecked()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);

                long reminderTimeMillis = calendar.getTimeInMillis();

                if (reminderTimeMillis > System.currentTimeMillis()) {
                    scheduleNotification(reminderTimeMillis, taskTitle);
                    Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Reminder time must be in the future", Toast.LENGTH_SHORT).show();
                }
            }

            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification() {
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private boolean isValidUsername(String username) {
        for (String user : otherUsers) {
            if (user.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    private void addUserChip(String username) {
        Chip chip = new Chip(this);
        chip.setText(username);

        model.SetChipIconPictureFromUsername(username, chip);
        chip.setCloseIconVisible(true);
        chip.setCloseIconResource(android.R.drawable.ic_menu_delete);  // Set the 'X' icon

        // Remove the chip when the user presses 'X'
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            Toast.makeText(this, username + " removed", Toast.LENGTH_SHORT).show();
        });

        chipGroup.addView(chip);
    }

    private void showDatePicker(int option) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            String selectedDate = sdf.format(calendar.getTime());

            if (option == 1) {
                tvDate.setText(selectedDate);
            } else if (option == 2) {
                tvReminderDate.setText(selectedDate);
            }
        });
    }

    private void showTimePicker(int option) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        timePicker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");

        timePicker.addOnPositiveButtonClickListener(selection -> {
            selectedHour = timePicker.getHour();
            selectedMinute = timePicker.getMinute();

            String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            if (option == 1) {
                tvStartTime.setText(formattedTime);
            } else if (option == 2) {
                tvEndTime.setText(formattedTime);
            } else if (option == 3) {
                tvReminderTime.setText(formattedTime);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReminderChannel";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("reminderChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleNotification(long reminderTimeMillis, String taskTitle) {
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("taskTitle", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
        }
    }

}