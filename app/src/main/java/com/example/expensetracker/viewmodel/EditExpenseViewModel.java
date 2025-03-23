package com.example.expensetracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EditExpenseViewModel extends AndroidViewModel {
    private ExpenseRepository repository;
    private String currentUserId;
    private MutableLiveData<Expense> expense = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>();
    private FirebaseFirestore firestore;

    public EditExpenseViewModel(Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
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

    public void loadExpense(String expenseId) {
        firestore.collection("expenses")
                .document(expenseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Expense loadedExpense = documentSnapshot.toObject(Expense.class);
                    if (loadedExpense != null) {
                        expense.setValue(loadedExpense);
                    }
                });
    }

    public void updateExpense(String expenseId, double amount, String category, 
                            String description, Date date) {
        Expense updatedExpense = new Expense(expenseId, currentUserId, amount, 
                                           category, description, date);
        repository.update(updatedExpense);
        operationSuccessful.setValue(true);
    }

    public void deleteExpense(Expense expense) {
        repository.delete(expense);
        operationSuccessful.setValue(true);
    }

    public LiveData<Expense> getExpense() {
        return expense;
    }

    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }
} 