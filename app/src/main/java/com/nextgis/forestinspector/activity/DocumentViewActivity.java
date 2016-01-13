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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.fragment.FieldWorksViewFragment;
import com.nextgis.forestinspector.fragment.IndictmentViewFragment;
import com.nextgis.forestinspector.fragment.MapViewFragment;
import com.nextgis.forestinspector.fragment.PhotoTableFragment;
import com.nextgis.forestinspector.fragment.ProductionListViewerFragment;
import com.nextgis.forestinspector.fragment.SheetListViewerFragment;
import com.nextgis.forestinspector.fragment.SheetViewFragment;
import com.nextgis.forestinspector.fragment.TabFragment;
import com.nextgis.forestinspector.fragment.VehicleViewFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.util.AttachItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The tabbed view with document contents. The tab list consist of document type.
 */
public class DocumentViewActivity extends FIActivity implements  IDocumentFeatureSource{
    protected ViewPager            mViewPager;
    protected SectionsPagerAdapter mSectionsPagerAdapter;
    protected DocumentFeature      mDocFeature;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // get document from id
        DocumentsLayer docs = DocumentEditFeature.getDocumentsLayer();
        if (null == docs) {
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            return;
        }

        Bundle b = getIntent().getExtras();
        long id = b.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
        mDocFeature = docs.getFeatureWithAttaches(id);
        if (null == mDocFeature) {
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            return;
        }

        int nType;
        switch (mDocFeature.getFieldValueAsInteger(Constants.FIELD_DOCUMENTS_TYPE)) {
            case Constants.DOC_TYPE_INDICTMENT:
                nType = Constants.DOC_TYPE_INDICTMENT;
                setTitle(getString(R.string.indictment));
                break;
            case Constants.DOC_TYPE_SHEET:
                nType = Constants.DOC_TYPE_SHEET;
                setTitle(getString(R.string.sheet_title));
                break;
            case Constants.DOC_TYPE_FIELD_WORKS:
                nType = Constants.DOC_TYPE_FIELD_WORKS;
                setTitle(getString(R.string.field_works_title));
                break;
            case Constants.DOC_TYPE_VEHICLE: // no separate document type
            default:
                setContentView(R.layout.activity_document_noview);
                setToolbar(R.id.main_toolbar);
                return;
        }

        setContentView(R.layout.activity_document_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        String sNum = mDocFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_NUMBER);
        Date date = (Date) mDocFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE);
        String sDate = DateFormat.getDateInstance().format(date);
        toolbar.setSubtitle(sNum + " " + getString(R.string.on) + " " + sDate);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), nType, docs);

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
    public DocumentFeature getFeature() {
        return mDocFeature;
    }

    protected class SectionsPagerAdapter extends FragmentPagerAdapter {

        protected List<TabFragment> mTabFragmentList;

        public SectionsPagerAdapter(FragmentManager fm, int nType, DocumentsLayer docs) {
            super(fm);

            mTabFragmentList = new ArrayList<>();

            switch (nType) {
                case Constants.DOC_TYPE_INDICTMENT: {
                    // indictment
                    IndictmentViewFragment indictmentViewFragment = new IndictmentViewFragment();
                    indictmentViewFragment.setName(getString(R.string.indictment_tab_name));
                    mTabFragmentList.add(indictmentViewFragment);

                    // sheet
                    if (mDocFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) > 0) {
                        SheetListViewerFragment sheetListViewerFragment =
                                new SheetListViewerFragment();
                        sheetListViewerFragment.setName(getString(R.string.sheet_tab_name));
                        mTabFragmentList.add(sheetListViewerFragment);
                    }

                    // production
                    if (mDocFeature.getSubFeaturesCount(Constants.KEY_LAYER_PRODUCTION) > 0) {
                        ProductionListViewerFragment productionListViewerFragment =
                                new ProductionListViewerFragment();
                        productionListViewerFragment.setName(
                                getString(R.string.production_tab_name));
                        mTabFragmentList.add(productionListViewerFragment);
                    }

                    // vehicle
                    if (mDocFeature.getSubFeaturesCount(Constants.KEY_LAYER_VEHICLES) > 0) {
                        VehicleViewFragment vehicleViewFragment = new VehicleViewFragment();
                        vehicleViewFragment.setName(getString(R.string.vehicle_tab_name));
                        mTabFragmentList.add(vehicleViewFragment);
                    }

                    // photo table
                    if (mDocFeature.getAttachments() != null && mDocFeature.getAttachments().size() > 0) {
                        // filter of a signature
                        int photosCount = 0;
                        for (Map.Entry<String, AttachItem> entry : mDocFeature.getAttachments()
                                .entrySet()) {
                            AttachItem attachItem = entry.getValue();
                            if (attachItem.getDisplayName().equals(Constants.SIGN_FILENAME)) {
                                continue;
                            }
                            ++photosCount;
                        }

                        if (photosCount > 0) {
                            PhotoTableFragment photoTableFragment = new PhotoTableFragment();
                            photoTableFragment.setName(getString(R.string.photo_table_tab_name));
                            photoTableFragment.setIsPhotoTableViewer(true);
                            mTabFragmentList.add(photoTableFragment);
                        }
                    }

                    break;
                }

                case Constants.DOC_TYPE_SHEET: {
                    SheetViewFragment sheetViewFragment = new SheetViewFragment();
                    sheetViewFragment.setName(getString(R.string.sheet_head));
                    mTabFragmentList.add(sheetViewFragment);

                    // sheet
                    if (mDocFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) > 0) {
                        SheetListViewerFragment sheetListViewerFragment =
                                new SheetListViewerFragment();
                        sheetListViewerFragment.setName(getString(R.string.sheet_tab_name));
                        mTabFragmentList.add(sheetListViewerFragment);
                    }

                    break;
                }

                case Constants.DOC_TYPE_FIELD_WORKS: {
                    // indictment
                    FieldWorksViewFragment fieldWorksViewFragment = new FieldWorksViewFragment();
                    fieldWorksViewFragment.setName(getString(R.string.field_works_tab_name));
                    mTabFragmentList.add(fieldWorksViewFragment);

                    // photo table
                    if (mDocFeature.getAttachments() != null && mDocFeature.getAttachments().size() > 0) {
                        // filter of a signature
                        int photosCount = 0;
                        for (Map.Entry<String, AttachItem> entry : mDocFeature.getAttachments()
                                .entrySet()) {
                            AttachItem attachItem = entry.getValue();
                            if (attachItem.getDisplayName().equals(Constants.SIGN_FILENAME)) {
                                continue;
                            }
                            ++photosCount;
                        }

                        if (photosCount > 0) {
                            PhotoTableFragment photoTableFragment = new PhotoTableFragment();
                            photoTableFragment.setName(getString(R.string.photo_table_tab_name));
                            photoTableFragment.setIsPhotoTableViewer(true);
                            mTabFragmentList.add(photoTableFragment);
                        }
                    }

                    break;
                }
            }

            MapViewFragment mapViewFragment = new MapViewFragment();
            mapViewFragment.setName(getString(R.string.title_map));
            mTabFragmentList.add(mapViewFragment);
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
