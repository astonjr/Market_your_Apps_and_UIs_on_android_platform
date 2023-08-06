package com.example.iqreatealpha;

public class PdfItem {
    private String reportName;
    private String dateGenerated;
    private String userId;
    private String pdfUrl;

    public PdfItem() {
        // Required empty constructor for Firestore mapping
    }

    public PdfItem(String reportName, String dateGenerated, String userId, String downloadUrl) {
        this.reportName = reportName;
        this.dateGenerated = dateGenerated;
        this.userId = userId;
        this.pdfUrl = pdfUrl;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(String dateGenerated) {
        this.dateGenerated = dateGenerated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
