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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.InitStepListAdapter;
import com.nextgis.forestinspector.fragment.LoginFragment;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;

import java.util.Locale;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener {

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
            // should never happen
            mFirsRun = true;
            createFirstStartView();
        }

        final Account account = app.getAccount(getString(R.string.account_name));
        if(account == null){
            mFirsRun = true;
            createFirstStartView();
        }
        else {
            MapBase map = app.getMap();
            if(map.getLayerCount() == 0)
            {
                mFirsRun = true;
                createSecondStartView(account);
            }
            else {
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
            ngwLoginFragment.setForNewAccount(true);
        }

        ngwLoginFragment.setOnAddAccountListener(this);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(com.nextgis.maplibui.R.id.login_frame, ngwLoginFragment, "NGWLogin");
        ft.commit();
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

        task.execute(

        );
    }

    protected void createNormalView(){
        setContentView(R.layout.activity_main);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.app_name));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!mFirsRun)
            getMenuInflater().inflate(R.menu.menu_main, menu);
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


    protected void refreshActivityView()
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onAddAccount(Account account, String token, boolean accountAdded) {
        if(accountAdded) {

            //free any map data here
            final MainApplication app = (MainApplication) getApplication();
            MapBase map = app.getMap();

            // delete all layers from map if any
            map.delete();

            // goto step 2
            refreshActivityView();

            /*
            //delete account on any problem


            // 2. create base map layers
            createBasicLayers(map);

            // 3. start downloading data
            FirstRunDialog newFragment = new FirstRunDialog();
            newFragment.setFirstRunOperations(this)
                       .setAccount(account)
                       .show(getSupportFragmentManager(), "first_run");
            newFragment.startInitializeProcess();
            */
        }
        else
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
    }

    /*public void onFirstRunOperationsFinished(boolean bSucceed, Account account){
        if(bSucceed)
            refreshActivityView();
        else{
            final MainApplication app = (MainApplication) getApplication();
            app.removeAccount(account);
        }
    }*/

    /*protected void createBasicLayers(MapBase map){

        //add OpenStreetMap layer on application first run
        String layerName = getString(R.string.osm);
        String layerURL = SettingsConstantsUI.OSM_URL;
        RemoteTMSLayerUI osmLayer =
                new RemoteTMSLayerUI(getApplicationContext(), map.createLayerStorage());
        osmLayer.setName(layerName);
        osmLayer.setURL(layerURL);
        osmLayer.setTMSType(GeoConstants.TMSTYPE_OSM);
        osmLayer.setMaxZoom(22);
        osmLayer.setMinZoom(12.4f);
        osmLayer.setVisible(true);

        map.addLayer(osmLayer);
        //mMap.moveLayer(0, osmLayer);

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


        /*
        // init vector layers
        // get layers by keys
        // 1. get inspector details (name, description, bbox, id) from "inspectors"


        NGWVectorLayerUI ngwVectorLayer =
                new NGWVectorLayerUI(getApplicationContext(), map.createLayerStorage());
        ngwVectorLayer.setName("GeoMixer violations vector");
        ngwVectorLayer.setRemoteId(34);
        ngwVectorLayer.setVisible(false);
        ngwVectorLayer.setAccountName(account.name);
        ngwVectorLayer.setMinZoom(0);
        ngwVectorLayer.setMaxZoom(100);

        map.addLayer(ngwVectorLayer);

        ngwVectorLayer.downloadAsync();
        *//*

        map.save();
    }*/

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
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
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
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Fragment newInstance(int sectionNumber) {
            if(sectionNumber == 0) {
                PlaceholderFragment fragment = new PlaceholderFragment();
                Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                fragment.setArguments(args);
                return fragment;
            }
            else{
                MapFragment fragment = new MapFragment();
                Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                fragment.setArguments(args);
                return fragment;
            }
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
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
            int counter = 0;
            while(true) {
                if(counter > 9)
                    counter = 0;

                mMessage = "Working...";
                publishProgress(counter, 1);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mMessage = "Done";
                publishProgress(counter, 2);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                counter++;

                if(isCancelled())
                    return false;
            }

            //return true;
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
                app.removeAccount(mAccount);
            }
            refreshActivityView();
        }
    }

}
