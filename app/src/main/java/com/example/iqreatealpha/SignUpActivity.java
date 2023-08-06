package com.example.iqreatealpha;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private EditText signupUsername, signupEmail, signupPassword, signupReenterPassword, signupUserProfession;
    private Button signupButton;
    private TextView loginRedirectText;
    private ProgressBar signupProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        signupUsername = findViewById(R.id.signup_username);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupReenterPassword = findViewById(R.id.signup_reenter_password);
        signupUserProfession = findViewById(R.id.signup_userprofession);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupProgressBar = findViewById(R.id.signup_progressBar);

        signupProgressBar.setVisibility(View.GONE);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = signupUsername.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                String reenterPassword = signupReenterPassword.getText().toString().trim();
                String userProfession = signupUserProfession.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || reenterPassword.isEmpty() || userProfession.isEmpty()) {
                    showAlert("Please fill in all the fields.");
                    return;
                }

                if (!password.equals(reenterPassword)) {
                    showAlert("Passwords do not match.");
                    return;
                }

                signupProgressBar.setVisibility(View.VISIBLE);
                signUpUser(username, email, password, userProfession);
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
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

    private void signUpUser(final String username, final String email, final String password, final String userProfession) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username) // Set the display name
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> profileTask) {
                                            if (profileTask.isSuccessful()) {
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> emailTask) {
                                                                signupProgressBar.setVisibility(View.GONE);
                                                                if (emailTask.isSuccessful()) {
                                                                    String userId = user.getUid();
                                                                    String hashedPassword = hashPassword(password); // Hash the password
                                                                    saveUserDataToFirestore(userId, username, email, hashedPassword, userProfession);
                                                                    showAlert("Registration successful. Please check your email to verify your account.");

                                                                    // Retrieve the FCM device token
                                                                    FirebaseMessaging.getInstance().getToken()
                                                                            .addOnCompleteListener(tokenTask -> {
                                                                                if (tokenTask.isSuccessful()) {
                                                                                    String deviceToken = tokenTask.getResult();
                                                                                    saveDeviceTokenToFirestore(userId, deviceToken);

                                                                                } else {
                                                                                    showAlert("Failed to retrieve device token: " + tokenTask.getException());
                                                                                }
                                                                            });
                                                                } else {
                                                                    showAlert("Failed to send verification email: " + emailTask.getException().getMessage());
                                                                }
                                                            }
                                                        });
                                            } else {
                                                signupProgressBar.setVisibility(View.GONE);
                                                showAlert("Failed to update display name: " + profileTask.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            signupProgressBar.setVisibility(View.GONE);
                            showAlert("Signup Failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String username, String email, String password, String userProfession) {
        DocumentReference userRef = firestore.collection("Users").document(userId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String signupDate = dateFormat.format(new Date());

        // Create a Map object to hold the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password);
        userData.put("userProfession", userProfession);
        userData.put("signupDate", signupDate);

        userRef.set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        } else {
                            showAlert("Signup Failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveDeviceTokenToFirestore(String userId, String deviceToken) {
        DocumentReference userRef = firestore.collection("Users").document(userId);

        // Create a Map object to hold the device token data
        Map<String, Object> deviceTokenData = new HashMap<>();
        deviceTokenData.put("deviceToken", deviceToken);

        userRef.set(deviceTokenData, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Device Token", "Device token saved successfully");
                        } else {
                            showAlert("Failed to save device token: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
