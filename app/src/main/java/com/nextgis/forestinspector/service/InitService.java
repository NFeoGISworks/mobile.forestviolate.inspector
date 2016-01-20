/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
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

package com.nextgis.forestinspector.service;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.IProgressor;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.datasource.TileItem;
import com.nextgis.maplib.datasource.ngw.Connection;
import com.nextgis.maplib.datasource.ngw.INGWResource;
import com.nextgis.maplib.datasource.ngw.Resource;
import com.nextgis.maplib.datasource.ngw.ResourceGroup;
import com.nextgis.maplib.display.SimpleFeatureRenderer;
import com.nextgis.maplib.display.SimpleTiledPolygonStyle;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.MapUtil;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplib.util.NGWUtil;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Application initialisation service
 */
public class InitService extends Service {
    public static final String ACTION_START  = "START_INITIAL_SYNC";
    public static final String ACTION_STOP   = "STOP_INITIAL_SYNC";
    public static final String ACTION_REPORT = "REPORT_INITIAL_SYNC";

    public static final int MAX_SYNC_STEP = 9;

    private InitialSyncThread mThread;

    private volatile boolean mIsRunning;


    @Override
    public void onCreate()
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        super.onCreate();
        mIsRunning = false;
    }


    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId)
    {
        if (intent == null) return START_NOT_STICKY;

        switch (intent.getAction()) {
            case ACTION_START:
                if (mIsRunning) {
                    Log.d(Constants.FITAG, "reportSync()");
                    reportSync();
                } else {
                    Log.d(Constants.FITAG, "startSync()");
                    startSync();
                }
                break;
            case ACTION_STOP:
                Log.d(Constants.FITAG, "stopSync()");
                stopSync();
                break;
            case ACTION_REPORT:
                if (mIsRunning) {
                    Log.d(Constants.FITAG, "reportSync()");
                    reportSync();
                } else {
                    Log.d(Constants.FITAG, "reportFinish()");
                    reportFinish();
                }
                break;
        }

        return START_STICKY;
    }


    private void reportSync()
    {
        if (null != mThread) {
            mThread.publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);
        }
    }


    private void reportFinish()
    {
        Intent intent = new Intent(Constants.BROADCAST_MESSAGE);

        intent.putExtra(Constants.KEY_STEP, MAX_SYNC_STEP);
        intent.putExtra(Constants.KEY_MESSAGE, getString(R.string.done));
        intent.putExtra(Constants.KEY_STATE, Constants.STEP_STATE_FINISH);

        sendBroadcast(intent);
        stopSync();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    private void startSync()
    {
        final MainApplication app = (MainApplication) getApplication();
        if (app == null) {
            Log.d(Constants.FITAG, "failed to get main application");
        }

        final Account account = app.getAccount(getString(R.string.account_name));
        if (account == null) {
            Log.d(
                    Constants.FITAG,
                    "No account" + getString(R.string.account_name) + " created. Run first step.");
        }

        mThread = new InitialSyncThread(account);
        mIsRunning = true;
        mThread.start();
    }


    private void stopSync()
    {
        mIsRunning = false;

        if (mThread != null && mThread.isAlive()) { mThread.interrupt(); }

        stopSelf();
    }


    @Override
    public void onDestroy()
    {
        mIsRunning = false;
        super.onDestroy();
    }


    private class InitialSyncThread
            extends Thread
            implements IProgressor
    {
        protected Account mAccount;
        protected int     mMaxProgress;
        protected String  mProgressMessage;
        protected int     mStep;
        protected Intent  mMessageIntent;


        public InitialSyncThread(Account account)
        {
            mAccount = account;
            mMaxProgress = 0;
        }


        @Override
        public void setMax(int maxValue)
        {
            mMaxProgress = maxValue;
        }


        @Override
        public boolean isCanceled()
        {
            return !mIsRunning;
        }


        @Override
        public void setValue(int value)
        {
            String message = mProgressMessage + " (" + value + " " + getString(R.string.of) + " "
                    + mMaxProgress + ")";
            publishProgress(message, Constants.STEP_STATE_WORK);
        }


        @Override
        public void setIndeterminate(boolean indeterminate)
        {

        }


        @Override
        public void setMessage(String message)
        {
            mProgressMessage = message;
        }


        @Override
        public void run()
        {
            doWork();
            InitService.this.stopSync();
        }


        public final void publishProgress(
                String message,
                int state)
        {
            if (null == mMessageIntent) {
                return;
            }

            mMessageIntent.putExtra(Constants.KEY_STEP, mStep);
            mMessageIntent.putExtra(Constants.KEY_MESSAGE, message);
            mMessageIntent.putExtra(Constants.KEY_STATE, state);
            sendBroadcast(mMessageIntent);
        }

        protected Boolean doWork() {
            mMessageIntent = new Intent(Constants.BROADCAST_MESSAGE);

            // step 1: connect to server
            mStep = 0;
            int nTimeout = 4000;
            final MainApplication app = (MainApplication) getApplication();
            final String sLogin = app.getAccountLogin(mAccount);
            final String sPassword = app.getAccountPassword(mAccount);
            final String sURL = app.getAccountUrl(mAccount);

            if (null == sURL || null == sPassword || null == sLogin) {
                return false;
            }

            Connection connection = new Connection("tmp", sLogin, sPassword, sURL);
            publishProgress(getString(R.string.connecting), Constants.STEP_STATE_WORK);

            if(!connection.connect()){
                publishProgress(getString(R.string.error_connect_failed), Constants.STEP_STATE_ERROR);
                return false;
            }
            else{
                publishProgress(getString(R.string.connected), Constants.STEP_STATE_WORK);
            }

            if(isCanceled())
                return false;

            // step 1: find keys

            publishProgress(getString(R.string.check_tables_exist), Constants.STEP_STATE_WORK);

            Map<String, Long> keys = new HashMap<>();
            keys.put(Constants.KEY_INSPECTORS, -1L);
            keys.put(Constants.KEY_DOCUMENTS, -1L);
            keys.put(Constants.KEY_SHEET, -1L);
            keys.put(Constants.KEY_PRODUCTION, -1L);
            keys.put(Constants.KEY_NOTES, -1L);
            keys.put(Constants.KEY_VEHICLES, -1L);
            keys.put(Constants.KEY_KV, -1L);
            keys.put(Constants.KEY_LV, -1L);
            keys.put(Constants.KEY_ULV, -1L);
            keys.put(Constants.KEY_VIOLATE_TYPES, -1L);
            keys.put(Constants.KEY_SPECIES_TYPES, -1L);
            keys.put(Constants.KEY_FOREST_CAT_TYPES, -1L);
            keys.put(Constants.KEY_THICKNESS_TYPES, -1L);
            keys.put(Constants.KEY_TREES_TYPES, -1L);
            keys.put(Constants.KEY_HEIGHT_TYPES, -1L);
            keys.put(Constants.KEY_FIELDWORK_TYPES, -1L);
            keys.put(Constants.KEY_CONTRACT_TYPES, -1L);
            keys.put(Constants.KEY_FV, -1L);
            keys.put(Constants.KEY_FV_REGIONS, -1L);

            if(!checkServerLayers(connection, keys)){
                publishProgress(getString(R.string.error_wrong_server), Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            if(isCanceled())
                return false;

            // step 2: get inspector detail
            // name, description, bbox
            mStep = 1;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if(!getInspectorDetail(connection, keys.get(Constants.KEY_INSPECTORS), sLogin)){
                publishProgress(getString(R.string.error_get_inspector_detail), Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            if(isCanceled())
                return false;

            // step 3: create base layers

            mStep = 2;
            MapBase map = app.getMap();

            createBasicLayers(map);

            if(isCanceled())
                return false;

            // step 4: forest cadastre

            mStep = 3;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadForestCadastre(keys.get(Constants.KEY_LV), keys.get(Constants.KEY_ULV), keys.get(Constants.KEY_KV), mAccount.name, map, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            if(isCanceled())
                return false;

            // step 5: load documents

            mStep = 4;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadDocuments(keys.get(Constants.KEY_DOCUMENTS), mAccount.name, map, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            if(isCanceled())
                return false;

            // step 6: load sheets

            mStep = 5;
            int nSubStep = 1;
            int nTotalSubSteps = 9;
            DocumentsLayer documentsLayer = null;

            for(int i = 0; i < map.getLayerCount(); i++){
                ILayer layer = map.getLayer(i);
                if(layer instanceof DocumentsLayer){
                    documentsLayer = (DocumentsLayer) layer;
                }
            }

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadLinkedTables(keys.get(Constants.KEY_SHEET), mAccount.name,
                    Constants.KEY_LAYER_SHEET, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);

                return false;
            }

            if(isCanceled())
                return false;

            // step 6: load productions

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLinkedTables(keys.get(Constants.KEY_PRODUCTION), mAccount.name,
                    Constants.KEY_LAYER_PRODUCTION, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            // step 6: load vehicles

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLinkedTables(keys.get(Constants.KEY_VEHICLES), mAccount.name,
                    Constants.KEY_LAYER_VEHICLES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(
                    keys.get(Constants.KEY_VIOLATE_TYPES), mAccount.name,
                    Constants.KEY_LAYER_VIOLATE_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(
                    keys.get(Constants.KEY_SPECIES_TYPES), mAccount.name,
                    Constants.KEY_LAYER_SPECIES_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_TREES_TYPES), mAccount.name,
                    Constants.KEY_LAYER_TREES_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_HEIGHT_TYPES), mAccount.name,
                    Constants.KEY_LAYER_HEIGHT_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_THICKNESS_TYPES), mAccount.name,
                    Constants.KEY_LAYER_THICKNESS_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);

                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);

            if (!loadLookupTables(keys.get(Constants.KEY_FOREST_CAT_TYPES), mAccount.name,
                    Constants.KEY_LAYER_FOREST_CAT_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(
                    keys.get(Constants.KEY_FIELDWORK_TYPES), mAccount.name,
                    Constants.KEY_LAYER_FIELDWORK_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(
                    keys.get(Constants.KEY_CONTRACT_TYPES), mAccount.name,
                    Constants.KEY_LAYER_CONTRACT_TYPES, documentsLayer, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }

            if(isCanceled())
                return false;

            // step 7: load notes

            mStep = 6;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadNotes(keys.get(Constants.KEY_NOTES), mAccount.name, map, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            // step 8: forest violation targeting

            mStep = 7;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadTargeting(keys.get(Constants.KEY_FV), mAccount.name, map, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            // step 9: regions

            mStep = 8;

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            if (!loadRegions(keys.get(Constants.KEY_FV_REGIONS), mAccount.name, map, this)){
                publishProgress(getString(R.string.error_unexpected), Constants.STEP_STATE_ERROR);
                return false;
            }
            else {
                publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
            }

            //TODO: load additional tables

            map.save();

            mStep = MAX_SYNC_STEP; // add extra step to finish view
            publishProgress(getString(R.string.done), Constants.STEP_STATE_FINISH);
            Log.d(Constants.FITAG, "init work is finished");

            return true;
        }


        protected void createBasicLayers(MapBase map){

            publishProgress(getString(R.string.working), Constants.STEP_STATE_WORK);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InitService.this);
            float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
            float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
            float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
            float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

            //add OpenStreetMap layer on application first run
            String layerName = getString(R.string.osm);
            String layerURL = SettingsConstantsUI.OSM_URL;
            final RemoteTMSLayerUI osmLayer =
                    new RemoteTMSLayerUI(getApplicationContext(), map.createLayerStorage());
            osmLayer.setName(layerName);
            osmLayer.setURL(layerURL);
            osmLayer.setTMSType(GeoConstants.TMSTYPE_OSM);
            osmLayer.setMaxZoom(22);
            osmLayer.setMinZoom(12.4f);
            osmLayer.setVisible(true);

            map.addLayer(osmLayer);
            //mMap.moveLayer(0, osmLayer);
            GeoEnvelope extent = new GeoEnvelope(minX, maxX, minY, maxY);

        /*
        if(extent.isInit()) {
            try {
                downloadTiles(osmLayer, initAsyncTask, nStep, map.getFullBounds(), extent, 12, 13);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

            String kosmosnimkiLayerName = getString(R.string.topo);
            String kosmosnimkiLayerURL = SettingsConstants.KOSOSNIMKI_URL;
            RemoteTMSLayerUI ksLayer =
                    new RemoteTMSLayerUI(getApplicationContext(), map.createLayerStorage());
            ksLayer.setName(kosmosnimkiLayerName);
            ksLayer.setURL(kosmosnimkiLayerURL);
            ksLayer.setTMSType(GeoConstants.TMSTYPE_OSM);
            ksLayer.setMaxZoom(12.4f);
            ksLayer.setMinZoom(0);
            ksLayer.setVisible(true);

            map.addLayer(ksLayer);
            //mMap.moveLayer(1, ksLayer);

            if(extent.isInit()) {
                //download
                try {
                    downloadTiles(ksLayer, extent, 5, 12);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String mixerLayerName = getString(R.string.geomixer_fv_tiles);
            String mixerLayerURL = SettingsConstants.VIOLATIONS_URL;
            RemoteTMSLayerUI mixerLayer =
                    new RemoteTMSLayerUI(getApplicationContext(), map.createLayerStorage());
            mixerLayer.setName(mixerLayerName);
            mixerLayer.setURL(mixerLayerURL);
            mixerLayer.setTMSType(GeoConstants.TMSTYPE_OSM);
            mixerLayer.setMaxZoom(19);
            mixerLayer.setMinZoom(0);
            mixerLayer.setTileMaxAge(com.nextgis.maplib.util.Constants.ONE_DAY);
            mixerLayer.setVisible(true);

            map.addLayer(mixerLayer);
            //mMap.moveLayer(2, mixerLayer);

            //set extent
            if(map instanceof MapDrawable && extent.isInit()) {
                ((MapDrawable) map).zoomToExtent(extent);
            }

            publishProgress(getString(R.string.done), Constants.STEP_STATE_DONE);
        }

        private void downloadTiles(final RemoteTMSLayerUI osmLayer, GeoEnvelope loadBounds, int zoomFrom, int zoomTo) throws InterruptedException {
            //download
            publishProgress(getString(R.string.form_tiles_list), Constants.STEP_STATE_WORK);
            final List<TileItem> tilesList = new LinkedList<>();
            for(int zoom = zoomFrom; zoom < zoomTo + 1; zoom++) {
                tilesList.addAll(MapUtil.getTileItems(loadBounds, zoom, osmLayer.getTMSType()));
            }

            int threadCount = Constants.DOWNLOAD_SEPARATE_THREADS;
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    threadCount, threadCount, com.nextgis.maplib.util.Constants.KEEP_ALIVE_TIME,
                    com.nextgis.maplib.util.Constants.KEEP_ALIVE_TIME_UNIT,
                    new LinkedBlockingQueue<Runnable>(), new RejectedExecutionHandler()
            {
                @Override
                public void rejectedExecution(
                        Runnable r,
                        ThreadPoolExecutor executor)
                {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        //throw new RuntimeException("Interrupted while submitting task", e);
                    }
                }
            });

            int tilesSize = tilesList.size();
            List<Future> futures = new ArrayList<>(tilesSize);

            for (int i = 0; i < tilesSize; ++i) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                final TileItem tile = tilesList.get(i);

                futures.add(
                        threadPool.submit(
                                new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        android.os.Process.setThreadPriority(
                                                com.nextgis.maplib.util.Constants.DEFAULT_DRAW_THREAD_PRIORITY);
                                        osmLayer.downloadTile(tile);
                                    }
                                }));
            }

            // wait for download ending
            int nProgressStep = futures.size() / com.nextgis.maplib.util.Constants.DRAW_NOTIFY_STEP_PERCENT;
            if(nProgressStep == 0)
                nProgressStep = 1;
            double percentFract = 100.0 / futures.size();

            for (int i = 0, futuresSize = futures.size(); i < futuresSize; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                try {
                    Future future = futures.get(i);
                    future.get(); // wait for task ending

                    if(i % nProgressStep == 0) {
                        int percent = (int) (i * percentFract);
                        publishProgress(percent + "% " + getString(R.string.downloaded), Constants.STEP_STATE_WORK);
                    }

                } catch (CancellationException | InterruptedException e) {
                    //e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        protected boolean checkServerLayers(INGWResource resource, Map<String, Long> keys){
            if (resource instanceof Connection) {
                Connection connection = (Connection) resource;
                connection.loadChildren();
            }
            else if (resource instanceof ResourceGroup) {
                ResourceGroup resourceGroup = (ResourceGroup) resource;
                resourceGroup.loadChildren();
            }

            for(int i = 0; i < resource.getChildrenCount(); ++i){
                INGWResource childResource = resource.getChild(i);

                if(keys.containsKey(childResource.getKey()) && childResource instanceof Resource) {
                    Resource ngwResource = (Resource) childResource;
                    keys.put(ngwResource.getKey(), ngwResource.getRemoteId());
                }

                boolean bIsFill = true;
                for (Map.Entry<String, Long> entry : keys.entrySet()) {
                    if(entry.getValue() <= 0){
                        bIsFill = false;
                        break;
                    }
                }

                if(bIsFill){
                    return true;
                }

                if(checkServerLayers(childResource, keys)){
                    return true;
                }
            }

            boolean bIsFill = true;

            for (Map.Entry<String, Long> entry : keys.entrySet()) {
                if(entry.getValue() <= 0){
                    bIsFill = false;
                    break;
                }
            }

            return bIsFill;
        }

        protected boolean getInspectorDetail(Connection connection, long resourceId, String login){

            String sURL = NGWUtil.getFeaturesUrl(connection.getURL(), resourceId, "login=" + login);

            try {
                String sResponse = NetworkUtil.get(
                        sURL, connection.getLogin(), connection.getPassword());
                if(null == sResponse)
                    return false;

                JSONArray features = new JSONArray(sResponse);
                if(features.length() == 0)
                    return false;

                JSONObject jsonDetail = features.getJSONObject(0);
                int id = jsonDetail.getInt(NGWUtil.NGWKEY_ID);
                GeoGeometry geom = GeoGeometryFactory.fromWKT(
                        jsonDetail.getString(NGWUtil.NGWKEY_GEOM));
                GeoEnvelope env = geom.getEnvelope();

                JSONObject fields = jsonDetail.getJSONObject(NGWUtil.NGWKEY_FIELDS);
                String sUserName = fields.getString(Constants.KEY_INSPECTOR_USER);
                String sUserDescription = fields.getString(Constants.KEY_INSPECTOR_USER_DESC);
                String sUserPassId = fields.getString(Constants.KEY_INSPECTOR_USER_PASS_ID);

                // if no exception store data in config
                final SharedPreferences.Editor edit =
                        PreferenceManager.getDefaultSharedPreferences(InitService.this).edit();
                edit.putInt(SettingsConstants.KEY_PREF_USERID, id);
                edit.putString(SettingsConstants.KEY_PREF_USER, sUserName);
                edit.putString(SettingsConstants.KEY_PREF_USERDESC, sUserDescription);
                edit.putString(SettingsConstants.KEY_PREF_USERPASSID, sUserPassId);
                edit.putFloat(SettingsConstants.KEY_PREF_USERMINX, (float) env.getMinX());
                edit.putFloat(SettingsConstants.KEY_PREF_USERMINY, (float) env.getMinY());
                edit.putFloat(SettingsConstants.KEY_PREF_USERMAXX, (float) env.getMaxX());
                edit.putFloat(SettingsConstants.KEY_PREF_USERMAXY, (float) env.getMaxY());
                return edit.commit();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        protected boolean loadForestCadastre(long lvId, long ulvId, long kvId, String accountName, MapBase map, IProgressor progressor){
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InitService.this);
            float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
            float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
            float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
            float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

            NGWVectorLayerUI ngwLVVectorLayer =
                    new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_LV));
            ngwLVVectorLayer.setName(getString(R.string.lv));
            ngwLVVectorLayer.setRemoteId(lvId);
            ngwLVVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                    minX, minY, maxX, maxY));
            ngwLVVectorLayer.setVisible(true);
            ngwLVVectorLayer.setAccountName(accountName);
            ngwLVVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_NONE);
            ngwLVVectorLayer.setMinZoom(0);
            ngwLVVectorLayer.setMaxZoom(9.5f);
            //TODO: add layer draw default style and labels
            SimpleTiledPolygonStyle lvStyle = new SimpleTiledPolygonStyle(getResources().getColor(R.color.primary_dark));
            lvStyle.setFill(false);
            SimpleFeatureRenderer lvRenderer = new SimpleFeatureRenderer(ngwLVVectorLayer, lvStyle);
            ngwLVVectorLayer.setRenderer(lvRenderer);

            map.addLayer(ngwLVVectorLayer);

            try {
                ngwLVVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            NGWVectorLayerUI ngwULVVectorLayer =
                    new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_ULV));
            ngwULVVectorLayer.setName(getString(R.string.ulv));
            ngwULVVectorLayer.setRemoteId(ulvId);
            ngwULVVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                    minX, minY, maxX, maxY));
            ngwULVVectorLayer.setVisible(true);
            ngwULVVectorLayer.setAccountName(accountName);
            ngwULVVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_NONE);
            ngwULVVectorLayer.setMinZoom(9.5f);
            ngwULVVectorLayer.setMaxZoom(14.5f);
            //TODO: add layer draw default style and labels
            SimpleTiledPolygonStyle ulvStyle = new SimpleTiledPolygonStyle(getResources().getColor(R.color.primary_dark));
            ulvStyle.setFill(false);
            SimpleFeatureRenderer ulvRenderer = new SimpleFeatureRenderer(ngwULVVectorLayer, ulvStyle);
            ngwULVVectorLayer.setRenderer(ulvRenderer);

            map.addLayer(ngwULVVectorLayer);

            try {
                ngwULVVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            NGWVectorLayerUI ngwKVVectorLayer =
                    new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_KV));
            ngwKVVectorLayer.setName(getString(R.string.cadastre));
            ngwKVVectorLayer.setRemoteId(kvId);
            ngwKVVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                    minX, minY, maxX, maxY));
            ngwKVVectorLayer.setVisible(true);
            ngwKVVectorLayer.setAccountName(accountName);
            ngwKVVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_NONE);
            ngwKVVectorLayer.setMinZoom(14.5f);
            ngwKVVectorLayer.setMaxZoom(23);
            //TODO: add layer draw default style and labels
            SimpleTiledPolygonStyle style = new SimpleTiledPolygonStyle(getResources().getColor(R.color.primary_dark));
            style.setFill(false);
            SimpleFeatureRenderer renderer = new SimpleFeatureRenderer(ngwKVVectorLayer, style);
            ngwKVVectorLayer.setRenderer(renderer);

            map.addLayer(ngwKVVectorLayer);

            try {
                ngwKVVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected boolean loadDocuments(long resourceId, String accountName, MapBase map, IProgressor progressor){
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InitService.this);
            float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
            float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
            float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
            float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

            DocumentsLayer ngwVectorLayer =
                    new DocumentsLayer(getApplicationContext(),
                            map.createLayerStorage(Constants.KEY_LAYER_DOCUMENTS),
                            map.getLayerFactory());
            ngwVectorLayer.setName(getString(R.string.documents_layer));
            ngwVectorLayer.setRemoteId(resourceId);
            ngwVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                    minX, minY, maxX, maxY));
            ngwVectorLayer.setVisible(true);
            ngwVectorLayer.setAccountName(accountName);
            ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_ALL);
            ngwVectorLayer.setMinZoom(0);
            ngwVectorLayer.setMaxZoom(25);

            map.addLayer(ngwVectorLayer);

            try {
                ngwVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected boolean loadLinkedTables(long resourceId, String accountName, String layerName,
                                           DocumentsLayer docs, IProgressor progressor){
            if(null == docs)
                return false;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    InitService.this);
            float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
            float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
            float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
            float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

            NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(getApplicationContext(),
                    docs.createLayerStorage(layerName));

            ngwVectorLayer.setName(layerName);
            ngwVectorLayer.setRemoteId(resourceId);
            ngwVectorLayer.setServerWhere(
                    String.format(Locale.US, "bbox=%f,%f,%f,%f", minX, minY, maxX, maxY));
            ngwVectorLayer.setVisible(false);
            ngwVectorLayer.setAccountName(accountName);
            ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
            ngwVectorLayer.setMinZoom(0);
            ngwVectorLayer.setMaxZoom(25);

            docs.addLayer(ngwVectorLayer);

            try {
                ngwVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        protected boolean loadLookupTables(long resourceId, String accountName, String layerName,
                                           DocumentsLayer docs, IProgressor progressor){

            NGWLookupTable ngwTable = new NGWLookupTable(getApplicationContext(),
                    docs.createLayerStorage(layerName));

            ngwTable.setName(layerName);
            ngwTable.setRemoteId(resourceId);
            ngwTable.setAccountName(accountName);
            ngwTable.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);

            try {
                ngwTable.fillFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            docs.addLayer(ngwTable);

            return true;
        }

        protected boolean loadNotes(long resourceId, String accountName, MapBase map, IProgressor progressor) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    InitService.this);
            long inspectorId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);

            NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(
                    getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_NOTES));
            ngwVectorLayer.setName(getString(R.string.notes));
            ngwVectorLayer.setRemoteId(resourceId);
            ngwVectorLayer.setServerWhere(Constants.KEY_NOTES_USERID + "=" + inspectorId);
            ngwVectorLayer.setVisible(true);
            //TODO: add layer draw default style and quarter labels
            ngwVectorLayer.setAccountName(accountName);
            ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
            ngwVectorLayer.setMinZoom(0);
            ngwVectorLayer.setMaxZoom(25);

            map.addLayer(ngwVectorLayer);

            try {
                ngwVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }


        protected boolean loadTargeting(
                long resourceId,
                String accountName,
                MapBase map,
                IProgressor progressor)
        {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    InitService.this);
            float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
            float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
            float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
            float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

            NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(
                    getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_FV));
            ngwVectorLayer.setName(getString(R.string.targeting));
            ngwVectorLayer.setRemoteId(resourceId);
            ngwVectorLayer.setServerWhere(
                    String.format(Locale.US, "bbox=%f,%f,%f,%f", minX, minY, maxX, maxY));
            ngwVectorLayer.setVisible(true);
            //TODO: add layer draw default style and quarter labels
            ngwVectorLayer.setAccountName(accountName);
            ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
            ngwVectorLayer.setMinZoom(0);
            ngwVectorLayer.setMaxZoom(25);

            map.addLayer(ngwVectorLayer);

            try {
                ngwVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }


        protected boolean loadRegions(
                long resourceId,
                String accountName,
                MapBase map,
                IProgressor progressor)
        {
            NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(
                    getApplicationContext(),
                    map.createLayerStorage(Constants.KEY_LAYER_FV_REGIONS));
            ngwVectorLayer.setName(getString(R.string.regions));
            ngwVectorLayer.setRemoteId(resourceId);
            ngwVectorLayer.setVisible(false);
            //TODO: add layer draw default style and quarter labels
            ngwVectorLayer.setAccountName(accountName);
            ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
            ngwVectorLayer.setMinZoom(0);
            ngwVectorLayer.setMaxZoom(25);

            map.addLayer(ngwVectorLayer);

            try {
                ngwVectorLayer.createFromNGW(progressor);
            } catch (NGException | IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
