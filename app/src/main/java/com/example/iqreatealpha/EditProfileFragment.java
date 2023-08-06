package com.example.iqreatealpha;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 200;

    private ImageView profilePictureImageView;
    private EditText editUsernameEditText;
    private EditText editUserProfessionEditText;
    private Button editProfileButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Find the "cancel_profile_edit" button and set a click listener
        view.findViewById(R.id.cancel_profile_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the settings page
                Fragment fragment = new SettingsFragment();
                if (fragment != null) {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        profilePictureImageView = view.findViewById(R.id.edit_profile_picture);
        editUsernameEditText = view.findViewById(R.id.edit_username);
        editUserProfessionEditText = view.findViewById(R.id.edit_userprofession);
        editProfileButton = view.findViewById(R.id.edit_profile_button);

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profilePictureImageView.setImageURI(selectedImageUri);
        }
    }

    private void updateProfile() {
        final String userId = mAuth.getCurrentUser().getUid();
        final String username = editUsernameEditText.getText().toString().trim();
        final String userProfession = editUserProfessionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(userProfession) && selectedImageUri == null) {
            Toast.makeText(getActivity(), "No changes made", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        // Update username if not empty
        if (!TextUtils.isEmpty(username)) {
            updates.put("username", username);
        }

        // Update userProfession if not empty
        if (!TextUtils.isEmpty(userProfession)) {
            updates.put("userProfession", userProfession);
        }

        // Update profile picture if selected
        if (selectedImageUri != null) {
            final StorageReference imageRef = storageRef.child("Profile images").child(userId + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String profilePictureUrl = uri.toString();
                                        updates.put("profilePictureUrl", profilePictureUrl);
                                        updateFirestore(userId, updates);
                                        clearFields();
                                    }
                                });
                            } else {
                                Toast.makeText(getActivity(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            updateFirestore(userId, updates);
            clearFields();
        }
    }

    private void updateFirestore(String userId, Map<String, Object> updates) {
        db.collection("Users").document(userId)
                .set(updates, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void clearFields() {
        editUsernameEditText.setText("");
        editUserProfessionEditText.setText("");
        profilePictureImageView.setImageResource(R.drawable.ic_add_profile_pic);
        selectedImageUri = null;
    }
}
