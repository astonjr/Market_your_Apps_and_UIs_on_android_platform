package com.example.iqreatealpha;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.iqreatealpha.databinding.FragmentProfileBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration userListener;
    private StorageReference storageReference;
    private ProfileAdapter profileAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        bottomNavigationView = getActivity().findViewById(R.id.navigation_bottom);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadProfileImages);

        // Set up the user data listener
        if (currentUser != null) {
            userListener = db.collection("Users").document(currentUser.getUid())
                    .addSnapshotListener((snapshot, exception) -> {
                        if (exception != null) {
                            // Handle any errors
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Get the user data from the snapshot
                            String username = snapshot.getString("username");
                            String email = snapshot.getString("email");
                            String profilePictureUrl = snapshot.getString("profilePictureUrl");
                            String userProfession = snapshot.getString("userProfession");

                            // Update the views with the retrieved data
                            binding.profileUsername.setText(username);
                            binding.profileUseremail.setText(email);
                            binding.profileProfession.setText(userProfession);

                            // Load the profile picture using Glide library
                            RequestOptions options = new RequestOptions()
                                    .placeholder(R.drawable.ic_add_profile_pic)
                                    .error(R.drawable.ic_add_profile_pic)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL);

                            Glide.with(requireContext())
                                    .load(profilePictureUrl)
                                    .apply(options)
                                    .into(binding.profilePicture);
                        }
                    });
        }

        // Set up the GridView and ProfileAdapter
        profileAdapter = new ProfileAdapter(requireContext());
        GridView gridView = binding.gridView;
        gridView.setAdapter(profileAdapter);

        // Load the profile images from Firestore
        loadProfileImages();

        // Set the number of projects uploaded
        TextView numberOfProjectsUploaded = view.findViewById(R.id.number_of_projects_uploaded);
        numberOfProjectsUploaded.setText(String.valueOf(profileAdapter.getCount()));

        // Set click listener for grid items (if needed)
        gridView.setOnItemClickListener((parent, view12, position, id) -> {
            // Get the selected image from the adapter
            Model selectedImage = (Model) profileAdapter.getItem(position);

            // Create an Intent to open a new activity for full-screen display
            Intent intent = new Intent(requireContext(), FullScreenOwnActivity.class);
            intent.putExtra("imageUrl", selectedImage.getImageUrl());
            intent.putExtra("imageDescription", selectedImage.getImageDescription());
            intent.putExtra("imageLink", selectedImage.getLink());
            startActivity(intent);
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 10; // Adjust this threshold as needed
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getChildAt(0) == null) {
                    return;
                }

                int currentScrollY = -view.getChildAt(0).getTop();
                int dy = currentScrollY - scrolledDistance;

                // Check the scroll direction
                if (dy > 0) {
                    // Scrolling down
                    if (controlsVisible) {
                        scrolledDistance += dy;
                        if (scrolledDistance > HIDE_THRESHOLD) {
                            hideBottomNavigationView();
                            controlsVisible = false;
                        }
                    }
                } else {
                    // Scrolling up
                    if (!controlsVisible) {
                        showBottomNavigationView();
                        controlsVisible = true;
                    }
                    scrolledDistance += dy;
                }

                // Handle edge cases
                if (currentScrollY == 0) {
                    showBottomNavigationView();
                    controlsVisible = true;
                    scrolledDistance = 0;
                }
            }
        });
    }

    private void hideBottomNavigationView() {
        bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(400).start();
    }

    private void showBottomNavigationView() {
        bottomNavigationView.animate().translationY(0).setDuration(400).start();
    }

    private void loadProfileImages() {
        if (currentUser != null) {
            db.collection("Images")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Retrieve the images from Firestore
                        List<Model> images = queryDocumentSnapshots.toObjects(Model.class);
                        // Set the retrieved images in the ProfileAdapter
                        profileAdapter.setImages(images);

                        // Update the number of projects uploaded
                        TextView numberOfProjectsUploaded = getView().findViewById(R.id.number_of_projects_uploaded);
                        numberOfProjectsUploaded.setText(String.valueOf(profileAdapter.getCount()));

                        // Stop the SwipeRefreshLayout refreshing animation
                        swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(requireContext(), "Failed to load profile images.", Toast.LENGTH_SHORT).show();
                        // Stop the SwipeRefreshLayout refreshing animation on failure
                        swipeRefreshLayout.setRefreshing(false);
                    });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the user data listener
        if (userListener != null) {
            userListener.remove();
        }
        binding = null; // Release the binding
    }
}
