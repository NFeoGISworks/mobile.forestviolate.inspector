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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.fragment.IndictmentViewFragment;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.forestinspector.fragment.MapViewFragment;
import com.nextgis.forestinspector.fragment.SheetViewFragment;
import com.nextgis.forestinspector.fragment.TabFragment;
import com.nextgis.forestinspector.fragment.VehicleViewFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The tabbed view with document contents. The tab list consist of document type.
 */
public class DocumentViewActivity extends FIActivity{
    protected ViewPager mViewPager;
    protected SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get document from id
        MapBase map = MapBase.getInstance();
        DocumentsLayer docs = null;
        for(int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docs = (DocumentsLayer) layer;
                break;
            }
        }

        if(null == docs){
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            return;
        }

        Bundle b = getIntent().getExtras();
        long id = b.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
        DocumentFeature feature = docs.getFeature(id);
        if(null == feature){
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            return;
        }

        int nType;
        switch (feature.getFieldValueAsInteger(Constants.FIELD_DOCUMENTS_TYPE)){
            case Constants.TYPE_DOCUMENT:
                nType = Constants.TYPE_DOCUMENT;
                setTitle(getString(R.string.indictment));
                break;
            case Constants.TYPE_SHEET:
                nType = Constants.TYPE_SHEET;
                setTitle(getString(R.string.sheet));
                break;
            case Constants.TYPE_VEHICLE: // no separate document type
            default:
                setContentView(R.layout.activity_document_noview);
                setToolbar(R.id.main_toolbar);
                return;
        }

        setContentView(R.layout.activity_document_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        String sNum = feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_NUMBER);
        Date date = (Date) feature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE);
        String sDate = DateFormat.getDateInstance().format(date);
        toolbar.setSubtitle(sNum + " " + getString(R.string.on) + " " + sDate);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), nType, docs.getName(),
                feature);

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

    protected class SectionsPagerAdapter extends FragmentPagerAdapter {

        protected List<TabFragment> mTabFragmentList;

        public SectionsPagerAdapter(FragmentManager fm, int nType, String docsLayerName, DocumentFeature feature) {
            super(fm);

            mTabFragmentList = new ArrayList<>();

            if(nType == Constants.TYPE_DOCUMENT) {
                // indictment
                mTabFragmentList.add(new IndictmentViewFragment(getString(R.string.indictment_tab_name),
                        feature));
                // sheet
                if (feature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) > 0)
                    mTabFragmentList.add(new SheetViewFragment(getString(R.string.sheet_tab_name),
                            feature));
                // vehicle
                if (feature.getSubFeaturesCount(Constants.KEY_LAYER_DOCUMENTS) > 0)
                    mTabFragmentList.add(new VehicleViewFragment(getString(R.string.vehicle_tab_name),
                            feature));
                // photo table
                if (feature.getAttachments() != null && feature.getAttachments().size() > 0) {
                    // TODO: 28.07.15 create photo table
                }
            }
            else if(nType == Constants.TYPE_SHEET){
                mTabFragmentList.add(new SheetViewFragment(getString(R.string.sheet_tab_name),
                        feature));
            }

            mTabFragmentList.add(new MapViewFragment(getString(R.string.title_map), feature));
        }

        @Override
        public Fragment getItem(int position) {
            return mTabFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mTabFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TabFragment fragment = mTabFragmentList.get(position);
            return fragment.getName();
        }
    }
}
