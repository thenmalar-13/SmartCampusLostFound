package com.example.smartcampuslostfound;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {
    private String itemId1;
    private String itemId2;
    private int matchPercentage;
    private List<String> matchReasons;

    // Required empty constructor for Firebase
    public MatchResult() {
        this.matchReasons = new ArrayList<>();
    }

    public MatchResult(String itemId1, String itemId2, int matchPercentage, List<String> matchReasons) {
        this.itemId1 = itemId1;
        this.itemId2 = itemId2;
        this.matchPercentage = matchPercentage;
        this.matchReasons = matchReasons != null ? matchReasons : new ArrayList<>();
    }

    // Getters
    public String getItemId1() { return itemId1; }
    public String getItemId2() { return itemId2; }
    public int getMatchPercentage() { return matchPercentage; }
    public List<String> getMatchReasons() { return matchReasons; }

    // Setters
    public void setItemId1(String itemId1) { this.itemId1 = itemId1; }
    public void setItemId2(String itemId2) { this.itemId2 = itemId2; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }
    public void setMatchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; }
}