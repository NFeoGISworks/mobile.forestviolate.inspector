/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2015. NextGIS, info@nextgis.com
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

package com.nextgis.forestinspector;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManagerFuture;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.nextgis.forestinspector.activity.FIPreferencesActivity;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.fragment.NGWSettingsFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.map.FILayerFactory;
import com.nextgis.forestinspector.service.InitService;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.File;

import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.SettingsConstants.KEY_PREF_MAP;


public class MainApplication
        extends GISApplication
        implements NGWLoginFragment.OnAddAccountListener,
                   NGWSettingsFragment.OnDeleteAccountListener

{
    protected DocumentsLayer      mDocsLayer;
    protected DocumentEditFeature mEditFeature;
    protected File                mDocFeatureFolder;

    protected boolean mIsNewTempFeature = false;

    protected NetworkUtil mNet;

    protected OnAccountAddedListener   mOnAccountAddedListener;
    protected OnAccountDeletedListener mOnAccountDeletedListener;
    protected OnReloadMapListener      mOnReloadMapListener;

    protected boolean mIsAccountCreated = false;
    protected boolean mIsAccountDeleted = false;
    protected boolean mIsMapReloaded    = false;


    @Override
    public void onCreate()
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        super.onCreate();

        mNet = new NetworkUtil(this);

        BroadcastReceiver initSyncStatusReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(
                    Context context,
                    Intent intent)
            {
                if (isRanAsService()) {
                    return;
                }

                int state = intent.getIntExtra(Constants.KEY_STATE, Constants.STEP_STATE_WAIT);

                switch (state) {

                    case Constants.STEP_STATE_ERROR:
                    case Constants.STEP_STATE_CANCEL: {

                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Account account = getAccount();

                        if (null != account) {
                            ContentResolver.removePeriodicSync(
                                    account, getAuthority(), Bundle.EMPTY);
                            ContentResolver.setSyncAutomatically(account, getAuthority(), false);
                            ContentResolver.cancelSync(account, getAuthority());

                            AccountManagerFuture<Boolean> future = removeAccount(account);

                            while (!future.isDone()) {
                                // wait until the removing is complete
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        //delete map
                        MapBase map = getMap();
                        map.delete();
                        FileUtil.deleteRecursive(map.getPath());
                        mMap = null;

                        reloadMap();
                        break;
                    }

                    case Constants.STEP_STATE_FINISH:
                        reloadMap();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_MESSAGE);
        registerReceiver(initSyncStatusReceiver, intentFilter);
    }


    @Override
    public MapBase getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(getFilesDir(), KEY_PREF_MAP);
        }

        String mapPath = sharedPreferences.getString(
                SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName =
                sharedPreferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

        File mapFullPath = new File(mapPath, mapName + MAP_EXT);
        mDocFeatureFolder = new File(mapPath, Constants.TEMP_DOCUMENT_FEATURE_FOLDER);

        final Bitmap bkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        mMap = new MapDrawable(bkBitmap, this, mapFullPath, new FILayerFactory());
        mMap.setName(mapName);
        mMap.load();
        mMap.setMaxZoom(19);

        return mMap;
    }


    public DocumentsLayer getDocsLayer()
    {
        if (null != mDocsLayer) {
            return mDocsLayer;
        }

        MapBase map = getMap();
        mDocsLayer = (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);

        return mDocsLayer;
    }


    /**
     * @return A authority for sync purposes or empty string if not sync anything
     */
    @Override
    public String getAuthority()
    {
        return com.nextgis.forestinspector.util.SettingsConstants.AUTHORITY;
    }


    /**
     * Show settings Activity
     */
    @Override
    public void showSettings(String settings)
    {
        Intent intentSet = new Intent(this, FIPreferencesActivity.class);
        intentSet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentSet);
    }

    @Override
    public void sendEvent(
            String category,
            String action,
            String label)
    {

    }

    @Override
    public void sendScreen(String name)
    {

    }

    @Override
    public String getAccountsType()
    {
        return com.nextgis.maplib.util.Constants.NGW_ACCOUNT_TYPE;
    }


    @Override
    protected int getThemeId(boolean isDark)
    {
        if (isDark) {
            return R.style.AppTheme_Dark;
        } else {
            return R.style.AppTheme_Light;
        }
    }


    public File getDocFeatureFolder()
    {
        if (!mDocFeatureFolder.exists()) {
            FileUtil.createDir(mDocFeatureFolder);
        }
        return mDocFeatureFolder;
    }


    public void clearTempDocumentFeatureFolder()
    {
        if (FileUtil.renameAndDelete(mDocFeatureFolder)) {
            FileUtil.createDir(mDocFeatureFolder);
        }
    }


    public void clearAllTemps()
    {
        DocumentsLayer documentsLayer = getDocsLayer();

        if (null != documentsLayer) {
            documentsLayer.deleteAllTemps();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(com.nextgis.forestinspector.util.SettingsConstants.KEY_PREF_TEMP_FEATURE_ID);
        edit.commit();

        clearTempDocumentFeatureFolder();

        mEditFeature = null;
    }


    public DocumentEditFeature getEditFeature(Long featureId)
    {
        mIsNewTempFeature = false;

        if (null != mEditFeature) {
            return mEditFeature;
        }

        DocumentsLayer docsLayer = getDocsLayer();
        DocumentFeature feature;

        if (null != featureId) {
            feature = docsLayer.getFeatureWithAttaches(featureId);

        } else {
            clearAllTemps();
            mIsNewTempFeature = true;
            feature = getTempFeature();
        }

        if (null == feature || !docsLayer.hasFeatureTempFlag(feature.getId())
                && !docsLayer.hasFeatureNotSyncFlag(feature.getId())) {

            mIsNewTempFeature = false;
            mEditFeature = null;
            return null;
        }

        mEditFeature = new DocumentEditFeature(feature);
        return mEditFeature;
    }


    public boolean isNewTempFeature()
    {
        return mIsNewTempFeature;
    }


    protected DocumentFeature getTempFeature()
    {
        DocumentsLayer docsLayer = getDocsLayer();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        DocumentFeature feature = null;

        long featureId = prefs.getLong(
                com.nextgis.forestinspector.util.SettingsConstants.KEY_PREF_TEMP_FEATURE_ID,
                com.nextgis.maplib.util.Constants.NOT_FOUND);

        if (com.nextgis.maplib.util.Constants.NOT_FOUND != featureId) {
            feature = docsLayer.getFeatureWithAttaches(featureId);

            if (null == feature) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.remove(
                        com.nextgis.forestinspector.util.SettingsConstants.KEY_PREF_TEMP_FEATURE_ID);
                edit.commit();

                featureId = (long) com.nextgis.maplib.util.Constants.NOT_FOUND;
            }
        }

        if (com.nextgis.maplib.util.Constants.NOT_FOUND == featureId) {
            feature = docsLayer.getNewTempFeature();

            if (null == feature) {
                return null;
            }

            SharedPreferences.Editor edit = prefs.edit();
            edit.putLong(
                    com.nextgis.forestinspector.util.SettingsConstants.KEY_PREF_TEMP_FEATURE_ID,
                    feature.getId());
            edit.commit();
        }

        if (!docsLayer.hasFeatureTempFlag(feature.getId())) {
            return null;
        } else {
            return feature;
        }
    }


    public boolean isRanAsService()
    {
        return getCurrentProcessName().matches(".*:(init)?sync$");
    }


    public String getCurrentProcessName()
    {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }


    public boolean isInitServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {

            if (InitService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }


    public boolean isNetworkAvailable()
    {
        return mNet.isNetworkAvailable();
    }


    public Account getAccount()
    {
        return getAccount(getString(R.string.account_name));
    }

    @Override
    public boolean addAccount(
            String name,
            String url,
            String login,
            String password,
            String token)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return super.addAccount(name, url, login, password, token);
    }

    public boolean runSync()
    {
        if (!isNetworkAvailable()) {
            return false;
        }

        Account account = getAccount();

        if (null == account) {
            return false;
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(account, getAuthority(), settingsBundle);
        return true;
    }


    @Override
    public void onAddAccount(
            Account account,
            String token,
            boolean accountAdded)
    {
        if (accountAdded) {
            mIsAccountCreated = true;

            //free any map data here
            MapBase map = getMap();

            // delete all layers from map if any
            map.delete();

            if (null != account) {
                //set sync with server
                ContentResolver.setSyncAutomatically(account, getAuthority(), true);
                ContentResolver.addPeriodicSync(
                        account, getAuthority(), Bundle.EMPTY,
                        com.nextgis.maplib.util.Constants.DEFAULT_SYNC_PERIOD);
            }

            // goto step 2
            if (null != mOnAccountAddedListener) {
                mIsAccountCreated = false;
                mOnAccountAddedListener.onAccountAdded();
            }

        } else {
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDeleteAccount(Account account)
    {
        mIsAccountDeleted = true;
        MapBase map = getMap();
        map.load(); // reload map without listener

        if (null != mOnAccountDeletedListener) {
            mIsAccountDeleted = false;
            mOnAccountDeletedListener.onAccountDeleted();
        }
    }


    public void reloadMap()
    {
        MapBase map = getMap();
        map.load();

        mIsMapReloaded = true;

        if (null != mOnReloadMapListener) {
            mIsMapReloaded = false;
            mOnReloadMapListener.onReloadMap();
        }
    }


    public boolean isAccountAdded()
    {
        boolean isCreated = mIsAccountCreated;
        mIsAccountCreated = false;
        return isCreated;
    }


    public boolean isAccountDeleted()
    {
        boolean isDeleted = mIsAccountDeleted;
        mIsAccountDeleted = false;
        return isDeleted;
    }


    public boolean isMapReloaded()
    {
        boolean isReloaded = mIsMapReloaded;
        mIsMapReloaded = false;
        return isReloaded;
    }


    public void setOnAccountAddedListener(OnAccountAddedListener onAccountAddedListener)
    {
        mOnAccountAddedListener = onAccountAddedListener;
    }


    public interface OnAccountAddedListener
    {
        void onAccountAdded();
    }


    public void setOnAccountDeletedListener(OnAccountDeletedListener onAccountDeletedListener)
    {
        mOnAccountDeletedListener = onAccountDeletedListener;
    }


    public interface OnAccountDeletedListener
    {
        void onAccountDeleted();
    }


    public void setOnReloadMapListener(OnReloadMapListener onReloadMapListener)
    {
        mOnReloadMapListener = onReloadMapListener;
    }


    public interface OnReloadMapListener
    {
        void onReloadMap();
    }
}
