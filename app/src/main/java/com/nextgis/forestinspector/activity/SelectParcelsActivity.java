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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.ParcelCursorAdapter;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;


public class SelectParcelsActivity
        extends FIActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    protected ListView            mListView;
    protected ParcelCursorAdapter mAdapter;
    protected DocumentEditFeature mDocumentFeature;
    protected boolean mFiltered     = false;
    protected boolean mLoaderIsInit = false;

    protected static String KEY_QUERY = "query";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication) getApplication();
        mDocumentFeature = app.getTempFeature();

        setContentView(R.layout.activity_select_territory_form_cadastre);
        setToolbar(R.id.main_toolbar);

        mListView = (ListView) findViewById(R.id.parcelsList);
        mAdapter = new ParcelCursorAdapter(this, null, 0, mDocumentFeature);
        mListView.setAdapter(mAdapter);

        handleIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        handleIntent(intent);
    }


    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data
            String fullQuery = Constants.FIELD_CADASTRE_LV + " LIKE '%" + query + "%' OR " +
                               Constants.FIELD_CADASTRE_ULV + " LIKE '%" + query + "%' OR " +
                               Constants.FIELD_CADASTRE_PARCEL + " LIKE '%" + query + "%'";

            Bundle bundle = new Bundle();
            bundle.putString(KEY_QUERY, fullQuery);
            if (mLoaderIsInit) {
                getSupportLoaderManager().restartLoader(0, bundle, this);
            } else {
                getSupportLoaderManager().initLoader(0, bundle, this);
                mLoaderIsInit = true;
            }
        } else {
            if (mLoaderIsInit) {
                getSupportLoaderManager().restartLoader(0, null, this);
            } else {
                getSupportLoaderManager().initLoader(0, null, this);
                mLoaderIsInit = true;
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args)
    {
        Uri uri = Uri.parse(
                "content://" + SettingsConstants.AUTHORITY + "/" + Constants.KEY_LAYER_KV);
        if (args == null) {
            return new CursorLoader(this, uri, null, null, null, null);
        } else {
            return new CursorLoader(this, uri, null, " " + args.getString(KEY_QUERY), null, null);
        }
    }


    @Override
    public void onLoadFinished(
            Loader<Cursor> loader,
            Cursor data)
    {
        if (null != data) {
            Cursor oldCursor = mAdapter.swapCursor(data);
            if (null != oldCursor) {
                oldCursor.close();
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parcels_select, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        if (null != item) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ParcelSearchView searchView = (ParcelSearchView) MenuItemCompat.getActionView(item);
            if (null == searchView) {
                searchView = new ParcelSearchView(getSupportActionBar().getThemedContext());
                MenuItemCompat.setActionView(item, searchView);
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            if (!mFiltered) {
                String fullQuery = mDocumentFeature.getWhereClauseForParcelIds();
                Bundle bundle = new Bundle();
                bundle.putString(KEY_QUERY, fullQuery);
                getSupportLoaderManager().restartLoader(0, bundle, this);
            } else {
                getSupportLoaderManager().restartLoader(0, null, this);
            }
            mFiltered = !mFiltered;

            return true;
        } else if (id == R.id.action_apply) {
            apply();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void apply()
    {
        finish();
    }


    public class ParcelSearchView
            extends SearchView
    {

        public ParcelSearchView(Context context)
        {
            super(context);
            init();
        }


        public ParcelSearchView(
                Context context,
                AttributeSet attrs)
        {
            super(context, attrs);
            init();
        }


        public ParcelSearchView(
                Context context,
                AttributeSet attrs,
                int defStyleAttr)
        {
            super(context, attrs, defStyleAttr);
            init();
        }


        private void init()
        {
            ImageView closeButton =
                    (ImageView) findViewById(android.support.v7.appcompat.R.id.search_close_btn);
            closeButton.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            getSupportLoaderManager().restartLoader(
                                    0, null, SelectParcelsActivity.this);
                        }
                    });
        }


        @Override
        public void onActionViewCollapsed()
        {
            super.onActionViewCollapsed();
            getSupportLoaderManager().restartLoader(0, null, SelectParcelsActivity.this);
        }
    }
}
