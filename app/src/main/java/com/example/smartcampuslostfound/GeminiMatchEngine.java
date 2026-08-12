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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class GeminiMatchEngine {

    public interface MatchCallback {
        void onMatchEvaluated(MatchResult matchResult);
    }

    public static void evaluateMatch(ItemModel lostItem, ItemModel foundItem, String apiKey, MatchCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            MatchResult matchResult;
            try {
                JSONObject jsonPayload = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject partsObject = new JSONObject();

                String prompt = "Compare these two lost & found items strictly.\n" +
                        "Item 1: " + (lostItem != null ? lostItem.getName() + " - " + lostItem.getDescription() : "") + "\n" +
                        "Item 2: " + (foundItem != null ? foundItem.getName() + " - " + foundItem.getDescription() : "") + "\n\n" +
                        "CRITICAL INSTRUCTIONS:\n" +
                        "- If these items are completely different objects (e.g. box vs phone, white vs black), matchScore MUST BE 0.\n" +
                        "- If they describe the exact same item, matchScore should be high (80-100).\n" +
                        "Return ONLY valid JSON format:\n" +
                        "{\n" +
                        "  \"matchScore\": integer,\n" +
                        "  \"reasons\": [\"reason 1\", \"reason 2\"]\n" +
                        "}";

                partsObject.put("text", prompt);
                contentsArray.put(new JSONObject().put("parts", new JSONArray().put(partsObject)));
                jsonPayload.put("contents", contentsArray);

                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                InputStream stream = conn.getInputStream();
                Scanner scanner = new Scanner(stream).useDelimiter("\\A");
                String responseText = scanner.hasNext() ? scanner.next() : "";

                JSONObject responseJson = new JSONObject(responseText);
                String textResponse = responseJson.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                if (textResponse.contains("```json")) {
                    textResponse = textResponse.substring(textResponse.indexOf("```json") + 7);
                    if (textResponse.contains("```")) {
                        textResponse = textResponse.substring(0, textResponse.indexOf("```"));
                    }
                } else if (textResponse.contains("```")) {
                    textResponse = textResponse.substring(textResponse.indexOf("```") + 3);
                    if (textResponse.contains("```")) {
                        textResponse = textResponse.substring(0, textResponse.indexOf("```"));
                    }
                }

                JSONObject parsedData = new JSONObject(textResponse.trim());
                double score = parsedData.optDouble("matchScore", 0.0);
                int matchPercentage = (int) Math.round(score);

                List<String> reasonList = new ArrayList<>();
                JSONArray reasonsArray = parsedData.optJSONArray("reasons");
                if (reasonsArray != null) {
                    for (int i = 0; i < reasonsArray.length(); i++) {
                        reasonList.add(reasonsArray.optString(i));
                    }
                } else {
                    reasonList.add(parsedData.optString("reason", "Item similarity detected"));
                }

                matchResult = new MatchResult(
                        lostItem != null ? lostItem.getItemId() : "",
                        foundItem != null ? foundItem.getItemId(): "",
                        matchPercentage,
                        reasonList
                );

            } catch (Exception e) {
                e.printStackTrace();
                // Return 0% match on error so false dialogs NEVER pop up
                matchResult = new MatchResult(
                        lostItem != null ? lostItem.getItemId() : "",
                        foundItem != null ? foundItem.getItemId() : "",
                        0,
                        new ArrayList<>()
                );
            }

            MatchResult finalResult = matchResult;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onMatchEvaluated(finalResult);
                }
            });
        });
    }
}