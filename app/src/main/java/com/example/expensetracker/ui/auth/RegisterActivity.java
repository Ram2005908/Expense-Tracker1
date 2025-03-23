package com.example.expensetracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.databinding.ActivityRegisterBinding;
import com.example.expensetracker.model.User;
import com.example.expensetracker.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.registerButton.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();
                    User user = new User(uid, email);
                    user.setDisplayName(name);

                    // Save user to Firestore
                    db.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            startMainActivity();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, 
                                "Error creating user profile", Toast.LENGTH_SHORT).show();
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this, 
                        "Registration failed: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 