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

import com.example.expensetracker.R;
import com.example.expensetracker.databinding.ActivityEditExpenseBinding;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.viewmodel.ExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {
    private ActivityEditExpenseBinding binding;
    private ExpenseViewModel viewModel;
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

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        setupSpinner();
        setupClickListeners();
        setupViewModel();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_dropdown_item_1line);
        binding.categorySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.dateButton.setOnClickListener(v -> showDatePicker());
        
        binding.updateButton.setOnClickListener(v -> updateExpense());
        binding.deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        
        expenseId = getIntent().getStringExtra("expense_id");
        if (expenseId != null) {
            viewModel.getExpense(expenseId).observe(this, this::populateExpense);
        }
    }

    private void populateExpense(Expense expense) {
        if (expense != null) {
            currentExpense = expense;
            binding.amountEditText.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
            binding.categorySpinner.setText(expense.getCategory());
            binding.descriptionEditText.setText(expense.getDescription());
            calendar.setTime(expense.getDate());
            updateDateButtonText();
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButtonText();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
           calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtonText() {
        binding.dateButton.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateExpense() {
        if (!validateInput()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        
        double amount = Double.parseDouble(binding.amountEditText.getText().toString());
        String category = binding.categorySpinner.getText().toString();
        String description = binding.descriptionEditText.getText().toString();

        viewModel.updateExpense(expenseId, amount, category, description, calendar.getTime())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to update expense: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
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

    private boolean validateInput() {
        if (binding.amountEditText.getText().toString().trim().isEmpty()) {
            binding.amountEditText.setError("Amount is required");
            return false;
        }
        if (binding.categorySpinner.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.descriptionEditText.getText().toString().trim().isEmpty()) {
            binding.descriptionEditText.setError("Description is required");
            return false;
        }
        return true;
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