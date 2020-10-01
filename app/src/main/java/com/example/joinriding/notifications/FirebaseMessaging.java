package com.example.joinriding.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.joinriding.MyListUsersActivity;
import com.example.joinriding.PostDetailActivity;
import com.example.joinriding.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID = "admin_channel" ;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // get current user from shared preferences
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        // two notification chat and post
        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("PostNotification")){
            // post notif
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            // if user is same posted don't show notif
            if (!sender.equals(savedCurrentUser)){
                showPostNotif(""+pId, ""+pTitle, ""+pDescription);
            }

        }else if(notificationType.equals("ChatNotification")){
            // chat notif
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

            if (fuser != null && sent.equals(fuser.getUid())){
                if (!savedCurrentUser.equals(user)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        sendAboveNotification(remoteMessage);
                    }else{
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }

    }

    private void showPostNotif(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notifID = new Random().nextInt(3000);

        // post notif version above android
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.O){
            setupPostNotifChannel(notificationManager);
        }

        // show post detail activity using postId when notif is clicked
        Intent myIntent = new Intent(this, PostDetailActivity.class);
        myIntent.putExtra("postId", pId);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, PendingIntent.FLAG_ONE_SHOT);

        // largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_join_riding);

        // sound for notif
        Uri notifSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notifBuider = new NotificationCompat.Builder(this, ""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_join_riding)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notifSoundUri)
                .setContentIntent(pendingIntent);
        // show notification post
        notificationManager.notify(notifID, notifBuider.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotifChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device To Post Notif";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MyListUsersActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pIntent);

        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int j = 0;
        if (i > 0){
            j = i;
        }
        noti.notify(j, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MyListUsersActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        VersionNotification notification1 = new VersionNotification(this);
        Notification.Builder builder = notification1.getOnNotifications(title, body, pIntent, defaultSound, icon);

        int j = 0;
        if (i > 0){
            j = i;
        }
        notification1.getNotificationManager().notify(j, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        // update user token
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fuser != null){
            // signed in
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("TBL_TOKENS");
        Token token = new Token(tokenRefresh);
        reference.child(fuser.getUid()).setValue(token);
    }
}