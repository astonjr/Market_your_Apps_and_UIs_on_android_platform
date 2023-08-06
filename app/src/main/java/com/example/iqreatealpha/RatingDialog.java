package com.example.iqreatealpha;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RatingDialog extends AppCompatDialogFragment {

    private SeekBar ratingSeekBar;
    private TextView ratingValue;
    private String imageId;
    private String currentUserId;
    private Context context;
    private List<Model> projectList;

    // Constructor to receive the projectList
    public RatingDialog() {
        // Empty constructor required for DialogFragment.
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);
        ratingSeekBar = view.findViewById(R.id.ratingSeekBar);
        ratingValue = view.findViewById(R.id.ratingValue);

        // Get the image ID from the arguments
        Bundle args = getArguments();
        if (args != null) {
            imageId = args.getString("imageId");
        }

        // Get the current user ID from Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Assign the currentUserId and context class members
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // User is not authenticated, show a toast message and dismiss the dialog
            Toast.makeText(getContext(), "Please log in first to rate.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        context = getContext();

        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rating = (float) progress;
                ratingValue.setText(String.format(Locale.getDefault(), "%.1f", rating));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view)
                .setTitle("Rate this project")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = (float) ratingSeekBar.getProgress();
                    submitRating(rating);
                });
        return builder.create();
    }


    private void submitRating(float rating) {
        // Get the reference to the Firestore collection "ImageRatings"
        CollectionReference imageRatingsRef = FirebaseFirestore.getInstance().collection("ImageRatings");

        // Get the reference to the Firestore collection "Images"
        CollectionReference imagesRef = FirebaseFirestore.getInstance().collection("Images");

        // Check if the current user's ID exists in the userIds array of the selected image
        imageRatingsRef.document(imageId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<String> userIds = (List<String>) document.get("userIds");
                    if (userIds != null && userIds.contains(currentUserId)) {
                        // User has already rated the image, show the toast
                        Toast.makeText(context, "You have already rated this project.", Toast.LENGTH_SHORT).show();
                        return; // Return without updating the rating
                    }
                }
                // Continue with the rating submission
                updateRatingInFirestore(imageRatingsRef, imagesRef, rating);
            } else {
                // Handle the error if the Firestore query fails
                Toast.makeText(context, "Error fetching image ratings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRatingInFirestore(CollectionReference imageRatingsRef, CollectionReference imagesRef, float rating) {
        // First, check if the "ImageRatings" document for the imageId exists
        imageRatingsRef.document(imageId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // The document already exists, get the existing userIDs array
                    List<String> userIds = (List<String>) document.get("userIds");

                    // Check if the userIds array is null, and initialize it if needed
                    if (userIds == null) {
                        userIds = new ArrayList<>();
                    }

                    // Check if the user's ID is already in the array
                    if (!userIds.contains(currentUserId)) {
                        // Add the user's ID to the existing array
                        userIds.add(currentUserId);

                        // Update the document in the "ImageRatings" collection with the updated userIDs array
                        Map<String, Object> ratingData = new HashMap<>();
                        ratingData.put("userIds", userIds);

                        imageRatingsRef.document(imageId)
                                .set(ratingData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    // Document updated successfully
                                    // ...

                                    // Continue with updating the rating information in the "Images" collection
                                    updateImageRatings(imageId, imagesRef, rating);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error if the Firestore update fails
                                    Toast.makeText(context, "Error updating image ratings.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // User has already rated the image, show the toast or handle accordingly
                        Toast.makeText(context, "You have already rated this project.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Document does not exist, create a new one with the current user's ID
                    List<String> userIds = new ArrayList<>();
                    userIds.add(currentUserId);
                    Map<String, Object> ratingData = new HashMap<>();
                    ratingData.put("userIds", userIds);

                    imageRatingsRef.document(imageId)
                            .set(ratingData)
                            .addOnSuccessListener(aVoid -> {
                                // Document created successfully
                                // ...

                                // Continue with updating the rating information in the "Images" collection
                                updateImageRatings(imageId, imagesRef, rating);
                            })
                            .addOnFailureListener(e -> {
                                // Handle the error if the Firestore update fails
                                Toast.makeText(context, "Error updating image ratings.", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                // Handle the error if the Firestore query fails
                Toast.makeText(context, "Error fetching image ratings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageRatings(String imageUniqueId, CollectionReference imagesRef, float rating) {
        // Retrieve the document from the "Images" collection with the given imageUniqueId
        imagesRef.document(imageUniqueId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the current values of numRatings and totalRatings
                        long numRatings = documentSnapshot.getLong("numRatings");
                        long totalRatings = documentSnapshot.getLong("totalRatings");

                        // Calculate the new total rating and increment the number of ratings by 1
                        totalRatings += (long) rating;
                        numRatings++;

                        // Calculate the new average rating
                        float averageRating = (float) totalRatings / numRatings;
                        averageRating = (float) (Math.round(averageRating * 10.0) / 10.0); // Round to one decimal place

                        // Update the fields in the document
                        Map<String, Object> newRatingData = new HashMap<>();
                        newRatingData.put("numRatings", numRatings);
                        newRatingData.put("totalRatings", totalRatings);
                        newRatingData.put("averageRating", averageRating);

                        // Update the document in the "Images" collection with the new rating information
                        imagesRef.document(imageUniqueId)
                                .update(newRatingData)
                                .addOnSuccessListener(aVoid -> {
                                    // Rating information updated successfully
                                    // ...

                                    // Dismiss the dialog
                                    dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error if the Firestore update fails
                                    Toast.makeText(context, "Error updating image ratings.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Handle the case where the document doesn't exist
                        Toast.makeText(context, "Document not found in Images collection.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error if the document retrieval fails
                    Toast.makeText(context, "Error retrieving document from Images collection.", Toast.LENGTH_SHORT).show();
                });
    }

}