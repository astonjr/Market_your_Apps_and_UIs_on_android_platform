package com.example.iqreatealpha;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class FullScreenOtherActivity extends AppCompatActivity {

    private String imageDescription;
    private String imageUrl;
    private String imageLink;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_other);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the image details from the Intent
        imageDescription = getIntent().getStringExtra("imageDescription");
        imageUrl = getIntent().getStringExtra("imageUrl");
        imageLink = getIntent().getStringExtra("imageLink");

        // Load and display the image using Glide library
        ImageView imageView = findViewById(R.id.fullscreen_image);
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        // Set up the share button
        Button shareButton = findViewById(R.id.project_share);
        shareButton.setOnClickListener(v -> shareImage());

        // Set up the project view button
        Button projectViewButton = findViewById(R.id.project_view);
        projectViewButton.setOnClickListener(v -> openProjectView());

        // Set click listener for the ImageView
        imageView.setOnClickListener(v -> toggleButtonsVisibility(shareButton, projectViewButton));
    }

    private boolean buttonsVisible = true;

    private void toggleButtonsVisibility(View... buttons) {
        if (buttonsVisible) {
            // Buttons are visible, animate them to drop down
            for (View button : buttons) {
                button.animate()
                        .translationY(button.getHeight()) // Move the button down by its height
                        .alpha(0f) // Fade out the button
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // Set the buttons as invisible after animation
                                button.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        } else {
            // Buttons are not visible, animate them to reappear
            for (View button : buttons) {
                button.setVisibility(View.VISIBLE); // Make the button visible before animating
                button.setAlpha(0f); // Set initial alpha to 0 (fully transparent)
                button.setTranslationY(button.getHeight()); // Move the button down by its height

                button.animate()
                        .translationY(0f) // Move the button back to its original position
                        .alpha(1f) // Fade in the button
                        .setListener(null); // Remove the listener to prevent conflicts
            }
        }

        buttonsVisible = !buttonsVisible;
    }

    private void shareImage() {
        // Create the share message
        String shareMessage = "Check out this project:\n\n"
                + "Description: " + imageDescription + "\n"
                + "Link: " + imageLink + "\n\n"
                + "Join WeezCorp to easily discover projects like this!";

        // Create an intent to share the message
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Project");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Start the share activity
        startActivity(Intent.createChooser(shareIntent, "Share Project"));
    }

    private void openProjectView() {
        // Start the WebViewActivity with the project URL
        Intent intent = WebViewActivity.createIntent(FullScreenOtherActivity.this, imageLink);
        startActivity(intent);
    }
}
