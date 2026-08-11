package com.example.smartcampuslostfound;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class GeminiAttributeExtractor {

    private static final String API_KEY = "AQ.Ab8RN6LVoplAGj1LaAf7JuaXpEMW085uMbwaYtYGOM_widd1Jw"; // Paste your API key inside double quotes
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface ExtractorCallback {
        void onExtracted(ExtractedAttributes attributes);
    }

    public static void extractAttributes(String title, String description, ExtractorCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExtractedAttributes resultAttrs;

            try {
                URL url = new URL(ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                String prompt = "Extract key attributes from this lost/found report text.\n" +
                        "Title: " + title + "\n" +
                        "Description: " + description + "\n\n" +
                        "Tolerate typos (e.g., 'balck' = 'Black', 'samsunge' = 'Samsung').\n" +
                        "Return ONLY valid JSON with this schema:\n" +
                        "{\n" +
                        "  \"category\": \"Electronics / Clothing / Accessories / Keys / Bags / General\",\n" +
                        "  \"brand\": \"Brand name or Unknown\",\n" +
                        "  \"primaryColor\": \"Primary color or Unknown\",\n" +
                        "  \"distinctiveFeatures\": [\"feature1\", \"feature2\"]\n" +
                        "}";

                JSONObject jsonPayload = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentObj = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partObj = new JSONObject();

                partObj.put("text", prompt);
                partsArray.put(partObj);
                contentObj.put("parts", partsArray);
                contentsArray.put(contentObj);
                jsonPayload.put("contents", contentsArray);

                JSONObject config = new JSONObject();
                config.put("response_mime_type", "application/json");
                jsonPayload.put("generationConfig", config);

                OutputStream os = conn.getOutputStream();
                os.write(jsonPayload.toString().getBytes("UTF-8"));
                os.close();

                if (conn.getResponseCode() == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                    String responseStr = scanner.hasNext() ? scanner.next() : "";

                    JSONObject responseJson = new JSONObject(responseStr);
                    String textResponse = responseJson.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    JSONObject parsedData = new JSONObject(textResponse);
                    String category = parsedData.optString("category", "General");
                    String brand = parsedData.optString("brand", "Unknown");
                    String color = parsedData.optString("primaryColor", "Unknown");

                    List<String> features = new ArrayList<>();
                    JSONArray featuresArray = parsedData.optJSONArray("distinctiveFeatures");
                    if (featuresArray != null) {
                        for (int i = 0; i < featuresArray.length(); i++) {
                            features.add(featuresArray.getString(i));
                        }
                    }

                    resultAttrs = new ExtractedAttributes(category, brand, color, features);
                } else {
                    resultAttrs = fallbackExtraction(title, description);
                }

            } catch (Exception e) {
                resultAttrs = fallbackExtraction(title, description);
            }

            ExtractedAttributes finalResult = resultAttrs;
            new Handler(Looper.getMainLooper()).post(() -> callback.onExtracted(finalResult));
        });
    }

    private static ExtractedAttributes fallbackExtraction(String title, String description) {
        String combined = (title + " " + description).toLowerCase();

        // Updated Color Check
        String color = "Unknown";
        if (combined.contains("black") || combined.contains("balck")) color = "Black";
        else if (combined.contains("white")) color = "White";
        else if (combined.contains("blue")) color = "Blue";
        else if (combined.contains("red")) color = "Red";
        else if (combined.contains("yellow")) color = "Yellow";
        else if (combined.contains("green")) color = "Green";

        // Updated Brand Check
        String brand = "Unknown";
        if (combined.contains("samsung") || combined.contains("samsunge")) brand = "Samsung";
        else if (combined.contains("apple") || combined.contains("airpods")) brand = "Apple";
        else if (combined.contains("jbl")) brand = "JBL";
        else if (combined.contains("jupiter")) brand = "TVS / Jupiter";
        else if (combined.contains("honda")) brand = "Honda";

        // Updated Category Check
        String category = "General";
        if (combined.contains("phone") || combined.contains("airpods") || combined.contains("earbuds")) {
            category = "Electronics";
        } else if (combined.contains("key") || combined.contains("scooty")) {
            category = "Keys";
        }

        return new ExtractedAttributes(category, brand, color, new ArrayList<>());
    }
}