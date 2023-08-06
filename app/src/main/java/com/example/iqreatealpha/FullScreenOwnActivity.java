package com.example.iqreatealpha;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class FullScreenOwnActivity extends AppCompatActivity {

    private String imageUrl;
    private String imageDescription;
    private String imageLink;
    private FirebaseFirestore firestore;
    private TextView averageRatingTextView;
    private TextView projectDescriptionTextView;
    private LinearLayout projectEditsLayout;
    private LinearLayout projectEdits1Layout;

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

        // Find the TextView for averageRating and projectDescription
        averageRatingTextView = findViewById(R.id.averageRating);
        projectDescriptionTextView = findViewById(R.id.project_description);

        // Find the layouts for project_edits and project_edits1
        projectEditsLayout = findViewById(R.id.project_edits);
        projectEdits1Layout = findViewById(R.id.project_edits1);

        // Set click listeners for the buttons
        Button deleteButton = findViewById(R.id.project_delete);
        deleteButton.setOnClickListener(v -> deleteImage());

        Button shareButton = findViewById(R.id.project_share);
        shareButton.setOnClickListener(v -> shareImage());

        Button projectViewButton = findViewById(R.id.project_view);
        projectViewButton.setOnClickListener(v -> openProjectView());

        // Fetch the updated average rating and project description from Firestore and update the TextViews
        fetchAndUpdateAverageRatingAndDescription();

        // Set click listener for the ImageView to hide the project_edits and project_edits1 layouts
        imageView.setOnClickListener(v -> toggleLayoutsVisibility(projectEditsLayout, projectEdits1Layout));
    }

    private void fetchAndUpdateAverageRatingAndDescription() {
        // Retrieve the document reference based on the image URL
        Query query = firestore.collection("Images").whereEqualTo("imageUrl", imageUrl);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Get the document reference of the image
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    // Get the average rating value and project description from the document
                    double averageRating = document.getDouble("averageRating");
                    String description = document.getString("imageDescription");

                    // Update the averageRatingTextView and projectDescriptionTextView with the new values
                    averageRatingTextView.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
                    projectDescriptionTextView.setText(description);
                } else {
                    // Image not found or data not available
                    Toast.makeText(FullScreenOwnActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error occurred while querying the image
                Toast.makeText(FullScreenOwnActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean layoutsVisible = true;

    private void toggleLayoutsVisibility(View... layouts) {
        if (layoutsVisible) {
            // Layouts are visible, animate them to drop down
            for (View layout : layouts) {
                layout.animate()
                        .translationY(layout.getHeight()) // Move the layout down by its height
                        .alpha(0f) // Fade out the layout
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // Set the layout as invisible after animation
                                layout.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        } else {
            // Layouts are not visible, animate them to reappear
            for (View layout : layouts) {
                layout.setVisibility(View.VISIBLE); // Make the layout visible before animating
                layout.setAlpha(0f); // Set initial alpha to 0 (fully transparent)
                layout.setTranslationY(layout.getHeight()); // Move the layout down by its height

                layout.animate()
                        .translationY(0f) // Move the layout back to its original position
                        .alpha(1f) // Fade in the layout
                        .setListener(null); // Remove the listener to prevent conflicts
            }
        }

        layoutsVisible = !layoutsVisible;
    }



    private void deleteImage() {
        // Retrieve the document reference based on the image URL
        Query query = firestore.collection("Images").whereEqualTo("imageUrl", imageUrl);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // Get the document reference of the image
                    DocumentSnapshot imageDocument = querySnapshot.getDocuments().get(0);
                    String documentId = imageDocument.getId();

                    // First, delete the corresponding image rating documents from the "ImageRatings" collection
                    deleteImageRatingDocuments(documentId, new OnImageRatingDocumentsDeletedListener() {
                        @Override
                        public void onImageRatingDocumentsDeleted() {
                            // After the image rating documents are deleted, delete the image document from "Images" collection
                            firestore.collection("Images").document(documentId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Image document deleted successfully
                                        Toast.makeText(FullScreenOwnActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                                        // Exit the fullscreen view
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to delete the image document
                                        Toast.makeText(FullScreenOwnActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                    });
                        }
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

    private void deleteImageRatingDocuments(String imageId, OnImageRatingDocumentsDeletedListener listener) {
        // Delete the image rating documents from the "ImageRatings" collection
        firestore.collection("ImageRatings").document(imageId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Image rating documents deleted successfully
                    listener.onImageRatingDocumentsDeleted();
                })
                .addOnFailureListener(e -> {
                    // Failed to delete the image rating documents
                    Toast.makeText(FullScreenOwnActivity.this, "Failed to delete image rating documents", Toast.LENGTH_SHORT).show();
                });
    }

    // Callback interface for notifying when image rating documents are deleted
    private interface OnImageRatingDocumentsDeletedListener {
        void onImageRatingDocumentsDeleted();
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
