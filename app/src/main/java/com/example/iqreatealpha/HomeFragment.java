package com.example.iqreatealpha;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements ProjectAdapter.OnLinkClickListener {

    private ArrayList<Model> list;
    private ProjectAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private String currentUserId; // Current user's ID
    private FirebaseAuth firebaseAuth;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "Images";
    private static final int PERMISSION_REQUEST_INTERNET = 123;
    private static final String SAVED_DATA_LIST = "saved_data_list";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerview);
        bottomNavigationView = getActivity().findViewById(R.id.navigation_bottom);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<>();
        adapter = new ProjectAdapter(list, getContext(), currentUserId);
        adapter.setOnLinkClickListener(this); // Set the link click listener
        recyclerView.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            // Restore the data list when the fragment's activity is recreated
            list = savedInstanceState.getParcelableArrayList(SAVED_DATA_LIST);
            if (list != null) {
                adapter.setData(list); // Use the setData() method to update the adapter
            }
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDataFromFirestore();
            swipeRefreshLayout.setRefreshing(false);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 20;  // Adjust this threshold as needed
            private int scrolledDistance = 0;
            private boolean controlsVisible = true;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check the scroll direction
                if (dy > 0) {
                    // Scrolling down
                    if (controlsVisible) {
                        scrolledDistance += dy;
                        if (scrolledDistance > HIDE_THRESHOLD) {
                            hideBottomNavigationView();
                            controlsVisible = false;
                            scrolledDistance = 0;
                        }
                    }
                } else {
                    // Scrolling up
                    if (!controlsVisible) {
                        showBottomNavigationView();
                        controlsVisible = true;
                        scrolledDistance = 0;
                    }
                }
            }
        });

        loadDataFromFirestore();

        return rootView;
    }

    private void hideBottomNavigationView() {
        bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(400).start();
    }

    private void showBottomNavigationView() {
        bottomNavigationView.animate().translationY(0).setDuration(400).start();
    }

    private void loadDataFromFirestore() {
        // Get the current user's ID from Firebase Auth or any other method
        currentUserId = "current_user_id";

        firestore.collection(COLLECTION_NAME).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                list.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Model model = document.toObject(Model.class);
                    String userID = model.getUserId();
                    firestore.collection("Users").document(userID).get().addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String username = userDoc.getString("username");
                            model.setUsername(username);
                        }
                        String imageDescription = document.getString("imageDescription");
                        model.setImageDescription(imageDescription);
                        list.add(model);
                        adapter.notifyDataSetChanged();
                    });
                }
            } else {
                // Handle error
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the data list of the adapter when the fragment is being saved
        if (adapter != null) {
            outState.putParcelableArrayList(SAVED_DATA_LIST, new ArrayList<>(list));
        }
    }

    @Override
    public void onLinkClick(String url) {
        openLinkInWebView(url);
    }

    private void openLinkInWebView(String url) {
        Intent intent = WebViewActivity.createIntent(getContext(), url);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(getContext(), "Internet permission granted. Please click again.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Internet permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
