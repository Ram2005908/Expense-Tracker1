package com.example.expensetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.expensetracker.model.Expense;

import java.util.Date;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses(String userId);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category")
    LiveData<List<Expense>> getExpensesByCategory(String userId, String category);

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    LiveData<List<Expense>> getExpensesByDateRange(String userId, Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpenseForPeriod(String userId, Date startDate, Date endDate);

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    List<Expense> getUnsyncedExpenses();
} 