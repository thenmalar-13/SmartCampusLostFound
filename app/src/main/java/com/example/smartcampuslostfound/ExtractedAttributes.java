package com.example.smartcampuslostfound;

import java.util.ArrayList;
import java.util.List;

public class ExtractedAttributes {
    private String category;
    private String brand;
    private String primaryColor;
    private List<String> distinctiveFeatures;

    // Required empty constructor for Firebase JSON deserialization
    public ExtractedAttributes() {
        this.category = "General";
        this.brand = "Unknown";
        this.primaryColor = "Unknown";
        this.distinctiveFeatures = new ArrayList<>();
    }

    // 4-argument constructor used by GeminiAttributeExtractor
    public ExtractedAttributes(String category, String brand, String primaryColor, List<String> distinctiveFeatures) {
        this.category = category != null ? category : "General";
        this.brand = brand != null ? brand : "Unknown";
        this.primaryColor = primaryColor != null ? primaryColor : "Unknown";
        this.distinctiveFeatures = distinctiveFeatures != null ? distinctiveFeatures : new ArrayList<>();
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

    public List<String> getDistinctiveFeatures() { return distinctiveFeatures; }
    public void setDistinctiveFeatures(List<String> distinctiveFeatures) { this.distinctiveFeatures = distinctiveFeatures; }
}