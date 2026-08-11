package com.example.smartcampuslostfound;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MatchDialog {

    private static final String GEMINI_API_KEY = "AQ.Ab8RN6LVoplAGj1LaAf7JuaXpEMW085uMbwaYtYGOM_widd1Jw";

    public static void showMatchDetails(Context context, MatchResult matchResult, String contactNumber) {
        showMatchDetails(context, matchResult, contactNumber, null);
    }

    public static void showMatchDetails(Context context, MatchResult matchResult, String contactNumber, String targetItemPrivateDetails) {
        if (context == null || matchResult == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_match_details, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtMatchTitle = dialogView.findViewById(R.id.txtMatchTitle);
        RecyclerView rvMatchReasons = dialogView.findViewById(R.id.rvMatchReasons);
        MaterialButton btnContactOwner = dialogView.findViewById(R.id.btnContactOwner);
        MaterialButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        txtMatchTitle.setText("🎯 " + matchResult.getMatchPercentage() + "% Potential Match");

        List<String> reasons = matchResult.getMatchReasons();
        if (reasons == null || reasons.isEmpty()) {
            reasons = new ArrayList<>();
            reasons.add("High feature similarity detected between listings");
        }

        rvMatchReasons.setLayoutManager(new LinearLayoutManager(context));
        MatchReasonAdapter adapter = new MatchReasonAdapter(reasons);
        rvMatchReasons.setAdapter(adapter);

        btnContactOwner.setOnClickListener(v -> {
            dialog.dismiss();
            promptOwnershipVerification(context, contactNumber, targetItemPrivateDetails);
        });

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void promptOwnershipVerification(Context context, String contactNumber, String storedPrivateDetail) {
        // STRICT GUARD: If item was posted without private details, reject direct contact reveal
        if (storedPrivateDetail == null || storedPrivateDetail.trim().isEmpty()) {
            showInconclusiveWarning(context, "The finder did not record any private verification details for this item. Contact info cannot be revealed automatically.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("🔒 Prove Item Ownership");
        builder.setMessage("The finder specified private identifying details for this item. Answer the prompt to verify ownership:");

        final EditText etAnswer = new EditText(context);
        etAnswer.setHint("e.g. Serial number, specific sticker, ID initials, wallpaper...");
        builder.setView(etAnswer);

        builder.setPositiveButton("Verify & Claim", (dialog, which) -> {
            String claimantAnswer = etAnswer.getText().toString().trim();

            if (TextUtils.isEmpty(claimantAnswer)) {
                Toast.makeText(context, "Please enter details to verify ownership.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify with Gemini Verification Engine (score >= 0.40 unlocks contact)
            VerificationEngine.verifyClaimantAnswer(claimantAnswer, storedPrivateDetail, GEMINI_API_KEY, new VerificationEngine.VerificationCallback() {
                @Override
                public void onResult(double score, String status, String reasoning) {
                    if (score >= 0.40) {
                        Toast.makeText(context, "✅ Ownership Verified! Opening contact...", Toast.LENGTH_LONG).show();
                        openDialer(context, contactNumber);
                    } else {
                        showInconclusiveWarning(context, "Your answer did not sufficiently match the private verification details (" + reasoning + ").");
                    }
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private static void showInconclusiveWarning(Context context, String reasonMessage) {
        new AlertDialog.Builder(context)
                .setTitle("⚠️ Verification Inconclusive")
                .setMessage(reasonMessage + "\n\nTo prevent fraudulent claims, contact information remains hidden. Please verify ownership directly in person.")
                .setPositiveButton("Understand", null)
                .show();
    }

    private static void openDialer(Context context, String contactNumber) {
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactNumber));
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No contact number provided.", Toast.LENGTH_SHORT).show();
        }
    }
}