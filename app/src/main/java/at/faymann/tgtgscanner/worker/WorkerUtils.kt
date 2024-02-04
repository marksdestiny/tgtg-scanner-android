/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.faymann.tgtgscanner.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.faymann.tgtgscanner.R

const val STOCK_NOTIFICATION_CHANNEL_NAME = "Stock Change Notifications"
const val STOCK_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever a stock rises from zero"
const val STOCK_NOTIFICATION_CHANNEL_ID = "STOCK_NOTIFICATION"
const val STOCK_NOTIFICATION_TITLE = "New bag available"
const val STOCK_NOTIFICATION_ID = 1

/**
 * Create a Notification that is shown as a heads-up notification if possible.
 *
 * @param message Message shown on the notification
 * @param context Context needed to create Toast
 */
fun makeStockNotification(message: String, context: Context) {

    // Make a channel if necessary
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    val name = STOCK_NOTIFICATION_CHANNEL_NAME
    val description = STOCK_NOTIFICATION_CHANNEL_DESCRIPTION
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(STOCK_NOTIFICATION_CHANNEL_ID, name, importance)
    channel.description = description

    // Add the channel
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

    notificationManager?.createNotificationChannel(channel)

    // Create the notification
    val builder = NotificationCompat.Builder(context, STOCK_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(STOCK_NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification if we have permissions
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    NotificationManagerCompat.from(context).notify(STOCK_NOTIFICATION_ID, builder.build())
}