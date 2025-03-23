package com.example.expensetracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private ExpenseRepository repository;
    private String currentUserId;
    private MutableLiveData<Double> monthlyBudget = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadMonthlyBudget();
    }

    private void loadMonthlyBudget() {
        // Load from Firestore
        // This is a placeholder - implement actual budget loading
        monthlyBudget.setValue(1000.0);
    }

    public LiveData<List<Expense>> getRecentExpenses() {
        return repository.getAllExpenses(currentUserId);
    }

    public LiveData<Double> getCurrentMonthExpenses() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        Date endDate = calendar.getTime();

        return repository.getTotalExpenseForPeriod(currentUserId, startDate, endDate);
    }

    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudget;
    }

    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return repository.getExpensesByCategory(currentUserId, category);
    }

    public void syncExpenses() {
        repository.syncExpenses();
    }
} 