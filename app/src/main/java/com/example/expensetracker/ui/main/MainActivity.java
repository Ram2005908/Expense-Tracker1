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
import com.example.expensetracker.viewmodel.MainViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

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
            // Implement budget warning notification
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
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 