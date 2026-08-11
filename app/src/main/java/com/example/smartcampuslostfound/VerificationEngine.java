package com.example.smartcampuslostfound;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class VerificationEngine {

    public interface VerificationCallback {
        void onResult(double score, String status, String reason);
    }

    public static void verifyClaimantAnswer(String claimantAnswer, String storedPrivateDetail, String apiKey, VerificationCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            double score = 0.0;
            String status = "Insufficient evidence";
            String reason = "Claimant details do not sufficiently match private records.";

            try {
                // Strict Smart Check: Count how many distinct significant words overlap
                String cleanClaimant = claimantAnswer.toLowerCase().trim();
                String cleanStored = storedPrivateDetail.toLowerCase().trim();

                int matchingWordsCount = 0;
                String[] claimantWords = cleanClaimant.split("\\s+");
                for (String word : claimantWords) {
                    if (word.length() > 3 && cleanStored.contains(word)) {
                        matchingWordsCount++;
                    }
                }

                // If multiple significant words match (e.g. "debit card"), give a strong pass immediately
                if (matchingWordsCount >= 2 || cleanClaimant.equals(cleanStored)) {
                    score = 0.85;
                    status = "Strong ownership evidence";
                    reason = "Multiple key terms matched successfully.";
                } else {
                    // Otherwise, rely on Gemini AI with clear contextual guidelines
                    JSONObject jsonPayload = new JSONObject();
                    String prompt = "You are a security verification assistant for a lost and found app.\n" +
                            "Stored Private Detail provided by finder: \"" + storedPrivateDetail + "\"\n" +
                            "Claimant Answer: \"" + claimantAnswer + "\"\n\n" +
                            "Task: Evaluate if the claimant's answer shows accurate knowledge of the private details. " +
                            "Do not pass random generic guesses. The answer must closely relate or share specific matching characteristics.\n" +
                            "Return ONLY valid JSON format:\n" +
                            "{\n" +
                            "  \"verificationScore\": <double between 0.0 and 1.0>,\n" +
                            "  \"status\": \"strong_evidence\" or \"insufficient_evidence\",\n" +
                            "  \"reason\": \"brief explanation\"\n" +
                            "}";

                    jsonPayload.put("contents", new JSONArray().put(
                            new JSONObject().put("parts", new JSONArray().put(
                                    new JSONObject().put("text", prompt)
                            ))
                    ));

                    URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
                    }

                    InputStream stream = conn.getInputStream();
                    Scanner scanner = new Scanner(stream).useDelimiter("\\A");
                    String responseText = scanner.hasNext() ? scanner.next() : "";

                    JSONObject responseJson = new JSONObject(responseText);
                    String text = responseJson.getJSONArray("candidates")
                            .getJSONObject(0).getJSONObject("content")
                            .getJSONArray("parts").getJSONObject(0).getString("text");

                    if (text.contains("```json")) {
                        text = text.substring(text.indexOf("```json") + 7);
                        if (text.contains("```")) text = text.substring(0, text.indexOf("```"));
                    }

                    JSONObject parsed = new JSONObject(text.trim());
                    score = parsed.optDouble("verificationScore", 0.0);
                    reason = parsed.optString("reason", "Verification complete.");

                    if (score >= 0.50) {
                        status = "Strong ownership evidence";
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                score = 0.0;
                status = "Insufficient evidence";
                reason = "Error processing verification.";
            }

            final double finalScore = score;
            final String finalStatus = status;
            final String finalReason = reason;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResult(finalScore, finalStatus, finalReason);
                }
            });
        });
    }
}