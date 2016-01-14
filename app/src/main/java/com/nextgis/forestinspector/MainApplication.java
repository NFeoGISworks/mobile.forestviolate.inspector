/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import com.nextgis.forestinspector.activity.PreferencesActivity;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.map.FILayerFactory;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.File;

import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.SettingsConstants.KEY_PREF_MAP;


public class MainApplication
        extends GISApplication
{
    protected DocumentsLayer      mDocsLayer;
    protected DocumentEditFeature mEditFeature;
    protected File                mDocFeatureFolder;

    protected boolean mIsNewTempFeature = false;


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

        return mMap;
    }


    public DocumentsLayer getDocsLayer()
    {
        if (null != mDocsLayer) {
            return mDocsLayer;
        }

        MapBase map = getMap();
        mDocsLayer = (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);

//        for (int i = 0; i < map.getLayerCount(); i++) {
//            ILayer layer = map.getLayer(i);
//            if (layer instanceof DocumentsLayer) {
//                mDocsLayer = (DocumentsLayer) layer;
//                break;
//            }
//        }

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
        Intent intentSet = new Intent(this, PreferencesActivity.class);
        intentSet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentSet);
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
        getDocsLayer().deleteAllTemps();

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

/*
    // for debug
    protected NetworkUtil mNet;


    @Override
    public void onCreate()
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        super.onCreate();

        mNet = new NetworkUtil(this);
    }


    public boolean isNetworkAvailable()
    {
        return mNet.isNetworkAvailable();
    }


    public Account getAccount()
    {
        return getAccount(getString(R.string.account_name));
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
*/
}
