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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.mapui.LayerFactoryUI;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.ConstantsUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.File;

import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.Constants.NGW_ACCOUNT_TYPE;
import static com.nextgis.maplib.util.GeoConstants.TMSTYPE_OSM;
import static com.nextgis.maplib.util.SettingsConstants.KEY_PREF_MAP;

public class MainApplication extends GISApplication {

    @Override
    public MapBase getMap() {
        if (null != mMap) {
            return mMap;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(getFilesDir(), KEY_PREF_MAP);
        }

        String mapPath = sharedPreferences.getString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName = sharedPreferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

        File mapFullPath = new File(mapPath, mapName + MAP_EXT);

        final Bitmap bkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        mMap = new MapDrawable(bkBitmap, this, mapFullPath, new LayerFactoryUI());
        mMap.setName(mapName);
        mMap.load();

        return mMap;
    }

    /**
     * @return A authority for sync purposes or empty string if not sync anything
     */
    @Override
    public String getAuthority() {
        return com.nextgis.forestinspector.util.SettingsConstants.AUTHORITY;
    }

    /**
     * Show settings Activity
     */
    @Override
    public void showSettings() {

    }

    @Override
    protected void onFirstRun()
    {
        //add OpenStreetMap layer on application first run
        String layerName = getString(R.string.osm);
        String layerURL = getString(R.string.osm_url);
        RemoteTMSLayerUI osmLayer =
                new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
        osmLayer.setName(layerName);
        osmLayer.setURL(layerURL);
        osmLayer.setTMSType(TMSTYPE_OSM);
        osmLayer.setMaxZoom(22);
        osmLayer.setMinZoom(12.4f);
        osmLayer.setVisible(true);

        mMap.addLayer(osmLayer);
        //mMap.moveLayer(0, osmLayer);

        String kosmosnimkiLayerName = getString(R.string.topo);
        String kosmosnimkiLayerURL = getString(R.string.topo_url);
        RemoteTMSLayerUI ksLayer =
                new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
        ksLayer.setName(kosmosnimkiLayerName);
        ksLayer.setURL(kosmosnimkiLayerURL);
        ksLayer.setTMSType(TMSTYPE_OSM);
        ksLayer.setMaxZoom(12.4f);
        ksLayer.setMinZoom(0);
        ksLayer.setVisible(true);

        mMap.addLayer(ksLayer);
        //mMap.moveLayer(1, ksLayer);

        String mixerLayerName = getString(R.string.geomixer_fv_tiles);
        String mixerLayerURL = getString(R.string.geomixer_fv_tiles_url);
        RemoteTMSLayerUI mixerLayer =
                new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
        mixerLayer.setName(mixerLayerName);
        mixerLayer.setURL(mixerLayerURL);
        mixerLayer.setTMSType(TMSTYPE_OSM);
        mixerLayer.setMaxZoom(25);
        mixerLayer.setMinZoom(0);
        mixerLayer.setVisible(true);

        mMap.addLayer(mixerLayer);
        //mMap.moveLayer(2, mixerLayer);

        // TODO: change to get layer ID from special key
        // http://176.9.38.120/fv/resource/34
        Account account = LayerFactory.getAccountByName(getApplicationContext(), "176.9.38.120/fv");
        final AccountManager am = AccountManager.get(getApplicationContext());
        if (null == account) {
            //create account
            Bundle userData = new Bundle();
            userData.putString("url", "http://176.9.38.120/fv");
            userData.putString("login", "administrator");
            account = new Account("176.9.38.120/fv", NGW_ACCOUNT_TYPE);
            if (!am.addAccountExplicitly(account, "admin", userData)) {
                return;
            }
        }


        NGWVectorLayerUI ngwVectorLayer =
                new NGWVectorLayerUI(getApplicationContext(), mMap.createLayerStorage());
        ngwVectorLayer.setName("GeoMixer violations vector");
        ngwVectorLayer.setRemoteId(34);
        ngwVectorLayer.setVisible(true);
        ngwVectorLayer.setAccountName("176.9.38.120/fv");
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);

        mMap.addLayer(ngwVectorLayer);

        ngwVectorLayer.downloadAsync();

        mMap.save();
    }
}

