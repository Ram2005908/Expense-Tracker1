package com.example.expensetracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsViewModel extends AndroidViewModel {
    private FirebaseFirestore firestore;
    private String currentUserId;
    private MutableLiveData<Double> monthlyBudget = new MutableLiveData<>();
    private MutableLiveData<Boolean> notificationsEnabled = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>();

    public SettingsViewModel(Application application) {
        super(application);
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadSettings();
    }

    private void loadSettings() {
        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double budget = documentSnapshot.getDouble("monthlyBudget");
                        Boolean notifications = documentSnapshot.getBoolean("notificationsEnabled");
                        monthlyBudget.setValue(budget != null ? budget : 0.0);
                        notificationsEnabled.setValue(notifications != null ? notifications : true);
                    }
                });
    }

    public void saveSettings(double budget, boolean notifications) {
        firestore.collection("users")
                .document(currentUserId)
                .update(
                    "monthlyBudget", budget,
                    "notificationsEnabled", notifications
                )
                .addOnSuccessListener(aVoid -> operationSuccessful.setValue(true))
                .addOnFailureListener(e -> operationSuccessful.setValue(false));
    }

    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudget;
    }

    public LiveData<Boolean> getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }
} 