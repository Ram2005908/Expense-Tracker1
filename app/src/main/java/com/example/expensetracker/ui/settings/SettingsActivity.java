package com.example.expensetracker.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.databinding.ActivitySettingsBinding;
import com.example.expensetracker.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.getMonthlyBudget().observe(this, budget -> {
            binding.budgetEditText.setText(String.valueOf(budget));
        });

        viewModel.getNotificationsEnabled().observe(this, enabled -> {
            binding.notificationSwitch.setChecked(enabled);
        });

        viewModel.getOperationSuccessful().observe(this, success -> {
            if (success != null) {
                if (success) {
                    Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.saveBudgetButton.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String budgetStr = binding.budgetEditText.getText().toString();
        if (budgetStr.isEmpty()) {
            Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);
            boolean notifications = binding.notificationSwitch.isChecked();
            viewModel.saveSettings(budget, notifications);
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