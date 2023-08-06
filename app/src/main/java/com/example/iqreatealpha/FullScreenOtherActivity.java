package com.example.iqreatealpha;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class FullScreenOtherActivity extends AppCompatActivity {

    private String imageDescription;
    private String imageUrl;
    private String imageLink;
    private FirebaseFirestore firestore;
    private TextView averageRatingTextView;
    private TextView projectDescriptionTextView;
    private LinearLayout projectEditsLayout;
    private LinearLayout projectEdits1Layout;

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

        // Find the TextView for averageRating and projectDescription
        averageRatingTextView = findViewById(R.id.averageRating);
        projectDescriptionTextView = findViewById(R.id.project_description);

        // Find the layouts for project_edits and project_edits1
        projectEditsLayout = findViewById(R.id.project_edits);
        projectEdits1Layout = findViewById(R.id.project_edits1);

        // Set up the share button
        Button shareButton = findViewById(R.id.project_share);
        shareButton.setOnClickListener(v -> shareImage());

        // Set up the project view button
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
                    Toast.makeText(FullScreenOtherActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error occurred while querying the image
                Toast.makeText(FullScreenOtherActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
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
