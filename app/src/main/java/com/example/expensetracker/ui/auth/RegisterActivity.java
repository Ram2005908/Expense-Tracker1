package com.example.expensetracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.databinding.ActivityRegisterBinding;
import com.example.expensetracker.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        // Add debug logging
        Log.d(TAG, "Attempting to register with email: " + email);

        // Validate input
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        // Create user with email and password
        Log.d(TAG, "Starting Firebase authentication...");
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    String userId = auth.getCurrentUser().getUid();
                    Log.d(TAG, "Created user with ID: " + userId);

                    // Create user document in Firestore
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("monthlyBudget", 0.0);
                    user.put("notificationsEnabled", true);
                    user.put("createdAt", System.currentTimeMillis());

                    Log.d(TAG, "Creating Firestore document for user...");
                    firestore.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User profile created successfully");
                            // Sign out the user after registration
                            auth.signOut();
                            Toast.makeText(RegisterActivity.this, 
                                "Registration successful! Please login.", 
                                Toast.LENGTH_LONG).show();
                            finish(); // This will go back to LoginActivity
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error creating user profile", e);
                            // Sign out if profile creation fails
                            auth.signOut();
                            binding.progressBar.setVisibility(View.GONE);
                            binding.registerButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, 
                                "Failed to create user profile: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        });
                } else {
                    Exception exception = task.getException();
                    Log.e(TAG, "createUserWithEmail:failure", exception);
                    
                    binding.progressBar.setVisibility(View.GONE);
                    binding.registerButton.setEnabled(true);

                    String message;
                    if (exception != null) {
                        if (exception instanceof FirebaseAuthWeakPasswordException) {
                            message = "Password is too weak";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid email format";
                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            message = "Email already in use";
                        } else {
                            message = "Registration failed: " + exception.getMessage();
                            Log.e(TAG, "Detailed error: ", exception);
                        }
                    } else {
                        message = "Registration failed";
                        Log.e(TAG, "Unknown registration error");
                    }

                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Registration process failed", e);
                binding.progressBar.setVisibility(View.GONE);
                binding.registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, 
                    "Registration failed: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
} 