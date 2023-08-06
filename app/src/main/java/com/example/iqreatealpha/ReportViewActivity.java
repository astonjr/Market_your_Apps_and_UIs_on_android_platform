package com.example.iqreatealpha;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PdfAdapter pdfAdapter;
    private List<PdfItem> pdfItemList;

    private FirebaseFirestore firestore;
    private CollectionReference systemReportsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportview);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pdfItemList = new ArrayList<>();
        pdfAdapter = new PdfAdapter(pdfItemList);
        recyclerView.setAdapter(pdfAdapter);

        // Initialize Firebase Firestore and create a reference to the collection
        firestore = FirebaseFirestore.getInstance();
        systemReportsRef = firestore.collection("SystemReports");

        // Fetch the data from Firestore and update the pdfItemList
        fetchReportsFromFirestore();

        pdfAdapter.notifyDataSetChanged();
    }

    private void fetchReportsFromFirestore() {
        // Fetch the reports from Firestore and update the pdfItemList
        systemReportsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            PdfItem pdfItem = documentChange.getDocument().toObject(PdfItem.class);
                            pdfItemList.add(pdfItem);
                        }
                    }
                    pdfAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(ReportViewActivity.this, "Failed to fetch reports from Firestore", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
