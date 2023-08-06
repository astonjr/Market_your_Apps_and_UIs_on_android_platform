package com.example.iqreatealpha;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<Model> searchResults;
    private Context context;
    private OnLinkClickListener onLinkClickListener; // Listener for link clicks

    public SearchAdapter(Context context) {
        this.context = context;
        this.searchResults = new ArrayList<>();
    }

    // Setter method for the link click listener
    public void setOnLinkClickListener(OnLinkClickListener listener) {
        this.onLinkClickListener = listener;
    }

    public void setSearchResults(List<Model> searchResults) {
        this.searchResults.clear();
        if (searchResults != null) {
            this.searchResults.addAll(searchResults);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = searchResults.get(position);
        holder.setImageUrl(model.getImageUrl());
        holder.setAppLink(model.getLink());
        holder.setAppOwner(model.getUsername());
        holder.setImageDescription(model.getImageDescription());

        holder.appLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLinkClickListener != null) {
                    onLinkClickListener.onLinkClick(model.getLink());
                }
            }
        });

        holder.appLinkTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(model.getLink());
                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        holder.appOwnerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile(model.getUserId());
            }
        });

        // Add hover tooltip
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TooltipCompat.setTooltipText(holder.appLinkTextView, "Hold to copy link");
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Link", text);
        clipboard.setPrimaryClip(clip);
    }

    private void openUserProfile(String userId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("userId", userId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView appLinkTextView;
        private TextView appOwnerTextView;
        private TextView projectDescriptionTextView;
        private String appLink;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.project_image);
            appLinkTextView = itemView.findViewById(R.id.project_redirect_link);
            appOwnerTextView = itemView.findViewById(R.id.project_username);
            projectDescriptionTextView = itemView.findViewById(R.id.project_description);
        }

        public void setImageUrl(String imageUrl) {
            // Use Picasso or any other image loading library to load the image into the image view
            Picasso.get().load(imageUrl).into(imageView);
        }

        public void setAppLink(String appLink) {
            this.appLink = appLink;
        }

        public void setAppOwner(String appOwner) {
            appOwnerTextView.setText(appOwner);
        }

        public void setImageDescription(String imageDescription) {
            projectDescriptionTextView.setText(imageDescription);
        }
    }

    // Interface for handling link clicks
    public interface OnLinkClickListener {
        void onLinkClick(String url);
    }
}
