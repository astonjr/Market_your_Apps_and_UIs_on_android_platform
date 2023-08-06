package com.example.iqreatealpha;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText resetEmail;
    private Button resetButton;
    private TextView backToLogin;
    private ProgressBar resetProgressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordreset);

        auth = FirebaseAuth.getInstance();

        resetEmail = findViewById(R.id.reset_email);
        resetButton = findViewById(R.id.password_reset_button);
        resetProgressBar = findViewById(R.id.login_progressBar);
        backToLogin = findViewById(R.id.back_to_login);

        resetProgressBar.setVisibility(View.GONE);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    showAlert("Please enter your email.");
                    return;
                }

                resetProgressBar.setVisibility(View.VISIBLE);
                resetPassword(email);
            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
            }
        });
    }

    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            showAlert("Password reset email sent. Please check your email to reset your password.");
                            startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
                        } else {
                            showAlert("Failed to send password reset email: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
