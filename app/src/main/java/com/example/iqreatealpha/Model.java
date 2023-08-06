package com.example.iqreatealpha;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Model implements Parcelable{
    private String imageId;
    private String imageUrl;
    private String userId;
    private String username;
    private String fileSizeInKbs;
    private String imageDescription;
    private String link;
    private String dateOfUpload;
    private String fileType;
    private List<Integer> userRatings;
    private float averageRating;
    private int numRatings;
    private int totalRatings;



    public Model() {
        // Default constructor required for Firebase database operations
    }

    protected Model(Parcel in) {
        imageUrl = in.readString();
        userId = in.readString();
        username = in.readString();
        fileSizeInKbs = in.readString();
        imageDescription = in.readString();
        link = in.readString();
        dateOfUpload = in.readString();
        fileType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(fileSizeInKbs);
        dest.writeString(imageDescription);
        dest.writeString(link);
        dest.writeString(dateOfUpload);
        dest.writeString(fileType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };


    public Model(String imageUrl, String userId, String username, String fileSize, String imageDescription, String link, String uploadTime, String fileType) {
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.username = username;
        this.fileSizeInKbs = fileSize;
        this.imageDescription = imageDescription;
        this.link = link;
        this.dateOfUpload = uploadTime;
        this.fileType = fileType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getfileSizeInKbs() {
        return fileSizeInKbs;
    }

    public void setfileSizeInKbs(String fileSizeInKbs) {
        this.fileSizeInKbs = fileSizeInKbs;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDateOfUpload() {
        return dateOfUpload;
    }

    public void setDateOfUpload(String dateOfUpload) {
        this.dateOfUpload = dateOfUpload;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<Integer> getUserRatings() {
        return userRatings;
    }

    public void setUserRatings(List<Integer> userRatings) {
        this.userRatings = userRatings;
    }
    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
}
