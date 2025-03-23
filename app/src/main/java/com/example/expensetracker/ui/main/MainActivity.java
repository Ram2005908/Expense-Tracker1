package com.example.expensetracker.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetracker.R;
import com.example.expensetracker.adapter.ExpenseAdapter;
import com.example.expensetracker.databinding.ActivityMainBinding;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.ui.auth.LoginActivity;
import com.example.expensetracker.ui.expense.AddExpenseActivity;
import com.example.expensetracker.ui.expense.EditExpenseActivity;
import com.example.expensetracker.ui.settings.SettingsActivity;
import com.example.expensetracker.viewmodel.MainViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.graphics.Color;
import android.graphics.ColorStateList;
import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ExpenseAdapter adapter;
    private static final String BUDGET_ALERT_CHANNEL_ID = "budget_alert_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Expense Tracker");

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupChart();
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(this);
        binding.recentExpensesRecyclerView.setAdapter(adapter);
        binding.recentExpensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupObservers() {
        viewModel.getRecentExpenses().observe(this, expenses -> {
            adapter.setExpenses(expenses);
            updateChart(expenses);
        });

        viewModel.getCurrentMonthExpenses().observe(this, totalExpense -> {
            viewModel.getMonthlyBudget().observe(this, budget -> {
                if (budget != null && totalExpense != null) {
                    updateBudgetProgress(totalExpense, budget);
                }
            });
        });
    }

    private void setupClickListeners() {
        binding.addExpenseFab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
        });
    }

    private void updateBudgetProgress(double totalExpense, double budget) {
        int progress = (int) ((totalExpense / budget) * 100);
        binding.budgetProgressBar.setProgress(progress);
        binding.budgetAmountText.setText(String.format("$%.2f / $%.2f", totalExpense, budget));

        // Show warning if over budget
        if (totalExpense > budget) {
            showBudgetAlert(totalExpense, budget);
            binding.budgetProgressBar.setProgressTintList(
                ColorStateList.valueOf(getColor(R.color.design_default_color_error)));
        } else {
            binding.budgetProgressBar.setProgressTintList(
                ColorStateList.valueOf(getColor(R.color.design_default_color_primary)));
        }

        // Show warning when approaching budget (90%)
        if (totalExpense >= budget * 0.9 && totalExpense <= budget) {
            showBudgetWarning(totalExpense, budget);
        }
    }

    private void setupChart() {
        binding.expenseChart.getDescription().setEnabled(false);
        binding.expenseChart.setDrawHoleEnabled(true);
        binding.expenseChart.setHoleRadius(58f);
        binding.expenseChart.setTransparentCircleRadius(61f);
        binding.expenseChart.setDrawCenterText(true);
        binding.expenseChart.setCenterText("Expenses");
        binding.expenseChart.setRotationEnabled(false);
    }

    private void updateChart(List<Expense> expenses) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        binding.expenseChart.setData(data);
        binding.expenseChart.invalidate();
    }

    @Override
    public void onExpenseClick(Expense expense) {
        Intent intent = new Intent(this, EditExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_set_budget) {
            showBudgetDialog();
            return true;
        }
        else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showBudgetDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_set_budget, null);
        TextInputEditText budgetInput = view.findViewById(R.id.budgetEditText);
        
        builder.setTitle("Set Monthly Budget")
               .setView(view)
               .setPositiveButton("Save", (dialog, which) -> {
                   String budgetStr = budgetInput.getText().toString();
                   if (!budgetStr.isEmpty()) {
                       double budget = Double.parseDouble(budgetStr);
                       viewModel.setMonthlyBudget(budget);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    private void showBudgetAlert(double totalExpense, double budget) {
        String title = "Budget Exceeded!";
        String message = String.format(Locale.getDefault(),
            "You've exceeded your monthly budget of $%.2f by $%.2f",
            budget, totalExpense - budget);

        // Show in-app alert
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("View Budget", (dialog, which) -> {
                showBudgetDialog();
            })
            .setNegativeButton("Dismiss", null)
            .show();

        // Show notification
        showNotification(title, message);
    }

    private void showBudgetWarning(double totalExpense, double budget) {
        String title = "Budget Warning";
        String message = String.format(Locale.getDefault(),
            "You're approaching your monthly budget. Used: $%.2f of $%.2f (%.1f%%)",
            totalExpense, budget, (totalExpense/budget) * 100);

        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                BUDGET_ALERT_CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Alerts for budget limits");
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent for when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BUDGET_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
} 