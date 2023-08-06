package com.example.iqreatealpha;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Model> projectList;
    private Context context;
    private String currentUserId; // Current user's ID
    private OnLinkClickListener onLinkClickListener; // Listener for link clicks

    public void setData(List<Model> newData) {
        projectList.clear();
        projectList.addAll(newData);
        notifyDataSetChanged();
    }

    public ProjectAdapter(List<Model> projectList, Context context, String currentUserId) {
        this.projectList = projectList;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    // Setter method for the link click listener
    public void setOnLinkClickListener(OnLinkClickListener listener) {
        this.onLinkClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = projectList.get(position);
        holder.setImageUrl(model.getImageUrl());
        holder.setAppLink(model.getLink());
        holder.setAppOwner(model.getUsername(), model.getUserId());
        holder.setImageDescription(model.getImageDescription());

        // Get the average rating for the image and update the custom rating bar
        float averageRating = model.getAverageRating();
        holder.bindAverageRating(averageRating);

        // Get the average rating for the image and update the custom rating bar
        holder.setAverageRating(averageRating);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Link", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView appLinkTextView;
        private TextView appOwnerTextView;
        private TextView projectDescriptionTextView;
        private String appLink;
        private String userId;
        private GestureDetector gestureDetector;
        private LinearLayout customRatingBar;
        private TextView averageRatingTextView;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.project_image);
            appLinkTextView = itemView.findViewById(R.id.project_redirect_link);
            appOwnerTextView = itemView.findViewById(R.id.project_username);
            projectDescriptionTextView = itemView.findViewById(R.id.project_description);
            customRatingBar = itemView.findViewById(R.id.customRatingBar);
            averageRatingTextView = itemView.findViewById(R.id.averageRating);

            // Create GestureDetector for detecting double-tap
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Handle double-tap event here
                    // Get the image URL of the double-tapped image
                    String imageId = projectList.get(getAdapterPosition()).getImageId();
                    // Check if the user has already rated the image in Firestore
                    openRatingDialog(imageId);
                    return true;
                }
            });

            // Set touch listener for the ImageView to detect double-tap
            imageView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });

            // Set click listener for the app owner TextView
            appOwnerTextView.setOnClickListener(v -> {
                // Check if the user has the same userId as the one on the image
                if (userId != null && !userId.equals(currentUserId)) {
                    // Open the profile of the user
                    String ownerId = projectList.get(getAdapterPosition()).getUserId();
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("userId", ownerId);
                    context.startActivity(intent);
                }
            });

            appLinkTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onLinkClickListener != null) {
                        onLinkClickListener.onLinkClick(appLink);
                    }
                }
            });

            appLinkTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    copyToClipboard(appLink);
                    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            // Add hover tooltip
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TooltipCompat.setTooltipText(appLinkTextView, "Hold to copy link");
            }
        }

        private void openRatingDialog(String imageId) {
            // Get the reference to the Firestore collection "ImageRatings"
            CollectionReference imageRatingsRef = FirebaseFirestore.getInstance().collection("ImageRatings");

            // Get the current user ID from Firebase Authentication
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String currentUserId = currentUser.getUid();

                // Check if the current user's ID exists in the userIds array of the selected image
                imageRatingsRef.document(imageId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            List<String> userIds = (List<String>) document.get("userIds");
                            if (userIds != null && userIds.contains(currentUserId)) {
                                // User has already rated the image, show the toast
                                Toast.makeText(context, "You have already rated this project.", Toast.LENGTH_SHORT).show();
                            } else {
                                // User has not rated the image yet, open the rating dialog
                                RatingDialog ratingDialog = new RatingDialog();
                                ratingDialog.setImageId(imageId); // Set the imageId using the setter method
                                ratingDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "rating_dialog");
                            }
                        } else {
                            // Document doesn't exist, open the rating dialog for the new image
                            RatingDialog ratingDialog = new RatingDialog();
                            ratingDialog.setImageId(imageId); // Set the imageId using the setter method
                            ratingDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "rating_dialog");
                        }
                    } else {
                        // Handle the error if the Firestore query fails
                        Toast.makeText(context, "Error fetching image ratings.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // User is not authenticated, show a toast message
                Toast.makeText(context, "Please log in first to rate.", Toast.LENGTH_SHORT).show();
            }
        }



        public void bindAverageRating(float averageRating) {
            // Assuming you have a TextView named averageRatingTextView
            averageRatingTextView.setText(String.format("%.1f", averageRating));
        }

        public void setImageUrl(String imageUrl) {
            // Use Picasso or any other image loading library to load the image into the image view
            Picasso.get().load(imageUrl).into(imageView);
        }

        public void setAppLink(String appLink) {
            this.appLink = appLink;
        }

        public void setAppOwner(String appOwner, String userId) {
            appOwnerTextView.setText(appOwner);
            this.userId = userId;
        }

        public void setImageDescription(String imageDescription) {
            projectDescriptionTextView.setText(imageDescription);
        }

        public void setAverageRating(float averageRating) {

            int roundedRating = Math.round(averageRating);
            // Update the custom rating bar based on the rounded rating
            switch (roundedRating) {
                case 1:
                    setCustomRatingBar(1, 0, 0, 0, 0);
                    break;
                case 2:
                    setCustomRatingBar(1, 1, 0, 0, 0);
                    break;
                case 3:
                    setCustomRatingBar(1, 1, 1, 0, 0);
                    break;
                case 4:
                    setCustomRatingBar(1, 1, 1, 1, 0);
                    break;
                case 5:
                    setCustomRatingBar(1, 1, 1, 1, 1);
                    break;
                default:
                    setCustomRatingBar(0, 0, 0, 0, 0);
                    break;
            }
        }

        private void setCustomRatingBar(int star1, int star2, int star3, int star4, int star5) {
            ImageView star1View = itemView.findViewById(R.id.star1);
            ImageView star2View = itemView.findViewById(R.id.star2);
            ImageView star3View = itemView.findViewById(R.id.star3);
            ImageView star4View = itemView.findViewById(R.id.star4);
            ImageView star5View = itemView.findViewById(R.id.star5);

            star1View.setImageResource(star1 == 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            star2View.setImageResource(star2 == 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            star3View.setImageResource(star3 == 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            star4View.setImageResource(star4 == 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
            star5View.setImageResource(star5 == 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        }

    }

    // Interface for handling link clicks
    public interface OnLinkClickListener {

        void onLinkClick(String url);
    }
}
