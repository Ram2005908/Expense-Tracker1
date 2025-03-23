package com.example.expensetracker.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.databinding.ActivityAddExpenseBinding;
import com.example.expensetracker.viewmodel.AddExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private ActivityAddExpenseBinding binding;
    private AddExpenseViewModel viewModel;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Expense");

        viewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        setupViews();
        setupObservers();
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
        binding.dateEditText.setText(dateFormat.format(calendar.getTime()));
        binding.dateEditText.setOnClickListener(v -> showDatePicker());

        // Setup save button
        binding.saveButton.setOnClickListener(v -> saveExpense());
    }

    private void setupObservers() {
        viewModel.getSaveSuccessful().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
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

    private void saveExpense() {
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
            binding.saveButton.setEnabled(false);
            viewModel.saveExpense(amount, category, description, date);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
        }
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