package com.example.iqreatealpha;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class DashboardActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private static final long DELAY_REPORT_GENERATION = 10000; // 10 seconds

    private boolean reportGenerated = false;
    private FirebaseFirestore firestore;
    private String userEmail;

    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firestore = FirebaseFirestore.getInstance();

        navigationView = findViewById(R.id.navigation_bottom);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        navigationView.setSelectedItemId(R.id.nav_home);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Fragment fragment = null;

                if (itemId == R.id.nav_home) {
                    fragment = new HomeFragment();
                } else if (itemId == R.id.nav_search) {
                    fragment = new SearchFragment();
                } else if (itemId == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                } else if (itemId == R.id.nav_Settings) {
                    fragment = new SettingsFragment();
                } else if (itemId == R.id.upload) {
                    checkPermissionsAndOpenGallery();
                    return true;
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                    return true;
                }

                return false;
            }
        });

        // Inside the onCreate() method of DashboardActivity

        // Retrieve the user ID from the intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            String userId = intent.getStringExtra("userId");
            if (userId != null) {
                // Fetch the user email and generate the system report
                fetchUserEmail(userId);
            }
        }
    }

    private void fetchUserEmail(String userId) {
        firestore.collection("Users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            userEmail = documentSnapshot.getString("email");
                            if (userEmail != null) {
                                // Generate the system report after a delay
                                // using a Handler
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        generateSystemReport(userId, userEmail);
                                    }
                                }, DELAY_REPORT_GENERATION);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch user email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startUploadActivity();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void startUploadActivity() {
        Intent intent = new Intent(DashboardActivity.this, UploadActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUploadActivity();
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Handle the selected image from the gallery
            if (data != null && data.getData() != null) {
                // Process the selected image here
            }
        }
    }

    private void generateSystemReport(String userId, String email) {
        long appLaunchTime = System.currentTimeMillis();

        // Create a Data object with the required parameters
        Data inputData = new Data.Builder()
                .putString("userId", userId)
                .putString("email", email)
                .putLong("appLaunchTime", appLaunchTime)
                .build();

        // Schedule the SystemReportWorker to run with the provided input data immediately
        OneTimeWorkRequest reportWorkRequest = new OneTimeWorkRequest.Builder(SystemReportWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(reportWorkRequest);

        // Save the scheduled report generation status
        reportGenerated = true;
    }

}
