package com.example.iqreatealpha;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private EditText projectDescriptionEditText, projectLinkEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    // Firebase
    private StorageReference storageReference;
    private CollectionReference imagesCollection;
    private Uri imageUri;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Widgets
        Button uploadBtn = findViewById(R.id.upload_button);
        Button backtoHomepage = findViewById(R.id.back_home);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);
        projectDescriptionEditText = findViewById(R.id.project_description);
        projectLinkEditText = findViewById(R.id.project_link);

        progressBar.setVisibility(View.INVISIBLE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("User projects");
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            imagesCollection = firestore.collection("Images");

            backtoHomepage.setOnClickListener(v -> {
                startActivity(new Intent(UploadActivity.this, DashboardActivity.class));
                finish();
            });

            imageView.setOnClickListener(v -> {
                if (checkStoragePermission()) {
                    openGallery();
                } else {
                    requestStoragePermission();
                }
            });

            uploadBtn.setOnClickListener(v -> {
                if (imageUri != null) {
                    uploadToFirebase(imageUri);
                } else {
                    Toast.makeText(UploadActivity.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not authenticated, handle the authentication process
            Toast.makeText(UploadActivity.this, "Please login first to start uploading", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UploadActivity.this, HomeFragment.class));
            finish();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadToFirebase(Uri uri) {
        final StorageReference fileRef = storageReference.child(generateFileName(generateRandomImageId()) + "." + getFileExtension(uri));
        UploadTask uploadTask = fileRef.putFile(uri);

        progressBar.setVisibility(View.VISIBLE); // Start the progress bar

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String imageId = generateRandomImageId();
                String imageUrl = task.getResult().toString();
                String imageDescription = projectDescriptionEditText.getText().toString();
                String link = projectLinkEditText.getText().toString();
                String fileType = getFileExtension(uri);
                String fileSizeInKbs = String.valueOf(getCurrentFileSize(uri)); // File size in kilobytes
                String DateOfUpload = getCurrentDate();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                double averageRating = 0.0;
                int totalRatings = 0;
                int numRatings = 0;

                if (isValidUrl(link)) {
                    // Create a new image document and add it to Firestore
                    DocumentReference imageDocumentRef = imagesCollection.document(imageId);
                    Map<String, Object> image = new HashMap<>();
                    image.put("imageId", imageId);
                    image.put("imageUrl", imageUrl);
                    image.put("imageDescription", imageDescription);
                    image.put("link", link);
                    image.put("fileType", fileType);
                    image.put("fileSizeInKbs", fileSizeInKbs);
                    image.put("dateOfUpload", DateOfUpload);
                    image.put("userId", userId);
                    image.put("totalRatings", totalRatings); // Set initial rating to 0
                    image.put("numRatings", numRatings); // Set initial userRatings to 0
                    image.put("averageRating", averageRating); // Set initial average rating to 0

                    ((DocumentReference) imageDocumentRef).set(image)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UploadActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                clearFields();
                                progressBar.setVisibility(View.INVISIBLE); // Stop the progress bar
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.INVISIBLE); // Stop the progress bar
                                Toast.makeText(UploadActivity.this, "Uploading Failed!", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(UploadActivity.this, "Unsecure or Invalid URL", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE); // Stop the progress bar
                }
            } else {
                progressBar.setVisibility(View.INVISIBLE); // Stop the progress bar
                Toast.makeText(UploadActivity.this, "Uploading Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRandomImageId() {
        return imagesCollection.document().getId();
    }

    private long getCurrentFileSize(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                return inputStream.available() / 1024; // Convert to kilobytes
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean isValidUrl(String url) {
        return url != null && url.startsWith("https://");
    }

    private void clearFields() {
        projectDescriptionEditText.setText("");
        projectLinkEditText.setText("");
        imageView.setImageResource(R.drawable.add_photo_upload);
    }

    private String generateFileName(String imageId) {
        return imageId;
    }


    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}