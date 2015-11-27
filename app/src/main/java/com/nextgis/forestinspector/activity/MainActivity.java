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
import android.content.ContentResolver;
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
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.InitStepListAdapter;
import com.nextgis.forestinspector.fragment.DocumentsFragment;
import com.nextgis.forestinspector.fragment.LoginFragment;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.forestinspector.service.InitService;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplibui.fragment.NGWLoginFragment;

import java.util.Locale;

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
    protected BroadcastReceiver mSyncStatusReceiver;

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

    protected void createSecondStartView(final Account account){
        setContentView(R.layout.activity_main_second);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.initialization));

        mAdapter = new InitStepListAdapter(this);

        ListView list = (ListView) findViewById(R.id.stepsList);
        list.setAdapter(mAdapter);

        mSyncStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int step = intent.getIntExtra(Constants.KEY_STEP, 0);
                int state = intent.getIntExtra(Constants.KEY_STATE, 0);
                String message = intent.getStringExtra(Constants.KEY_MESSAGE);
                if(state == Constants.STEP_STATE_ERROR){

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //delete map
                    final MainApplication app = (MainApplication) getApplication();
                    String accName = account.name;
                    app.removeAccount(account);

                    for(int i = 0; i < 10; i++){
                        if(app.getAccount(accName) == null)
                            break;
                    }
                    refreshActivityView();
                }
                else if (step >= mAdapter.getCount())
                    refreshActivityView();
                else
                    mAdapter.setMessage(step, state, message);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_MESSAGE);
        registerReceiver(mSyncStatusReceiver, intentFilter);

        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent syncIntent = new Intent(MainActivity.this, InitService.class);
                syncIntent.setAction(InitService.ACTION_STOP);
                startService(syncIntent);
            }
        });

        Intent syncIntent = new Intent(MainActivity.this, InitService.class);
        syncIntent.setAction(InitService.ACTION_START);
        startService(syncIntent);
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
                            addNote();
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
    protected void onPause() {
        super.onPause();

        if(null != mSyncStatusReceiver)
            unregisterReceiver(mSyncStatusReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(null != mSyncStatusReceiver) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_MESSAGE);
            registerReceiver(mSyncStatusReceiver, intentFilter);
        }
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

    public void addIndictment() {
        Intent intentIndictment = new Intent(this, IndictmentCreatorActivity.class);
        startActivity(intentIndictment);
    }

    public void addSheet() {
        Intent intentSheet = new Intent(this, SheetCreatorActivity.class);
        startActivity(intentSheet);
    }

    public void addNote() {

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
}
