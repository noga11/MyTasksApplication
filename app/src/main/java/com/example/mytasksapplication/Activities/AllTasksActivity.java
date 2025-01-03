package com.example.mytasksapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mytasksapplication.DailyTasksAdapter;
import com.example.mytasksapplication.Model;
import com.example.mytasksapplication.R;
import com.example.mytasksapplication.Task;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AllTasksActivity extends AppCompatActivity {

    private Model model;
    private ListView lstDailyTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("All Tasks");

        // Initialize the ListView
        lstDailyTasks = findViewById(R.id.lstDailyTasks);
        List<Task> tasks = new ArrayList<>();

        // Set the custom adapter to the ListView
        DailyTasksAdapter adapter = new DailyTasksAdapter(this, tasks);
        lstDailyTasks.setAdapter(adapter);

        // Initialize the NavigationBarView (formerly BottomNavigationView)
        NavigationBarView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_Add) {
                    startActivity(new Intent(AllTasksActivity.this, NewTaskActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_Today) {
                    startActivity(new Intent(AllTasksActivity.this, MainActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_follow_request) {
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_users){
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_following){
            startActivity(new Intent(AllTasksActivity.this, FollowingActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_profile){
            startActivity(new Intent(AllTasksActivity.this, LoginActivity.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_logout){
            startActivity(new Intent(AllTasksActivity.this, LoginActivity.class));
            return true;
        }
        return false;
    }
}
