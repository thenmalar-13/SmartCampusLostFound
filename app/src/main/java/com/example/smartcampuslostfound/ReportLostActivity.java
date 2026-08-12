package com.example.smartcampuslostfound;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportLostActivity extends AppCompatActivity {

    private TextInputEditText etItemName, etLocation, etContactNumber, etDescription, etPrivateDetails;
    private CheckBox checkboxSensitive;
    private MaterialButton btnSubmitReport;
    private MaterialButtonToggleGroup toggleGroupStatus;
    private MaterialCardView cardUploadImage;
    private ImageView imgPreview;
    private View placeholderContainer;

    private Uri selectedImageUri = null;

    // Image Picker Launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imgPreview.setImageURI(selectedImageUri);
                        imgPreview.setVisibility(View.VISIBLE);
                        placeholderContainer.setVisibility(View.GONE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        // Initialize views
        etItemName = findViewById(R.id.etItemName);
        etLocation = findViewById(R.id.etLocation);
        etContactNumber = findViewById(R.id.etContactNumber);
        etDescription = findViewById(R.id.etDescription);
        etPrivateDetails = findViewById(R.id.etPrivateDetails);
        checkboxSensitive = findViewById(R.id.checkboxSensitive);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        toggleGroupStatus = findViewById(R.id.toggleGroupStatus);

        cardUploadImage = findViewById(R.id.cardUploadImage);
        imgPreview = findViewById(R.id.imgPreview);
        placeholderContainer = findViewById(R.id.placeholderContainer);

        // Initialize the Image Upload Card Click
        cardUploadImage.setOnClickListener(v -> {
            // Open Android Gallery
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSubmitReport.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String contact = etContactNumber.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String privateDetails = etPrivateDetails.getText().toString().trim();

            if (name.isEmpty() || location.isEmpty() || contact.isEmpty()) {
                Toast.makeText(ReportLostActivity.this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String status = "Lost";
            int checkedId = toggleGroupStatus.getCheckedButtonId();
            if (checkedId == R.id.btnSelectFound) {
                status = "Found";
            }

            ItemModel itemModel = new ItemModel();
            itemModel.setName(name);
            itemModel.setLocation(location);
            itemModel.setContactNumber(contact);
            itemModel.setDescription(description);
            itemModel.setPrivateDetails(privateDetails);
            itemModel.setStatus(status);

            // Sensitive item check
            boolean isSensitive = checkboxSensitive.isChecked();
            itemModel.setSensitive(isSensitive);

            // Safe Image to Base64 Conversion with Compression
            if (selectedImageUri != null) {
                try {
                    android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                    // Resize/compress to prevent huge string sizes that break Firebase
                    android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos);
                    byte[] imageBytes = baos.toByteArray();

                    String encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                    itemModel.setImageUrl(encodedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    itemModel.setImageUrl("");
                }
            }

            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://smartcampuslostfound-5aa0e-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Items");
            String itemId = databaseReference.push().getKey();

            if (itemId != null) {
                itemModel.setItemId(itemId);

                databaseReference.child(itemId).setValue(itemModel)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ReportLostActivity.this, "Item posted successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ReportLostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }
}