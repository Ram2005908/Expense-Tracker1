package com.example.expensetracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class ExpenseTrackerApp extends Application {
    private static final String TAG = "ExpenseTrackerApp";
    public static final String NOTIFICATION_CHANNEL_ID = "expense_tracker_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
        
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Expense Tracker",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Budget alerts and notifications");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
} 