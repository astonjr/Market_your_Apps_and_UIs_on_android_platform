package com.example.iqreatealpha;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {
    private List<PdfItem> pdfItemList;
    private Context context;

    public PdfAdapter(List<PdfItem> pdfItemList) {
        this.pdfItemList = pdfItemList;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.report_item_view, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfItem pdfItem = pdfItemList.get(position);
        holder.reportNameTextView.setText(pdfItem.getReportName());

        // Click listener to open the PDF in a WebView activity
        holder.reportNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("pdfUrl", pdfItem.getPdfUrl());
                context.startActivity(intent);
            }
        });

        // Long click listener to copy the URL to the clipboard
        holder.reportNameTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Copy the PDF URL to the clipboard
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("PDF URL", pdfItem.getPdfUrl());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(v.getContext(), "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        // Click listener to open the link in an external browser
        holder.reportNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkInExternalBrowser(pdfItem.getPdfUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfItemList.size();
    }

    private void openLinkInExternalBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No external browser found to open the link.", Toast.LENGTH_SHORT).show();
        }
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView reportNameTextView;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            reportNameTextView = itemView.findViewById(R.id.report_name);
        }
    }
}
