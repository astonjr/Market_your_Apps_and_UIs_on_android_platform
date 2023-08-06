package com.example.iqreatealpha;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class SettingsFragment extends Fragment {

    private Button editAccountButton;
    private Button logoutButton;
    private Button deleteAccountButton;
    private Button shareProfileButton;
    private Button systemReportButton; // Declare systemReportButton
    private FirebaseAuth firebaseAuth;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        editAccountButton = view.findViewById(R.id.edit_account_button);
        logoutButton = view.findViewById(R.id.logout_button);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
        shareProfileButton = view.findViewById(R.id.account_share_button); // Initialize shareProfileButton
        systemReportButton = view.findViewById(R.id.system_report); // Initialize systemReportButton

        // Get the current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail().equals("junioraston23@gmail.com")) {
            // Show the systemReportButton only for the specified user
            systemReportButton.setVisibility(View.VISIBLE);
        } else {
            // Hide the systemReportButton for other users
            systemReportButton.setVisibility(View.GONE);
        }

        editAccountButton.setOnClickListener(v -> {
            // Open EditProfileFragment
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new EditProfileFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        logoutButton.setOnClickListener(v -> {
            // Show a confirmation dialog before logging out
            showLogoutConfirmationDialog();
        });

        deleteAccountButton.setOnClickListener(v -> {
            // Show a confirmation dialog before deleting the account
            showDelConfirmationDialog();
        });

        shareProfileButton.setOnClickListener(v -> {
            // Get the current user's data
            if (currentUser != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = currentUser.getUid();

                // Get the user document from the "Users" collection based on the current user's ID
                db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = currentUser.getDisplayName();
                            String email = currentUser.getEmail();
                            String profession = document.getString("userProfession");

                            // Create the share message
                            String shareMessage = "Check out my profile:\n\n"
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
                        } else {
                            // Document doesn't exist, handle accordingly
                            Toast.makeText(requireContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the error if the Firestore query fails
                        Toast.makeText(requireContext(), "Error fetching user data.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        systemReportButton.setOnClickListener(v -> {
            // Open ReportViewActivity
            startActivity(new Intent(requireContext(), ReportViewActivity.class));
        });


        return view;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Sign out the user and open MainActivity
                firebaseAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish(); // Close the current activity/fragment
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void showDelConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Account");
        builder.setMessage("Account and user Data will be permanently deleted" + "\n" + "Are you sure you want to delete your account?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the current logged in user from Firebase Authentication
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Delete user document from "Users" collection in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference usersCollection = db.collection("Users");
                    usersCollection.document(user.getUid()).delete()
                            .addOnSuccessListener(aVoid -> {
                                // User document deleted successfully
                                // Delete user images from "Images" collection in Firestore
                                CollectionReference imagesCollection = db.collection("Images");
                                Query userImagesQuery = imagesCollection.whereEqualTo("userId", user.getUid());
                                userImagesQuery.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        List<DocumentSnapshot> imageDocuments = task.getResult().getDocuments();
                                        for (DocumentSnapshot imageDocument : imageDocuments) {
                                            imageDocument.getReference().delete();
                                        }
                                        // Account deletion successful
                                        user.delete()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        // User account, Firestore document, and images deleted
                                                        Intent intent = new Intent(getActivity(), SignUpActivity.class);
                                                        startActivity(intent);
                                                        getActivity().finish(); // Close the current activity/fragment
                                                    } else {
                                                        // Handle account deletion failure
                                                    }
                                                });
                                    } else {
                                        // Handle Firestore query failure
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                // Handle Firestore document deletion failure
                            });
                }
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
