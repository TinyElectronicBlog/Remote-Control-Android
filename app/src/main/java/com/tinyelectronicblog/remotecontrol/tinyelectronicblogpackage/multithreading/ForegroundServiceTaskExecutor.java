package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading;

/**
 * MIT License
 * Copyright (c) 2022 TinyElectronicBlog
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import java.util.ArrayList;

public class ForegroundServiceTaskExecutor extends Service {

    private final static String notificationChannelID = "TEBtask7514"; //The id of the channel. Must be unique per package. The value may be truncated if it is too long.
    private final static String notificationChannelNAME = "TEB Remote Control"; //The user visible name of the channel. The recommended maximum length is 40 characters; the value may be truncated if it is too long.
    private static final ArrayList<ForegroundServiceTask> toExecute = new ArrayList<>();

    static void addTask (ForegroundServiceTask task) {
        toExecute.add(task);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        while (true) {
            try {
                ForegroundServiceTask task = toExecute.remove(0);
                Notification notification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notification = new Notification.Builder(task.fromContext, notificationChannelID).build();
                } else {
                    notification = new NotificationCompat.Builder(task.fromContext, notificationChannelID).build();
                }
                startForeground(1, notification);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        task.run();
                    }
                };
                t.start();
                task.fromContext = null;
            }
            catch(IndexOutOfBoundsException e) {break;}
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static boolean notificationChannelCreated = false;
    /**
     * Creates a notification channel that notifications can be posted to. It is mandatory from API level 26 (Android Oreo) or higher to display notifications.
     * @param fromContext Context executing this function.
     * @return Returns true on success.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static boolean createNotificationChannel (Context fromContext) {
        if (notificationChannelCreated) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager manager = fromContext.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(new NotificationChannel(notificationChannelID, notificationChannelNAME, NotificationManager.IMPORTANCE_DEFAULT)); //The importance of the channel. This controls how interruptive notifications posted to this channel are. Value is NotificationManager.IMPORTANCE_UNSPECIFIED, NotificationManager.IMPORTANCE_NONE, NotificationManager.IMPORTANCE_MIN, NotificationManager.IMPORTANCE_LOW, NotificationManager.IMPORTANCE_DEFAULT, or NotificationManager.IMPORTANCE_HIGH.
                notificationChannelCreated = true;
                return true;
            }
            catch (Exception e) {
                Debug.print(e.toString());
            }
        }
        return false;
    }

    /**
     * Deletes the channel created with createNotificationChannel().
     * param fromContext Context executing this function.
     * return Returns true on success.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static boolean deleteNotificationChannel (Context fromContext) {
        if (!notificationChannelCreated) return true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && notificationChannelCreated) {
            try {
                NotificationManager manager = fromContext.getSystemService(NotificationManager.class);
                manager.deleteNotificationChannel(notificationChannelID);
                notificationChannelCreated = false;
                return true;
            }
            catch (Exception e) {
                Debug.print(e.toString());
            }
        }
        return false;
    }

}