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

package com.nextgis.forestinspector.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.InitStepListAdapter;
import com.nextgis.forestinspector.fragment.DocumentsFragment;
import com.nextgis.forestinspector.fragment.LoginFragment;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.datasource.TileItem;
import com.nextgis.maplib.datasource.ngw.Connection;
import com.nextgis.maplib.datasource.ngw.INGWResource;
import com.nextgis.maplib.datasource.ngw.Resource;
import com.nextgis.maplib.datasource.ngw.ResourceGroup;
import com.nextgis.maplib.display.SimpleFeatureRenderer;
import com.nextgis.maplib.display.SimplePolygonStyle;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.NGWUtil;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.mapui.MapView;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends FIActivity implements NGWLoginFragment.OnAddAccountListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    protected ViewPager mViewPager;
    protected boolean mFirsRun;
    protected InitStepListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if first run
        final MainApplication app = (MainApplication) getApplication();
        if(app == null){
            Log.d(Constants.FITAG, "failed to get main application");
            // should never happen
            mFirsRun = true;
            createFirstStartView();
        }

        final Account account = app.getAccount(getString(R.string.account_name));
        if(account == null){
            Log.d(Constants.FITAG, "No account" + getString(R.string.account_name) + " created. Run first step.");
            mFirsRun = true;
            createFirstStartView();
        }
        else {
            MapBase map = app.getMap();
            if(map.getLayerCount() <= 0)
            {
                Log.d(Constants.FITAG, "Account" + getString(R.string.account_name) + " created. Run second step.");
                mFirsRun = true;
                createSecondStartView(account);
            }
            else {
                Log.d(Constants.FITAG, "Account" + getString(R.string.account_name) + " created. Layers created. Run normal view.");
                mFirsRun = false;
                createNormalView();
            }
        }

    }

    protected void createFirstStartView(){
        setContentView(R.layout.activity_main_first);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.first_run));

        FragmentManager fm = getSupportFragmentManager();
        NGWLoginFragment ngwLoginFragment = (NGWLoginFragment) fm.findFragmentByTag("NGWLogin");

        if (ngwLoginFragment == null) {
            ngwLoginFragment = new LoginFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(com.nextgis.maplibui.R.id.login_frame, ngwLoginFragment, "NGWLogin");
            ft.commit();
        }
        ngwLoginFragment.setForNewAccount(true);
        ngwLoginFragment.setOnAddAccountListener(this);
    }

    protected void createSecondStartView(Account account){
        setContentView(R.layout.activity_main_second);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.initialization));

        mAdapter = new InitStepListAdapter(this);

        ListView list = (ListView) findViewById(R.id.stepsList);
        list.setAdapter(mAdapter);

        final InitAsyncTask task = new InitAsyncTask(account);

        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.cancel(true);
            }
        });

        task.execute();
    }

    protected void createNormalView(){

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.app_name));

        // Create the adapter that will return a fragment for each of the primary sections of the
        // activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if(tabLayout.getTabCount() < mSectionsPagerAdapter.getCount()) {
            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                // Create a tab with text corresponding to the page title defined by
                // the adapter. Also specify this Activity object, which implements
                // the TabListener interface, as the callback (listener) for when
                // this tab is selected.
                tabLayout.addTab(tabLayout.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)));
            }
        }

        final View addIndictment = findViewById(R.id.add_indictment);
        if (null != addIndictment) {
            addIndictment.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addIndictment();
                        }
                    });
        }

        final View addSheet = findViewById(R.id.add_sheet);
        if (null != addSheet) {
            addSheet.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addSheet();
                        }
                    });
        }

        final View addBookmark = findViewById(R.id.add_bookmark);
        if (null != addBookmark) {
            addBookmark.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addBookmark();
                        }
                    });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!mFirsRun)
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final IGISApplication app = (IGISApplication) getApplication();
            app.showSettings();
            return true;
        }
        else if (id == R.id.action_about) {
            Intent intentAbout = new Intent(this, AboutActivity.class);
            startActivity(intentAbout);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setToolbar(int toolbarId){
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);
    }

    @Override
    public void onAddAccount(Account account, String token, boolean accountAdded) {
        if(accountAdded) {

            //free any map data here
            final MainApplication app = (MainApplication) getApplication();
            MapBase map = app.getMap();

            // delete all layers from map if any
            map.delete();

            //set sync with server
            ContentResolver.setSyncAutomatically(account, app.getAuthority(), true);
            ContentResolver.addPeriodicSync( account, app.getAuthority(), Bundle.EMPTY,
                    com.nextgis.maplib.util.Constants.DEFAULT_SYNC_PERIOD);

            // goto step 2
            refreshActivityView();
        }
        else
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
    }

    protected void createBasicLayers(MapBase map, final InitAsyncTask initAsyncTask, final int nStep){

        initAsyncTask.publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -180.0f);
        float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -90.0f);
        float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 180.0f);
        float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 90.0f);

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

        if(extent.isInit()) {
            try {
                downloadTiles(osmLayer, initAsyncTask, nStep, map.getFullBounds(), extent, 12, 15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
                downloadTiles(ksLayer, initAsyncTask, nStep, map.getFullBounds(), extent, 0, 12);
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
        mixerLayer.setMaxZoom(25);
        mixerLayer.setMinZoom(0);
        mixerLayer.setVisible(true);

        map.addLayer(mixerLayer);
        //mMap.moveLayer(2, mixerLayer);

        //set extent
        if(map instanceof MapDrawable && extent.isInit()) {
            MapDrawable mapDrawable = (MapDrawable) map;
            double size = GeoConstants.MERCATOR_MAX * 2;
            double scale = Math.min(extent.width() / size, extent.height() / size);
            double zoom = MapView.lg(1 / scale);
            mapDrawable.setZoomAndCenter((float) zoom, extent.getCenter());
        }

        initAsyncTask.publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
    }

    private void downloadTiles(final RemoteTMSLayerUI osmLayer, final InitAsyncTask initAsyncTask, final int nStep, final GeoEnvelope fullBound, GeoEnvelope loadBounds, int zoomFrom, int zoomTo) throws InterruptedException {
        //download
        initAsyncTask.publishProgress(getString(R.string.form_tiles_list), nStep, Constants.STEP_STATE_WORK);
        final List<TileItem> tilesList = new ArrayList<>();
        for(int zoom = zoomFrom; zoom < zoomTo + 1; zoom++) {
            tilesList.addAll(osmLayer.getTielsForBounds(fullBound, loadBounds, zoom));
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

        final int[] tileCompleteCount = {0, tilesList.size() / 100};
        if(tileCompleteCount[1] == 0)
            tileCompleteCount[1] = 1;

        for (int i = 0; i < tilesList.size(); ++i) {
            final TileItem tile = tilesList.get(i);
            threadPool.execute(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        android.os.Process.setThreadPriority(
                                com.nextgis.maplib.util.Constants.DEFAULT_DOWNLOAD_THREAD_PRIORITY);

                        osmLayer.downloadTile(tile);

                        synchronized (osmLayer) {
                            tileCompleteCount[0]++;
                            if(tileCompleteCount[0] % tileCompleteCount[1] == 0) {
                                //progress
                                float complete = tileCompleteCount[0];
                                int percent = (int) (complete * 100 / tilesList.size());
                                initAsyncTask.publishProgress(percent + "% " + getString(R.string.downloaded), nStep, Constants.STEP_STATE_WORK);
                            }
                        }
                    }
                }
            );
        }
        threadPool.shutdown();
        //wait until downloaded end or 10 minutes
        threadPool.awaitTermination(600, com.nextgis.maplib.util.Constants.KEEP_ALIVE_TIME_UNIT);
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
            HttpGet get = new HttpGet(sURL);
            get.setHeader("Cookie", connection.getCookie());
            get.setHeader("Accept", "*/*");
            HttpResponse response = connection.getHttpClient().execute(get);
            HttpEntity entity = response.getEntity();

            JSONArray features = new JSONArray(EntityUtils.toString(entity));
            if(features.length() == 0)
                return false;

            JSONObject jsonDetail = features.getJSONObject(0);
            int id = jsonDetail.getInt(NGWUtil.NGWKEY_ID);
            GeoGeometry geom = GeoGeometryFactory.fromWKT(jsonDetail.getString(NGWUtil.NGWKEY_GEOM));
            GeoEnvelope env = geom.getEnvelope();

            JSONObject fields = jsonDetail.getJSONObject(NGWUtil.NGWKEY_FIELDS);
            String sUserName = fields.getString(Constants.KEY_INSPECTOR_USER);
            String sUserDescription = fields.getString(Constants.KEY_INSPECTOR_USER_DESC);
            String sUserPassId = fields.getString(Constants.KEY_INSPECTOR_USER_PASS_ID);

            // if no exception store data in config
            final SharedPreferences.Editor edit =
                    PreferenceManager.getDefaultSharedPreferences(this).edit();
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

    protected boolean loadForestCadastre(long resourceId, String accountName, MapBase map){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -180.0f);
        float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -90.0f);
        float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 180.0f);
        float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 90.0f);

        NGWVectorLayerUI ngwVectorLayer =
                new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_CADASTRE));
        ngwVectorLayer.setName(getString(R.string.cadastre));
        ngwVectorLayer.setRemoteId(resourceId);
        ngwVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                minX, minY, maxX, maxY));
        ngwVectorLayer.setVisible(true);
        //TODO: add layer draw default style and quarter labels
        ngwVectorLayer.setAccountName(accountName);
        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_NONE);
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);
        SimplePolygonStyle style = new SimplePolygonStyle(getResources().getColor(R.color.primary_dark));
        style.setFill(false);
        SimpleFeatureRenderer renderer = new SimpleFeatureRenderer(ngwVectorLayer, style);
        ngwVectorLayer.setRenderer(renderer);

        map.addLayer(ngwVectorLayer);

        return ngwVectorLayer.download() == null;
    }

    protected boolean loadDocuments(long resourceId, String accountName, MapBase map){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -180.0f);
        float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -90.0f);
        float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 180.0f);
        float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 90.0f);

        DocumentsLayer ngwVectorLayer =
                new DocumentsLayer(getApplicationContext(),
                        map.createLayerStorage(Constants.KEY_LAYER_DOCUMENTS),
                        map.getLayerFactory());
        ngwVectorLayer.setName(getString(R.string.documents_layer));
        ngwVectorLayer.setRemoteId(resourceId);
        ngwVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                minX, minY, maxX, maxY));
        ngwVectorLayer.setVisible(true);
        //TODO: add layer draw default style and quarter labels
        ngwVectorLayer.setAccountName(accountName);
        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_ALL);
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);
        SimplePolygonStyle style = new SimplePolygonStyle(Color.RED);
        SimpleFeatureRenderer renderer = new SimpleFeatureRenderer(ngwVectorLayer, style);
        ngwVectorLayer.setRenderer(renderer);

        map.addLayer(ngwVectorLayer);

        return ngwVectorLayer.download() == null;
    }

    protected boolean loadLinkedTables(long resourceId, String accountName, String layerName,
                                       DocumentsLayer docs){
        if(null == docs)
            return false;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -180.0f);
        float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -90.0f);
        float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 180.0f);
        float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 90.0f);

        NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(getApplicationContext(),
                docs.createLayerStorage(layerName));

        ngwVectorLayer.setName(layerName);
        ngwVectorLayer.setRemoteId(resourceId);
        ngwVectorLayer.setServerWhere(String.format(Locale.US, "bbox=%f,%f,%f,%f",
                minX, minY, maxX, maxY));
        ngwVectorLayer.setVisible(false);
        ngwVectorLayer.setAccountName(accountName);
        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);

        docs.addLayer(ngwVectorLayer);

        return ngwVectorLayer.download() == null;
    }

    protected boolean loadLookupTables(long resourceId, String accountName, String layerName,
                                       DocumentsLayer docs){

        NGWLookupTable ngwTable = new NGWLookupTable(getApplicationContext(),
                docs.createLayerStorage(layerName));

        ngwTable.setName(layerName);
        ngwTable.setRemoteId(resourceId);
        ngwTable.setAccountName(accountName);
        ngwTable.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);

        docs.addLayer(ngwTable);

        return ngwTable.download() == null;
    }

    protected boolean loadNotes(long resourceId, String accountName, MapBase map){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long inspectorId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);

        NGWVectorLayerUI ngwVectorLayer =
                new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage(Constants.KEY_LAYER_NOTES));
        ngwVectorLayer.setName(getString(R.string.notes));
        ngwVectorLayer.setRemoteId(resourceId);
        ngwVectorLayer.setServerWhere(Constants.KEY_NOTES_USERID + "=" + inspectorId);
        ngwVectorLayer.setVisible(true);
        //TODO: add layer draw default style and quarter labels
        ngwVectorLayer.setAccountName(accountName);
        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);

        map.addLayer(ngwVectorLayer);

        return ngwVectorLayer.download() == null;
    }

    public void addIndictment() {
        Intent intentAbout = new Intent(this, IndictmentActivity.class);
        startActivity(intentAbout);
    }

    public void addSheet() {

    }

    public void addBookmark() {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return new DocumentsFragment();
            }
            else{
                return new MapFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_notes).toUpperCase(l);
                case 1:
                    return getString(R.string.title_map).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A async task to execute resources functions (connect, loadChildren, etc.) asynchronously.
     */
    protected class InitAsyncTask
            extends AsyncTask<Void, Integer, Boolean>
    {
        protected String mMessage;
        protected Account mAccount;

        public InitAsyncTask(Account account) {
            mAccount = account;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // step 1: connect to server
            int nStep = 0;
            int nTimeout = 4000;
            final MainApplication app = (MainApplication) getApplication();
            final String sLogin = app.getAccountLogin(mAccount);
            final String sPassword = app.getAccountPassword(mAccount);
            final String sURL = app.getAccountUrl(mAccount);

            if (null == sURL || null == sPassword || null == sLogin) {
                return false;
            }

            Connection connection = new Connection("tmp", sLogin, sPassword, sURL);
            publishProgress(getString(R.string.connecting), nStep, Constants.STEP_STATE_WORK);

            if(!connection.connect()){
                publishProgress(getString(R.string.error_connect_failed), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else{
                publishProgress(getString(R.string.connected), nStep, Constants.STEP_STATE_WORK);
            }

            if(isCancelled())
                return false;

            // step 1: find keys

            publishProgress(getString(R.string.check_tables_exist), nStep, Constants.STEP_STATE_WORK);

            Map<String, Long> keys = new HashMap<>();
            keys.put(Constants.KEY_INSPECTORS, -1L);
            keys.put(Constants.KEY_DOCUMENTS, -1L);
            keys.put(Constants.KEY_SHEET, -1L);
            keys.put(Constants.KEY_PRODUCTION, -1L);
            keys.put(Constants.KEY_NOTES, -1L);
            keys.put(Constants.KEY_VEHICLES, -1L);
            keys.put(Constants.KEY_CADASTRE, -1L);
            keys.put(Constants.KEY_VIOLATE_TYPES, -1L);
            keys.put(Constants.KEY_SPECIES_TYPES, -1L);
            keys.put(Constants.KEY_FOREST_CAT_TYPES, -1L);
            keys.put(Constants.KEY_THICKNESS_TYPES, -1L);

            if(!checkServerLayers(connection, keys)){
                publishProgress(getString(R.string.error_wrong_server), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            if(isCancelled())
                return false;

            // step 2: get inspector detail
            // name, description, bbox
            nStep = 1;

            publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

            if(!getInspectorDetail(connection, keys.get(Constants.KEY_INSPECTORS), sLogin)){
                publishProgress(getString(R.string.error_get_inspector_detail), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            if(isCancelled())
                return false;

            // step 3: create base layers

            nStep = 2;
            MapBase map = app.getMap();

            createBasicLayers(map, this, nStep);

            if(isCancelled())
                return false;

            // step 4: forest cadastre

            nStep = 3;

            publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

            if (!loadForestCadastre(keys.get(Constants.KEY_CADASTRE), mAccount.name, map)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            if(isCancelled())
                return false;

            // step 5: load documents

            nStep = 4;

            publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

            if (!loadDocuments(keys.get(Constants.KEY_DOCUMENTS), mAccount.name, map)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            if(isCancelled())
                return false;

            // step 6: load sheets

            nStep = 5;
            int nSubStep = 1;
            int nTotalSubSteps = 7;
            DocumentsLayer documentsLayer = null;

            for(int i = 0; i < map.getLayerCount(); i++){
                ILayer layer = map.getLayer(i);
                if(layer instanceof DocumentsLayer){
                    documentsLayer = (DocumentsLayer) layer;
                }
            }

            publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

            if (!loadLinkedTables(keys.get(Constants.KEY_SHEET), mAccount.name,
                    Constants.KEY_LAYER_SHEET, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            // step 6: load productions

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLinkedTables(keys.get(Constants.KEY_PRODUCTION), mAccount.name,
                    Constants.KEY_LAYER_PRODUCTION, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            // step 6: load vehicles

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLinkedTables(keys.get(Constants.KEY_VEHICLES), mAccount.name,
                    Constants.KEY_LAYER_VEHICLES, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_VIOLATE_TYPES), mAccount.name,
                    Constants.KEY_LAYER_VIOLATE_TYPES, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_SPECIES_TYPES), mAccount.name,
                    Constants.KEY_LAYER_SPECIES_TYPES, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);
            nSubStep++;

            if (!loadLookupTables(keys.get(Constants.KEY_THICKNESS_TYPES), mAccount.name,
                    Constants.KEY_LAYER_THICKNESS_TYPES, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }

            if(isCancelled())
                return false;

            publishProgress(nSubStep + " " + getString(R.string.of) + " " + nTotalSubSteps, nStep,
                    Constants.STEP_STATE_WORK);

            if (!loadLookupTables(keys.get(Constants.KEY_FOREST_CAT_TYPES), mAccount.name,
                    Constants.KEY_LAYER_FOREST_CAT_TYPES, documentsLayer)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            if(isCancelled())
                return false;

            // step 7: load notes

            nStep = 6;

            publishProgress(getString(R.string.working), nStep, Constants.STEP_STATE_WORK);

            if (!loadNotes(keys.get(Constants.KEY_NOTES), mAccount.name, map)){
                publishProgress(getString(R.string.error_unexpected), nStep, Constants.STEP_STATE_ERROR);

                try {
                    Thread.sleep(nTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
            else {
                publishProgress(getString(R.string.done), nStep, Constants.STEP_STATE_DONE);
            }

            //TODO: load additional tables

            map.save();

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            InitStepListAdapter.InitStep step =
                    (InitStepListAdapter.InitStep) mAdapter.getItem(values[0]);
            step.mStepDescription = mMessage;
            step.mState = values[1];

            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result){
                //delete map
                final MainApplication app = (MainApplication) getApplication();
                String accName = mAccount.name;
                app.removeAccount(mAccount);

                for(int i = 0; i < 10; i++){
                    if(app.getAccount(accName) == null)
                        break;
                }
            }
            refreshActivityView();
        }

        public final void publishProgress(String message, int step, int state) {
            mMessage = message;
            publishProgress(step, state);

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
