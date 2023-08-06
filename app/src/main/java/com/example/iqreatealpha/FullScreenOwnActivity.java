package com.example.iqreatealpha;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FullScreenOwnActivity extends AppCompatActivity {

    private String imageUrl;
    private String imageDescription;
    private String imageLink;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_fullscreen);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get the image details from the Intent
        imageUrl = getIntent().getStringExtra("imageUrl");
        imageDescription = getIntent().getStringExtra("imageDescription");
        imageLink = getIntent().getStringExtra("imageLink");

        // Load and display the image using Glide library
        ImageView imageView = findViewById(R.id.fullscreen_image);
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        // Set click listeners for the buttons
        Button deleteButton = findViewById(R.id.project_delete);
        deleteButton.setOnClickListener(v -> deleteImage());

        Button shareButton = findViewById(R.id.project_share);
        shareButton.setOnClickListener(v -> shareImage());

        Button projectViewButton = findViewById(R.id.project_view);
        projectViewButton.setOnClickListener(v -> openProjectView());

        // Set click listener for the ImageView
        imageView.setOnClickListener(v -> toggleButtonsVisibility(deleteButton, shareButton, projectViewButton));
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


    private void deleteImage() {
        // Retrieve the document reference based on the image URL
        Query query = firestore.collection("Images").whereEqualTo("imageUrl", imageUrl);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Get the document reference of the image
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    String documentId = document.getId();
                    DocumentReference documentReference = firestore.collection("Images").document(documentId);

                    // Delete the document from Firestore
                    documentReference.delete()
                            .addOnSuccessListener(aVoid -> {
                                // Image deleted successfully
                                Toast.makeText(FullScreenOwnActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                                // Exit the fullscreen view
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Failed to delete the image
                                Toast.makeText(FullScreenOwnActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Image not found
                    Toast.makeText(FullScreenOwnActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error occurred while querying the image
                Toast.makeText(FullScreenOwnActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareImage() {
        // Create an intent to share the image, description, and link
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        // Set the URI of the image file to be shared
        Uri imageUri = Uri.parse(imageUrl);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        // Set the text to be shared (description and link)
        String shareText = "Check out my project:\n\n"
                + "Description: " + imageDescription + "\n"
                + "Link: " + imageLink + "\n\n"
                + "Join me on WeezCorp!";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Start the share activity
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void openProjectView() {
        // Start the WebViewActivity with the project URL
        Intent intent = WebViewActivity.createIntent(FullScreenOwnActivity.this, imageLink);
        startActivity(intent);
    }
}
