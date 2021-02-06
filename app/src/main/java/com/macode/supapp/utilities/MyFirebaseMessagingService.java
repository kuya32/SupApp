package com.macode.supapp.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.macode.supapp.ChatActivity;
import com.macode.supapp.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String title, body;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        title = remoteMessage.getNotification().getTitle();
        body = remoteMessage.getNotification().getBody();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "CHAT");
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(R.drawable.supapplogo);
        Intent intent = null;

        if (remoteMessage.getData().get("type").equalsIgnoreCase("message")) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("otherUserId", remoteMessage.getData().get("userId"));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(123, builder.build());
    }
}
