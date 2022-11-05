package com.example.sharencare

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.text.Html
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService : FirebaseMessagingService() {
    var mNotificationManager: NotificationManager? = null
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


// playing audio and vibration when user se reques
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(
            applicationContext, notification
        )
        r.play()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.isLooping = false
        }

        // vibration
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 300, 300, 300)
        v.vibrate(pattern, -1)
        val resourceImage = resources.getIdentifier(
            remoteMessage.notification!!.icon, "drawable", packageName
        )
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            builder.setSmallIcon(R.drawable.icontrans);
            builder.setSmallIcon(resourceImage)
        } else {
//            builder.setSmallIcon(R.drawable.icon_kritikar);
            builder.setSmallIcon(resourceImage)
        }
        val resultIntent = Intent(this, LoginActivity::class.java)
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val title_text = Html.fromHtml("<b>"+ remoteMessage.notification!!.title +"</b >")
        builder.setContentTitle(title_text.toString())
        builder.setContentText(remoteMessage.notification!!.body)
        builder.setContentIntent(pendingIntent)
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                remoteMessage.notification!!.body
            )
        )
        builder.setAutoCancel(true)
        builder.priority = Notification.PRIORITY_MAX
        mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager!!.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }


// notificationId is a unique int for each notification that you must define
        mNotificationManager!!.notify(100, builder.build())
    }
}