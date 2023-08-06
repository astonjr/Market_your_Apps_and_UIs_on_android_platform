package com.example.iqreatealpha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SearchAdapter.OnLinkClickListener {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private SearchAdapter adapter;
    private List<Model> searchResults;
    private FirebaseFirestore firestore;
    private static final String SAVED_SEARCH_QUERY = "saved_search_query";
    private static final String SAVED_SEARCH_RESULTS = "saved_search_results";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        searchResults = new ArrayList<>();
        adapter = new SearchAdapter(requireContext());
        adapter.setOnLinkClickListener(this);

        // Check if there's a saved instance state (i.e., configuration change)
        if (savedInstanceState != null) {
            // Restore the search query and results
            String savedQuery = savedInstanceState.getString(SAVED_SEARCH_QUERY);
            ArrayList<Model> savedResults = savedInstanceState.getParcelableArrayList(SAVED_SEARCH_RESULTS);

            if (savedQuery != null && savedResults != null) {
                searchView.setQuery(savedQuery, false); // Set the query text without triggering a search
                searchResults.addAll(savedResults);
                adapter.setSearchResults(searchResults);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current search query and results
        String currentQuery = searchView.getQuery().toString();
        ArrayList<Model> currentResults = new ArrayList<>(searchResults);

        outState.putString(SAVED_SEARCH_QUERY, currentQuery);
        outState.putParcelableArrayList(SAVED_SEARCH_RESULTS, currentResults);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = rootView.findViewById(R.id.search_bar);
        recyclerView = rootView.findViewById(R.id.recyclerview);
        bottomNavigationView = getActivity().findViewById(R.id.navigation_bottom);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
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

        return rootView;
    }

    private void hideBottomNavigationView() {
        bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(400).start();
    }

    private void showBottomNavigationView() {
        bottomNavigationView.animate().translationY(0).setDuration(400).start();
    }

    private void performSearch(String query) {
        searchResults.clear();
        if (!TextUtils.isEmpty(query)) {
            firestore.collection("Users")
                    .whereGreaterThanOrEqualTo("username", query)
                    .whereLessThanOrEqualTo("username", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener(userQuerySnapshot -> {
                        List<DocumentSnapshot> userDocuments = userQuerySnapshot.getDocuments();
                        List<String> userIds = new ArrayList<>();
                        for (DocumentSnapshot userDocument : userDocuments) {
                            userIds.add(userDocument.getId());
                        }
                        queryImagesByUserIds(userIds);
                        loadUsernames(userIds);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to perform search.", Toast.LENGTH_SHORT).show();
                    });

            firestore.collection("Images")
                    .whereGreaterThanOrEqualTo("imageDescription", query)
                    .whereLessThanOrEqualTo("imageDescription", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener(imageQuerySnapshot -> {
                        List<Model> results = imageQuerySnapshot.toObjects(Model.class);
                        searchResults.addAll(results);
                        adapter.setSearchResults(searchResults);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to perform search.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            adapter.setSearchResults(null);
        }
    }

    private void loadUsernames(List<String> userIds) {
        if (!userIds.isEmpty()) {
            firestore.collection("Users")
                    .whereIn("userId", userIds)
                    .get()
                    .addOnSuccessListener(userSnapshot -> {
                        for (Model model : searchResults) {
                            String userId = model.getUserId();
                            for (DocumentSnapshot userDocument : userSnapshot.getDocuments()) {
                                if (userId.equals(userDocument.getId())) {
                                    String username = userDocument.getString("username");
                                    model.setUsername(username);
                                    break;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load usernames.", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void queryImagesByUserIds(List<String> userIds) {
        if (!userIds.isEmpty()) {
            firestore.collection("Images")
                    .whereIn("userId", userIds)
                    .get()
                    .addOnSuccessListener(imageQuerySnapshot -> {
                        List<Model> results = imageQuerySnapshot.toObjects(Model.class);
                        searchResults.addAll(results);
                        adapter.setSearchResults(searchResults);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to perform search.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onLinkClick(String url) {
        Intent intent = WebViewActivity.createIntent(requireContext(), url);
        startActivity(intent);
    }
}
