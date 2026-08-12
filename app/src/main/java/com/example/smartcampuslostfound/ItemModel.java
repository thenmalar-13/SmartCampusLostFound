package com.example.smartcampuslostfound;

public class ItemModel {
    private String itemId;
    private String name;
    private String location;
    private String contactNumber;
    private String description;
    private String privateDetails;
    private String status;
    private boolean isSensitive;
    private ExtractedAttributes attributes;
    private String itemType;
    private String imageUrl; // Added for image data support

    // Required empty constructor for Firebase
    public ItemModel() {
    }

    // Getters
    public String getItemId() { return itemId; }
    public String getId() { return itemId; } // Alias to prevent any getId() errors
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getContactNumber() { return contactNumber; }
    public String getDescription() { return description; }
    public String getPrivateDetails() { return privateDetails; }
    public String getStatus() { return status; }
    public boolean isSensitive() { return isSensitive; }
    public ExtractedAttributes getAttributes() { return attributes; }
    public String getItemType() { return itemType; }
    public String getImageUrl() { return imageUrl; } // Added getter

    // Setters
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setDescription(String description) { this.description = description; }
    public void setPrivateDetails(String privateDetails) { this.privateDetails = privateDetails; }
    public void setStatus(String status) { this.status = status; }
    public void setSensitive(boolean sensitive) { isSensitive = sensitive; }
    public void setAttributes(ExtractedAttributes attributes) { this.attributes = attributes; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } // Added setter
}