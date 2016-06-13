/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2016. NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.adapter;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.MainActivity;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.util.Constants;


public class FISyncAdapter
        extends SyncAdapter
{
    public static final int IS_OK       = 0;
    public static final int IS_STARTED  = 1;
    public static final int IS_FINISHED = 2;
    public static final int IS_CANCELED = 3;
    public static final int IS_ERROR    = 4;

    private static final int NOTIFY_ID = 1;


    public FISyncAdapter(
            Context context,
            boolean autoInitialize)
    {
        super(context, autoInitialize);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FISyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    public void onPerformSync(
            Account account,
            Bundle bundle,
            String authority,
            ContentProviderClient contentProviderClient,
            SyncResult syncResult)
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        int syncStatus = FISyncAdapter.IS_OK;

        MainApplication app = (MainApplication) getContext().getApplicationContext();

        sendNotification(app, IS_STARTED, null);

        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);

        if (isCanceled()) {
            syncStatus = FISyncAdapter.IS_CANCELED;
            sendNotification(
                    app, FISyncAdapter.IS_CANCELED, app.getString(R.string.sync_canceled));
            Log.d(Constants.TAG, "FISyncAdapter - notification IS_CANCELED is sent");
        }

        if (syncResult.hasError()) {
            syncStatus = FISyncAdapter.IS_ERROR;
            sendNotification(app, FISyncAdapter.IS_ERROR, syncResult.toString());
        }

        if (FISyncAdapter.IS_OK == syncStatus) {
            sendNotification(app, FISyncAdapter.IS_FINISHED, null);
        }
    }


    public static void sendNotification(
            Context context,
            int notificationType,
            String errorMsg)
    {
        final SharedPreferences prefs = context.getSharedPreferences(Constants.PREFERENCES,
                Constants.MODE_MULTI_PROCESS);
        boolean isShow = prefs.getBoolean(
                com.nextgis.forestinspector.util.SettingsConstants.KEY_PREF_SHOW_SYNC_NOTIFICATION,
                true);

        if (!isShow) {
            return;
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setLargeIcon(
                BitmapFactory.decodeResource(
                        context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(false);

        switch (notificationType) {
            case FISyncAdapter.IS_STARTED:
                builder.setProgress(0, 0, true)
                        .setSmallIcon(R.drawable.ic_sync_started)
                        .setTicker(context.getString(R.string.sync_started))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_progress));
                break;

            case FISyncAdapter.IS_FINISHED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_finished)
                        .setTicker(context.getString(R.string.sync_finished))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_finished));
                break;

            case FISyncAdapter.IS_CANCELED:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(context.getString(R.string.sync_canceled))
                        .setContentTitle(context.getString(R.string.synchronization))
                        .setContentText(context.getString(R.string.sync_canceled));
                break;

            case FISyncAdapter.IS_ERROR:
                builder.setProgress(0, 0, false)
                        .setSmallIcon(R.drawable.ic_sync_error)
                        .setTicker(context.getString(R.string.sync_error))
                        .setContentTitle(context.getString(R.string.sync_error))
                        .setContentText(errorMsg);
                break;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}
