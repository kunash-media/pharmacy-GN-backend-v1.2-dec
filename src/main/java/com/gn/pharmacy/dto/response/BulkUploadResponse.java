package com.gn.pharmacy.dto.response;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadResponse {

    private int uploadedCount;
    private int skippedCount;
    private List<String> skippedReasons = new ArrayList<>();

    private String message;

    // Getters and Setters
    public int getUploadedCount() {
        return uploadedCount;
    }

    public void setUploadedCount(int uploadedCount) {
        this.uploadedCount = uploadedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<String> getSkippedReasons() {
        return skippedReasons;
    }

    public void setSkippedReasons(List<String> skippedReasons) {
        this.skippedReasons = skippedReasons;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}