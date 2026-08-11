package com.example.smartcampuslostfound;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class MatchNotificationHelper {

    private static final String CHANNEL_ID = "potential_match_channel";
    private static final String CHANNEL_NAME = "Potential Item Matches";

    public static void showMatchNotification(Context context, int matchScore) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications when a potential lost or found item match is detected");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Carefully worded according to requirements (No sensitive info, no legal declarations)
        String title = "🔔 Potential Match Found";
        String contentText = "Your lost item may match a recently found item. Match confidence: " + matchScore + "%";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            // Unique ID per match trigger using timestamp
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
