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

package com.nextgis.forestinspector.activity;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.DocumentsListAdapter;
import com.nextgis.forestinspector.adapter.InitStepListAdapter;
import com.nextgis.forestinspector.dialog.LayerListDialog;
import com.nextgis.forestinspector.fragment.DocumentsFragment;
import com.nextgis.forestinspector.fragment.LoginFragment;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.forestinspector.service.InitService;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.Layer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.util.Locale;


public class MainActivity
        extends FIActivity
        implements MainApplication.OnAccountAddedListener,
                   MainApplication.OnAccountDeletedListener,
                   MainApplication.OnReloadMapListener,
                   DocumentsListAdapter.OnDocLongClickListener,
                   IActivityWithMap
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected SectionsPagerAdapter mSectionsPagerAdapter;
    protected BroadcastReceiver    mSyncStatusReceiver;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    protected ViewPager           mViewPager;
    protected boolean             mFirstRun;
    protected InitStepListAdapter mAdapter;

    protected OnShowIndictmentsListener mOnShowIndictmentsListener;
    protected OnShowSheetsListener      mOnShowSheetsListener;
    protected OnShowFieldWorksListener  mOnShowFieldWorksListener;
    protected OnShowNotesListener       mOnShowNotesListener;

    protected boolean mMenuForMap = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // check if first run
        final MainApplication app = (MainApplication) getApplication();
        if (app == null) {
            Log.d(Constants.FITAG, "failed to get main application");
            // should never happen
            mFirstRun = true;
            createFirstStartView();
        }

        final Account account = app.getAccount();
        if (account == null) {
            Log.d(
                    Constants.FITAG,
                    "No account" + getString(R.string.account_name) + " created. Run first step.");
            mFirstRun = true;
            createFirstStartView();
        } else {
            MapBase map = app.getMap();
            if (map.getLayerCount() <= 0 || app.isInitServiceRunning()) {
                Log.d(
                        Constants.FITAG, "Account" + getString(R.string.account_name) +
                                " created. Run second step.");
                mFirstRun = true;
                createSecondStartView(account);
            } else {
                Log.d(Constants.FITAG, "Account" + getString(R.string.account_name) + " created.");
                Log.d(Constants.FITAG, "Map data updating.");
                updateMap(map);
                Log.d(Constants.FITAG, "Layers created. Run normal view.");
                mFirstRun = false;
                createNormalView();
            }
        }
    }


    protected void updateMap(MapBase map)
    {
        Layer lvLayer = (Layer) map.getLayerByPathName(Constants.KEY_LAYER_LV);
        Layer ulvLayer = (Layer) map.getLayerByPathName(Constants.KEY_LAYER_ULV);
        Layer kvLayer = (Layer) map.getLayerByPathName(Constants.KEY_LAYER_KV);

        boolean saveMap = false;

        if (Float.compare(lvLayer.getMinZoom(), Constants.LV_MIN_ZOOM) != 0) {
            lvLayer.setMinZoom(Constants.LV_MIN_ZOOM);
            saveMap = true;
        }
        if (Float.compare(lvLayer.getMaxZoom(), Constants.LV_MAX_ZOOM) != 0) {
            lvLayer.setMaxZoom(Constants.LV_MAX_ZOOM);
            saveMap = true;
        }

        if (Float.compare(ulvLayer.getMinZoom(), Constants.ULV_MIN_ZOOM) != 0) {
            ulvLayer.setMinZoom(Constants.ULV_MIN_ZOOM);
            saveMap = true;
        }
        if (Float.compare(ulvLayer.getMaxZoom(), Constants.ULV_MAX_ZOOM) != 0) {
            ulvLayer.setMaxZoom(Constants.ULV_MAX_ZOOM);
            saveMap = true;
        }

        if (Float.compare(kvLayer.getMinZoom(), Constants.KV_MIN_ZOOM) != 0) {
            kvLayer.setMinZoom(Constants.KV_MIN_ZOOM);
            saveMap = true;
        }
        if (Float.compare(kvLayer.getMaxZoom(), Constants.KV_MAX_ZOOM) != 0) {
            kvLayer.setMaxZoom(Constants.KV_MAX_ZOOM);
            saveMap = true;
        }

        if (saveMap) {
            map.save();
            Log.d(Constants.FITAG, "Map data is updated.");
        } else {
            Log.d(Constants.FITAG, "Map data is not need to update.");
        }
    }


    protected void createFirstStartView()
    {
        setContentView(R.layout.activity_main_first);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.first_run));

        MainApplication app = (MainApplication) getApplication();
        FragmentManager fm = getSupportFragmentManager();
        NGWLoginFragment ngwLoginFragment = (NGWLoginFragment) fm.findFragmentByTag("NGWLogin");

        if (ngwLoginFragment == null) {
            ngwLoginFragment = new LoginFragment();
            ngwLoginFragment.setOnAddAccountListener(app);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(com.nextgis.maplibui.R.id.login_frame, ngwLoginFragment, "NGWLogin");
            ft.commit();
        }
    }


    protected void createSecondStartView(final Account account)
    {
        setContentView(R.layout.activity_main_second);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.initialization));

        mAdapter = new InitStepListAdapter(this);

        ListView list = (ListView) findViewById(R.id.stepsList);
        list.setAdapter(mAdapter);

        mSyncStatusReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(
                    Context context,
                    Intent intent)
            {
                int step = intent.getIntExtra(Constants.KEY_STEP, 0);
                int state = intent.getIntExtra(Constants.KEY_STATE, 0);
                String message = intent.getStringExtra(Constants.KEY_MESSAGE);

                switch (state) {
                    case Constants.STEP_STATE_FINISH:
                        refreshActivityView();
                        break;

                    case Constants.STEP_STATE_WAIT:
                    case Constants.STEP_STATE_WORK:
                    case Constants.STEP_STATE_DONE:
                        mAdapter.setMessage(step, state, message);
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_MESSAGE);
        registerReceiver(mSyncStatusReceiver, intentFilter);

        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent syncIntent = new Intent(MainActivity.this, InitService.class);
                        syncIntent.setAction(InitService.ACTION_STOP);
                        startService(syncIntent);
                    }
                });

        MainApplication app = (MainApplication) getApplication();
        String action;
        if (app.isInitServiceRunning()) {
            action = InitService.ACTION_REPORT;
        } else {
            action = InitService.ACTION_START;
        }

        Intent syncIntent = new Intent(MainActivity.this, InitService.class);
        syncIntent.setAction(action);
        startService(syncIntent);
    }


    protected void createNormalView()
    {
        PreferenceManager.setDefaultValues(this, R.xml.preference_headers_legacy, false);

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

        if (tabLayout.getTabCount() < mSectionsPagerAdapter.getCount()) {
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
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addSheet();
                        }
                    });
        }

        final View addFieldWorks = findViewById(R.id.add_field_work);
        if (null != addFieldWorks) {
            addFieldWorks.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addFieldWorks();
                        }
                    });
        }

        final View addNote = findViewById(R.id.add_note);
        if (null != addNote) {
            addNote.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addNote();
                        }
                    });
        }
    }


    private static String makeFragmentName(
            int viewId,
            int index)
    {
        return "android:switcher:" + viewId + ":" + index;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mFirstRun) {
            return true;
        }

        getMenuInflater().inflate(R.menu.main, menu);

        if (mMenuForMap) {
            menu.findItem(R.id.layers_props).setVisible(true);
            menu.findItem(R.id.docs_filter).setVisible(false);

        } else {
            menu.findItem(R.id.layers_props).setVisible(false);
            menu.findItem(R.id.docs_filter).setVisible(true);

            FragmentManager fm = getSupportFragmentManager();
            DocumentsFragment documentsFragment =
                    (DocumentsFragment) fm.findFragmentByTag(makeFragmentName(R.id.pager, 0));

            if (null != documentsFragment) {
                setOnShowIndictmentsListener(documentsFragment);
                setOnShowSheetsListener(documentsFragment);
                setOnShowFieldWorksListener(documentsFragment);
                setOnShowNotesListener(documentsFragment);

                MenuItem itemShowIndictments = menu.findItem(R.id.show_indictments);
                MenuItem itemShowSheets = menu.findItem(R.id.show_sheets);
                MenuItem itemShowFieldWorks = menu.findItem(R.id.show_field_works);
                MenuItem itemShowNotes = menu.findItem(R.id.show_notes);

                itemShowIndictments.setChecked(documentsFragment.isShowIndictments());
                itemShowSheets.setChecked(documentsFragment.isShowSheets());
                itemShowFieldWorks.setChecked(documentsFragment.isShowFieldWorks());
                itemShowNotes.setChecked(documentsFragment.isShowNotes());
            }
        }

        return true;
    }


    @Override
    public void setMenuForMap(boolean menuForMap)
    {
        mMenuForMap = menuForMap;
        updateMenuView();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        final IGISApplication app = (IGISApplication) getApplication();

        switch (item.getItemId()) {
            case R.id.layers_props:
                showLayersProps();
                return true;

            case R.id.show_indictments:
                if (item.isChecked()) { item.setChecked(false); } else { item.setChecked(true); }
                showIndictments(item.isChecked());
                return true;

            case R.id.show_sheets:
                if (item.isChecked()) { item.setChecked(false); } else { item.setChecked(true); }
                showSheets(item.isChecked());
                return true;

            case R.id.show_field_works:
                if (item.isChecked()) { item.setChecked(false); } else { item.setChecked(true); }
                showFieldWorks(item.isChecked());
                return true;

            case R.id.show_notes:
                if (item.isChecked()) { item.setChecked(false); } else { item.setChecked(true); }
                showNotes(item.isChecked());
                return true;

            case R.id.action_add_indictment:
                addIndictment();
                return true;

            case R.id.action_add_sheet:
                addSheet();
                return true;

            case R.id.action_add_field_work:
                addFieldWorks();
                return true;

            case R.id.action_add_note:
                addNote();
                return true;
// for debug
//            case R.id.action_sync:
//                ((MainApplication) app).runSync();
//                return true;

            case R.id.action_settings:
                app.showSettings(SettingsConstantsUI.ACTION_PREFS_GENERAL);
                return true;

            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void setToolbar(int toolbarId)
    {
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        MainApplication app = (MainApplication) getApplication();
        app.setOnAccountAddedListener(null);
        app.setOnAccountDeletedListener(null);
        app.setOnReloadMapListener(null);

        if (null != mSyncStatusReceiver) {
            unregisterReceiver(mSyncStatusReceiver);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        MainApplication app = (MainApplication) getApplication();
        app.setOnAccountAddedListener(this);
        app.setOnAccountDeletedListener(this);
        app.setOnReloadMapListener(this);

        if (app.isAccountAdded() || app.isAccountDeleted() || app.isMapReloaded()) {
            refreshActivityView();
        }

        if (null != mSyncStatusReceiver) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_MESSAGE);
            registerReceiver(mSyncStatusReceiver, intentFilter);
        }
    }


    @Override
    public void onAccountAdded()
    {
        refreshActivityView();
    }


    @Override
    public void onAccountDeleted()
    {
        refreshActivityView();
    }


    @Override
    public void onReloadMap()
    {
        refreshActivityView();
    }


    public void addIndictment()
    {
        Intent intentIndictment = new Intent(this, IndictmentCreatorActivity.class);
        startActivity(intentIndictment);
    }


    public void addFieldWorks()
    {
        Intent intentFieldWorks = new Intent(this, FieldWorksCreatorActivity.class);
        startActivity(intentFieldWorks);
    }


    public void addSheet()
    {
        Intent intentSheet = new Intent(this, SheetCreatorActivity.class);
        startActivity(intentSheet);
    }


    public void addNote()
    {
        Intent intentNote = new Intent(this, NoteCreatorActivity.class);
        startActivity(intentNote);
    }


    public void showLayersProps()
    {
        LayerListDialog dialog = new LayerListDialog();
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_LAYER_LIST);
    }


    protected void showIndictments(boolean show)
    {
        if (null != mOnShowIndictmentsListener) {
            mOnShowIndictmentsListener.onShowIndictments(show);
        }
    }


    protected void showSheets(boolean show)
    {
        if (null != mOnShowSheetsListener) {
            mOnShowSheetsListener.onShowSheets(show);
        }
    }


    protected void showFieldWorks(boolean show)
    {
        if (null != mOnShowFieldWorksListener) {
            mOnShowFieldWorksListener.onShowFieldWorks(show);
        }
    }


    protected void showNotes(boolean show)
    {
        if (null != mOnShowNotesListener) {
            mOnShowNotesListener.onShowNotes(show);
        }
    }


    public void setOnShowIndictmentsListener(OnShowIndictmentsListener onShowIndictmentsListener)
    {
        mOnShowIndictmentsListener = onShowIndictmentsListener;
    }


    public void setOnShowSheetsListener(OnShowSheetsListener onShowSheetsListener)
    {
        mOnShowSheetsListener = onShowSheetsListener;
    }


    public void setOnShowFieldWorksListener(OnShowFieldWorksListener onShowFieldWorksListener)
    {
        mOnShowFieldWorksListener = onShowFieldWorksListener;
    }


    public void setOnShowNotesListener(OnShowNotesListener onShowNotesListener)
    {
        mOnShowNotesListener = onShowNotesListener;
    }


    @Override
    public void onDocLongClick(
            GeoGeometry geometry,
            boolean isPoint)
    {
        if (isPoint) {
            GeoMultiPoint point = (GeoMultiPoint) geometry;
            if (point.size() > 0) {
                setZoomAndCenter(16, point.get(0));
                showMap();
            }

        } else {
            updateMapTerritory(geometry);
            showMap();
        }
    }


    public interface OnShowIndictmentsListener
    {
        void onShowIndictments(boolean show);
    }


    public interface OnShowSheetsListener
    {
        void onShowSheets(boolean show);
    }


    public interface OnShowFieldWorksListener
    {
        void onShowFieldWorks(boolean show);
    }


    public interface OnShowNotesListener
    {
        void onShowNotes(boolean show);
    }


    public void setZoomAndCenter(
            float zoom,
            GeoPoint center)
    {
        MapFragment mapFragment = (MapFragment) mSectionsPagerAdapter.getItem(1);
        mapFragment.setZoomAndCenter(zoom, center);
    }


    public void updateMapTerritory(GeoGeometry geometry)
    {
        MapFragment mapFragment = (MapFragment) mSectionsPagerAdapter.getItem(1);
        mapFragment.updateTerritory(geometry);
    }


    public void showMap()
    {
        mViewPager.setCurrentItem(1, true);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter
            extends FragmentPagerAdapter
    {
        protected MapFragment mMapFragment;


        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public Fragment getItem(int position)
        {
            switch (position) {
                case 0:
                    DocumentsFragment documentsFragment = new DocumentsFragment();
                    setOnShowIndictmentsListener(documentsFragment);
                    setOnShowSheetsListener(documentsFragment);
                    setOnShowNotesListener(documentsFragment);
                    return documentsFragment;
                case 1:
                    if (null == mMapFragment) {
                        mMapFragment = new MapFragment();
                        mMapFragment.setInViewPager(true);
                    }
                    return mMapFragment;
                default:
                    return null;
            }
        }


        @Override
        public int getCount()
        {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_documents).toUpperCase(l);
                case 1:
                    return getString(R.string.title_map).toUpperCase(l);
            }
            return null;
        }
    }
}
