package com.example.expensetracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.databinding.ActivityLoginBinding;
import com.example.expensetracker.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (auth.getCurrentUser() != null) {
            startMainActivity();
            finish();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> performLogin());
        binding.registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                binding.progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    startMainActivity();
                    finish();
                } else {
                    Toast.makeText(this, "Authentication failed: " + 
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 