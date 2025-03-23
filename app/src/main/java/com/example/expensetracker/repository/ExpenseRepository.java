package com.example.expensetracker.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.expensetracker.data.AppDatabase;
import com.example.expensetracker.data.ExpenseDao;
import com.example.expensetracker.model.Expense;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private ExpenseDao expenseDao;
    private FirebaseFirestore firestore;
    private ExecutorService executorService;
    private static final String COLLECTION_EXPENSES = "expenses";

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        expenseDao = db.expenseDao();
        firestore = FirebaseFirestore.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Expense expense) {
        executorService.execute(() -> {
            // Save locally
            expenseDao.insert(expense);
            
            // Save to Firestore
            firestore.collection(COLLECTION_EXPENSES)
                    .document(expense.getId())
                    .set(expense)
                    .addOnSuccessListener(aVoid -> {
                        expense.setSynced(true);
                        executorService.execute(() -> expenseDao.update(expense));
                    });
        });
    }

    public void update(Expense expense) {
        executorService.execute(() -> {
            expenseDao.update(expense);
            firestore.collection(COLLECTION_EXPENSES)
                    .document(expense.getId())
                    .set(expense);
        });
    }

    public void delete(Expense expense) {
        executorService.execute(() -> {
            expenseDao.delete(expense);
            firestore.collection(COLLECTION_EXPENSES)
                    .document(expense.getId())
                    .delete();
        });
    }

    public LiveData<List<Expense>> getAllExpenses(String userId) {
        return expenseDao.getAllExpenses(userId);
    }

    public LiveData<List<Expense>> getExpensesByCategory(String userId, String category) {
        return expenseDao.getExpensesByCategory(userId, category);
    }

    public LiveData<List<Expense>> getExpensesByDateRange(String userId, Date startDate, Date endDate) {
        return expenseDao.getExpensesByDateRange(userId, startDate, endDate);
    }

    public LiveData<Double> getTotalExpenseForPeriod(String userId, Date startDate, Date endDate) {
        return expenseDao.getTotalExpenseForPeriod(userId, startDate, endDate);
    }

    public void syncExpenses() {
        executorService.execute(() -> {
            List<Expense> unsyncedExpenses = expenseDao.getUnsyncedExpenses();
            for (Expense expense : unsyncedExpenses) {
                firestore.collection(COLLECTION_EXPENSES)
                        .document(expense.getId())
                        .set(expense)
                        .addOnSuccessListener(aVoid -> {
                            expense.setSynced(true);
                            executorService.execute(() -> expenseDao.update(expense));
                        });
            }
        });
    }
} 