package com.example.smartcampuslostfound;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReportLostActivity extends AppCompatActivity {

    private TextInputEditText etItemName, etLocation, etContactNumber, etDescription, etPrivateDetails;
    private MaterialButtonToggleGroup toggleGroupStatus;
    private MaterialButton btnSubmitReport;
    private MaterialCardView cardUploadImage;
    private ImageView imgPreview;
    private LinearLayout placeholderContainer, containerSensitiveAlert;

    private DatabaseReference dbRef;
    private String itemType = "LOST"; // Default
    private String encodedImageBase64 = ""; // Holds base64 encoded photo

    // Gallery Picker Launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        imgPreview.setImageURI(imageUri);
                        imgPreview.setVisibility(View.VISIBLE);
                        placeholderContainer.setVisibility(View.GONE);

                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        // Compress bitmap to fit inside Firebase Realtime Database limits
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] bytes = baos.toByteArray();
                        encodedImageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        // Target central "Items" reference
        dbRef = FirebaseDatabase.getInstance("https://smartcampuslostfound-5aa0e-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Items");

        // Map XML Layout elements
        etItemName = findViewById(R.id.etItemName);
        etLocation = findViewById(R.id.etLocation);
        etContactNumber = findViewById(R.id.etContactNumber);
        etDescription = findViewById(R.id.etDescription);
        etPrivateDetails = findViewById(R.id.etPrivateDetails);
        containerSensitiveAlert = findViewById(R.id.containerSensitiveAlert);
        toggleGroupStatus = findViewById(R.id.toggleGroupStatus);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        cardUploadImage = findViewById(R.id.cardUploadImage);
        imgPreview = findViewById(R.id.imgPreview);
        placeholderContainer = findViewById(R.id.placeholderContainer);

        // Feature 6: TextWatcher for real-time sensitive item detection
        TextWatcher sensitiveWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = etItemName != null && etItemName.getText() != null ? etItemName.getText().toString() : "";
                String desc = etDescription != null && etDescription.getText() != null ? etDescription.getText().toString() : "";
                boolean sensitive = SensitiveItemDetector.isSensitiveItem(name, desc);
                if (containerSensitiveAlert != null) {
                    containerSensitiveAlert.setVisibility(sensitive ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        if (etItemName != null) etItemName.addTextChangedListener(sensitiveWatcher);
        if (etDescription != null) etDescription.addTextChangedListener(sensitiveWatcher);

        // Upload photo card listener
        if (cardUploadImage != null) {
            cardUploadImage.setOnClickListener(v -> openGallery());
        }

        // Toggle button listener (Lost vs Found)
        if (toggleGroupStatus != null) {
            toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnSelectFound) {
                        itemType = "FOUND";
                    } else {
                        itemType = "LOST";
                    }
                }
            });
        }

        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> submitReport());
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void submitReport() {
        String name = etItemName != null ? etItemName.getText().toString().trim() : "";
        String location = etLocation != null ? etLocation.getText().toString().trim() : "";
        String contact = etContactNumber != null ? etContactNumber.getText().toString().trim() : "";
        String description = etDescription != null ? etDescription.getText().toString().trim() : "";
        String privateDetails = etPrivateDetails != null && etPrivateDetails.getText() != null
                ? etPrivateDetails.getText().toString().trim() : "";

        if (name.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = dbRef.push().getKey();
        if (itemId == null) return;

        // 1. Extract key attributes via Gemini AI
        GeminiAttributeExtractor.extractAttributes(name, description, attributes -> {

            // 2. Build ItemModel matching existing constructor
            ItemModel newItem = new ItemModel(
                    itemId,
                    name,
                    description,
                    location,
                    itemType,
                    contact,
                    attributes
            );

            // Assign image, user ID, private details, and sensitivity flag
            newItem.setImageUrl(encodedImageBase64);
            newItem.setUserId("anonymous_user");
            newItem.setPrivateDetails(privateDetails);
            newItem.setSensitive(SensitiveItemDetector.isSensitiveItem(name, description));

            // 3. Save Item to Firebase Realtime Database
            dbRef.child(itemId).setValue(newItem).addOnSuccessListener(aVoid -> {
                Toast.makeText(ReportLostActivity.this, "Report submitted successfully!", Toast.LENGTH_SHORT).show();

                // 4. Trigger Cross-Matching Engine Immediately
                runMatchingProcess(newItem);

            }).addOnFailureListener(e -> {
                Toast.makeText(ReportLostActivity.this, "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void runMatchingProcess(ItemModel newItem) {
        dbRef.get().addOnSuccessListener(dataSnapshot -> {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                ItemModel existingItem = snapshot.getValue(ItemModel.class);

                if (existingItem != null && existingItem.getId() != null && !existingItem.getId().equals(newItem.getId())) {
                    if (existingItem.getItemType() != null && newItem.getItemType() != null &&
                            !existingItem.getItemType().equalsIgnoreCase(newItem.getItemType())) {

                        String apiKey = "AQ.Ab8RN6LVoplAGj1LaAf7JuaXpEMW085uMbwaYtYGOM_widd1Jw"; // Your API Key

                        GeminiMatchEngine.evaluateMatch(newItem, existingItem, apiKey, matchResult -> {
                            if (matchResult != null && matchResult.getMatchPercentage() >= 80) {

                                String matchId = newItem.getId() + "_" + existingItem.getId();
                                DatabaseReference matchRef = FirebaseDatabase.getInstance("https://smartcampuslostfound-5aa0e-default-rtdb.asia-southeast1.firebasedatabase.app")
                                        .getReference("Matches")
                                        .child(matchId);

                                matchRef.setValue(matchResult);

                                // FEATURE 7: Automatic Potential-Match Notification (With Deduplication Check)
                                DatabaseReference sentNotifRef = FirebaseDatabase.getInstance("https://smartcampuslostfound-5aa0e-default-rtdb.asia-southeast1.firebasedatabase.app")
                                        .getReference("SentNotifications")
                                        .child(matchId);

                                sentNotifRef.get().addOnSuccessListener(notifSnapshot -> {
                                    if (!notifSnapshot.exists()) {
                                        // Mark as notified to prevent duplicate notifications
                                        sentNotifRef.setValue(true);

                                        // Trigger System Notification without sensitive data
                                        MatchNotificationHelper.showMatchNotification(
                                                ReportLostActivity.this,
                                                matchResult.getMatchPercentage()
                                        );
                                    }
                                });

                                if (!isFinishing() && !isDestroyed()) {
                                    MatchDialog.showMatchDetails(
                                            ReportLostActivity.this,
                                            matchResult,
                                            existingItem.getContactNumber(),
                                            existingItem.getPrivateDetails()
                                    );
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}