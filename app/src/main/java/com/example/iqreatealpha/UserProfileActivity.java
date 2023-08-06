package com.example.iqreatealpha;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    private StorageReference storageReference;
    private ProfileAdapter profileAdapter;

    private ImageView profilePicture;
    private TextView profileUsername;
    private TextView profileUseremail;
    private TextView profileProfession;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        profileUsername = findViewById(R.id.profile_username);
        profileUseremail = findViewById(R.id.profile_useremail);
        profileProfession = findViewById(R.id.profile_profession);
        gridView = findViewById(R.id.gridView);

        // Set up the GridView and ProfileAdapter
        profileAdapter = new ProfileAdapter(this);
        gridView.setAdapter(profileAdapter);

        // Load the user's profile data from Firestore
        loadUserProfileData();

        // Set onClick listener on the profileUseremail TextView
        profileUseremail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the email address from the TextView
                String email = profileUseremail.getText().toString().trim();

                // Check if the email address is valid
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Create an intent to send an email
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + email));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding Your Profile");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear User,\n\nI have a question about your profile.\n\nBest regards,");

                    // Check if there is an app that can handle the intent
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        // Request permission to open an email client
                        startActivity(Intent.createChooser(emailIntent, "Send Email"));
                    } else {
                        // No app can handle the intent
                        Toast.makeText(UserProfileActivity.this, "No email app found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Invalid email address
                    Toast.makeText(UserProfileActivity.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set item click listener for the grid view
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the clicked item data
            Model clickedItem = (Model) profileAdapter.getItem(position);
            if (clickedItem != null) {
                // Get the image description and URL
                String imageDescription = clickedItem.getImageDescription();
                String imageUrl = clickedItem.getImageUrl();
                String imageLink = clickedItem.getLink();

                // Open FullScreenOtherActivity with the image description and URL
                Intent intent = new Intent(UserProfileActivity.this, FullScreenOtherActivity.class);
                intent.putExtra("imageDescription", imageDescription);
                intent.putExtra("imageUrl", imageUrl);
                intent.putExtra("imageLink", imageLink);
                startActivity(intent);
            }
        });


        // Find the share_profile button
        Button shareProfileButton = findViewById(R.id.share_profile);

        // Set click listener for the share_profile button
        shareProfileButton.setOnClickListener(v -> {
            // Get the user's data
            String username = profileUsername.getText().toString();
            String email = profileUseremail.getText().toString();
            String profession = profileProfession.getText().toString();

            // Create the share message
            String shareMessage = "Check out my profile on WeezCorp:\n\n"
                    + "Username: " + username + "\n"
                    + "Email: " + email + "\n"
                    + "Profession: " + profession + "\n\n"
                    + "Join me on WeezCorp!";

            // Create an intent to share the message
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Profile");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            // Start the share activity
            startActivity(Intent.createChooser(shareIntent, "Share Profile"));
        });
    }

    private void loadUserProfileData() {
        // Get the user ID from the intent
        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            // Set up the user data listener
            userListener = db.collection("Users").document(userId)
                    .addSnapshotListener((snapshot, exception) -> {
                        if (exception != null) {
                            // Handle any errors
                            Toast.makeText(UserProfileActivity.this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Get the user data from the snapshot
                            String username = snapshot.getString("username");
                            String email = snapshot.getString("email");
                            String userProfession = snapshot.getString("userProfession");
                            String profilePictureUrl = snapshot.getString("profilePictureUrl");

                            // Update the views with the retrieved data
                            profileUsername.setText(username);
                            profileUseremail.setText(email);
                            profileProfession.setText(userProfession);

                            // Load the profile picture using Glide
                            RequestOptions requestOptions = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL);
                            Glide.with(UserProfileActivity.this)
                                    .load(profilePictureUrl)
                                    .apply(requestOptions)
                                    .into(profilePicture);

                            // Load the profile images from Firestore
                            loadProfileImages(userId);
                        }
                    });
        } else {
            // Show an error message or handle the null user ID case
            Toast.makeText(UserProfileActivity.this, "User ID is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImages(String userId) {
        db.collection("Images")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Retrieve the images from Firestore
                    List<Model> images = queryDocumentSnapshots.toObjects(Model.class);
                    // Set the retrieved images in the ProfileAdapter
                    profileAdapter.setImages(images);

                    // Set the number of projects uploaded
                    TextView numberOfProjectsUploaded = findViewById(R.id.number_of_projects_uploaded);
                    numberOfProjectsUploaded.setText(String.valueOf(profileAdapter.getCount()));
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile images.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the user data listener
        if (userListener != null) {
            userListener.remove();
        }
    }
}
