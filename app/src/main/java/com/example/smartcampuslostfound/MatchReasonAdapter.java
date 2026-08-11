package com.example.smartcampuslostfound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MatchReasonAdapter extends RecyclerView.Adapter<MatchReasonAdapter.ViewHolder> {

    private final List<String> matchReasons;

    public MatchReasonAdapter(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_reason, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String reason = matchReasons.get(position);
        holder.txtReason.setText(reason);
    }

    @Override
    public int getItemCount() {
        return matchReasons != null ? matchReasons.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReason = itemView.findViewById(R.id.txtReason);
        }
    }
}