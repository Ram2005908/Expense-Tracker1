package com.example.expensetracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository repository;
    private String currentUserId;
    private MutableLiveData<Boolean> saveSuccessful = new MutableLiveData<>();

    public AddExpenseViewModel(Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public List<String> getCategories() {
        return Arrays.asList(
            "Food",
            "Transportation",
            "Housing",
            "Utilities",
            "Entertainment",
            "Shopping",
            "Healthcare",
            "Education",
            "Other"
        );
    }

    public void saveExpense(double amount, String category, String description, Date date) {
        String expenseId = UUID.randomUUID().toString();
        Expense expense = new Expense(expenseId, currentUserId, amount, category, description, date);
        
        repository.insert(expense);
        saveSuccessful.setValue(true);
    }

    public LiveData<Boolean> getSaveSuccessful() {
        return saveSuccessful;
    }
} 