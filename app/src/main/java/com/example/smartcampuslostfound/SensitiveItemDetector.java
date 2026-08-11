package com.example.smartcampuslostfound;

import java.util.Arrays;
import java.util.List;

public class SensitiveItemDetector {

    private static final List<String> SENSITIVE_KEYWORDS = Arrays.asList(
            "card", "debit", "credit", "id", "student id", "passport", "license",
            "driver", "certificate", "wallet", "phone", "smartphone", "iphone",
            "samsung", "laptop", "macbook", "electronics", "jewelry", "ring",
            "chain", "gold", "key", "keys", "atm", "pancard", "aadhar"
    );

    public static boolean isSensitiveItem(String name, String description) {
        String combined = ((name != null ? name : "") + " " + (description != null ? description : "")).toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (combined.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
