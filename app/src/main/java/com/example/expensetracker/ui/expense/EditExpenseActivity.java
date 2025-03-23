package com.example.expensetracker.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.databinding.ActivityEditExpenseBinding;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.viewmodel.EditExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {
    private ActivityEditExpenseBinding binding;
    private EditExpenseViewModel viewModel;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String expenseId;
    private Expense currentExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Expense");

        expenseId = getIntent().getStringExtra("expense_id");
        if (expenseId == null) {
            Toast.makeText(this, "Error loading expense", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(EditExpenseViewModel.class);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        setupViews();
        setupObservers();
        viewModel.loadExpense(expenseId);
    }

    private void setupViews() {
        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            viewModel.getCategories()
        );
        binding.categorySpinner.setAdapter(categoryAdapter);

        // Setup date picker
        binding.dateEditText.setOnClickListener(v -> showDatePicker());

        // Setup buttons
        binding.updateButton.setOnClickListener(v -> updateExpense());
        binding.deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void setupObservers() {
        viewModel.getExpense().observe(this, expense -> {
            if (expense != null) {
                currentExpense = expense;
                binding.amountEditText.setText(String.valueOf(expense.getAmount()));
                binding.categorySpinner.setText(expense.getCategory());
                binding.descriptionEditText.setText(expense.getDescription());
                calendar.setTime(expense.getDate());
                binding.dateEditText.setText(dateFormat.format(calendar.getTime()));
            }
        });

        viewModel.getOperationSuccessful().observe(this, success -> {
            if (success) {
                finish();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.dateEditText.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateExpense() {
        String amountStr = binding.amountEditText.getText().toString();
        String category = binding.categorySpinner.getText().toString();
        String description = binding.descriptionEditText.getText().toString();
        Date date = calendar.getTime();

        if (amountStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.updateButton.setEnabled(false);
            binding.deleteButton.setEnabled(false);
            viewModel.updateExpense(expenseId, amount, category, description, date);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete", (dialog, which) -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.updateButton.setEnabled(false);
                binding.deleteButton.setEnabled(false);
                viewModel.deleteExpense(currentExpense);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 