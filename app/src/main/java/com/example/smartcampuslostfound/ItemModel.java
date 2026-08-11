package com.example.smartcampuslostfound;

public class ItemModel {
    private String id;
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private String contactNumber;
    private String itemType; // "LOST" or "FOUND"
    private String userId;
    private ExtractedAttributes attributes;
    private String privateDetails; // Private field for Feature 5 & 6 (Hidden from public feed)
    private boolean isSensitive;

    public ItemModel() { }

    public ItemModel(String id, String name, String description, String location, String itemType, String contactNumber, ExtractedAttributes attributes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.itemType = itemType;
        this.contactNumber = contactNumber;
        this.attributes = attributes != null ? attributes : new ExtractedAttributes();
        this.imageUrl = "";
        this.userId = "anonymous_user";
        this.privateDetails = "";
        this.isSensitive = false;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getImageUrl() { return imageUrl; }
    public String getContactNumber() { return contactNumber; }
    public String getItemType() { return itemType; }
    public String getUserId() { return userId; }
    public ExtractedAttributes getAttributes() { return attributes; }
    public String getPrivateDetails() { return privateDetails; }
    public boolean isSensitive() { return isSensitive; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAttributes(ExtractedAttributes attributes) { this.attributes = attributes; }
    public void setPrivateDetails(String privateDetails) { this.privateDetails = privateDetails; }
    public void setSensitive(boolean sensitive) { isSensitive = sensitive; }
}