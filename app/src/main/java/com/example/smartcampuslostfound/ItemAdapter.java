package com.example.smartcampuslostfound;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItemModel> originalList;
    private ArrayList<ItemModel> filteredList;

    public ItemAdapter(Context context, ArrayList<ItemModel> list) {
        this.context = context;
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void updateList(ArrayList<ItemModel> newList) {
        this.originalList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String query = text.toLowerCase().trim();
            for (ItemModel item : originalList) {
                if ((item.getName() != null && item.getName().toLowerCase().contains(query)) ||
                        (item.getLocation() != null && item.getLocation().toLowerCase().contains(query))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel model = filteredList.get(position);

        holder.txtTitle.setText(model.getName() != null ? model.getName() : "No Title");
        holder.txtDesc.setText(model.getDescription() != null ? model.getDescription() : "");
        holder.txtLoc.setText(model.getLocation() != null ? "Location: " + model.getLocation() : "");

        // Bind AI Extracted Attributes
        if (model.getAttributes() != null) {
            holder.txtAiExtracted.setVisibility(View.VISIBLE);
            String attrSummary = "✨ Category: " + model.getAttributes().getCategory() +
                    " | Brand: " + model.getAttributes().getBrand() +
                    " | Color: " + model.getAttributes().getPrimaryColor();
            holder.txtAiExtracted.setText(attrSummary);
        } else {
            holder.txtAiExtracted.setVisibility(View.GONE);
        }

        // Determine status safely from model.getStatus()
        String itemStatus = model.getStatus() != null ? model.getStatus() : model.getItemType();

        // Set status badge visual styling
        if ("FOUND".equalsIgnoreCase(itemStatus)) {
            holder.txtStatusChip.setText("FOUND");
            holder.txtStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_found));
        } else {
            holder.txtStatusChip.setText("LOST");
            holder.txtStatusChip.setBackgroundColor(ContextCompat.getColor(context, R.color.status_lost));
        }

        // Image decoding handling for Base64 strings
        String imgData = model.getImageUrl();
        if (imgData != null && !imgData.trim().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imgData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedByte != null) {
                    holder.itemImg.setImageBitmap(decodedByte);
                    holder.itemImg.setVisibility(View.VISIBLE);
                } else {
                    holder.itemImg.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.itemImg.setVisibility(View.GONE);
            }
        } else {
            holder.itemImg.setVisibility(View.GONE);
        }

        // SECURE CONTACT ACTION: Only triggers verification if it's a FOUND item AND explicitly marked as sensitive
        holder.btnContact.setOnClickListener(v -> {
            String contactNum = model.getContactNumber();
            if (contactNum == null || contactNum.trim().isEmpty()) {
                Toast.makeText(context, "No contact number provided", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isFound = "FOUND".equalsIgnoreCase(itemStatus);
            boolean isSensitive = model.isSensitive();

            // Trigger verification dialog ONLY if it's both Found and Sensitive
            if (isFound && isSensitive) {
                MatchResult dummyResult = new MatchResult();
                dummyResult.setMatchPercentage(100);
                ArrayList<String> reasons = new ArrayList<>();
                reasons.add("Secure Verification Required for Sensitive Found Item");
                dummyResult.setMatchReasons(reasons);

                MatchDialog.showMatchDetails(
                        context,
                        dummyResult,
                        contactNum,
                        model.getPrivateDetails()
                );
            } else {
                // Otherwise (Lost items or normal non-sensitive notes/found items), dial directly
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactNum));
                context.startActivity(intent);
            }
        });

        // Delete / Mark as Resolved Action
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Mark as Resolved")
                    .setMessage("Remove this item listing permanently?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (model.getItemId() != null && !model.getItemId().isEmpty()) {
                            DatabaseReference itemRef = FirebaseDatabase.getInstance("https://smartcampuslostfound-5aa0e-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("Items")
                                    .child(model.getItemId());

                            itemRef.removeValue()
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(context, "Item deleted!", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );
                        } else {
                            Toast.makeText(context, "Error: Item ID is missing.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDesc, txtLoc, txtStatusChip, txtAiExtracted;
        ImageView itemImg;
        MaterialButton btnContact, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtLoc = itemView.findViewById(R.id.txtLoc);
            txtStatusChip = itemView.findViewById(R.id.txtStatusChip);
            txtAiExtracted = itemView.findViewById(R.id.txtAiExtracted);
            itemImg = itemView.findViewById(R.id.itemImg);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}