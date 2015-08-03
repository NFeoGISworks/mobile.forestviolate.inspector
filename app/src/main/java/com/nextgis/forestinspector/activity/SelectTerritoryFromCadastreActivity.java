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
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;

/**
 * Created by bishop on 03.08.15.
 */
public class SelectTerritoryFromCadastreActivity extends FIActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    protected ListView mListView;
    protected ParcelCursorAdapter mAdapter;
    protected DocumentEditFeature mDocumentFeature;
    protected boolean mFiltered = false;
    protected boolean mLoaderIsInit = false;

    protected String KEY_QUERY = "query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_select_territory_form_cadastre);
        setToolbar(R.id.main_toolbar);

        mListView = (ListView)findViewById(R.id.parcelsList);
        mAdapter = new ParcelCursorAdapter(this, null, 0);
        mListView.setAdapter(mAdapter);

        handleIntent(getIntent());

        MainApplication app = (MainApplication) getApplication();
        mDocumentFeature = app.getTempFeature();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data
            String fullQuery = Constants.FIELD_CADASTRE_LV + " LIKE '%" + query + "%' OR " +
                    Constants.FIELD_CADASTRE_ULV + " LIKE '%" + query + "%' OR " +
                    Constants.FIELD_CADASTRE_PARCEL + " LIKE '%" + query + "%'";

            Bundle bundle = new Bundle();
            bundle.putString(KEY_QUERY, fullQuery);
            if(mLoaderIsInit) {
                getSupportLoaderManager().restartLoader(0, bundle, this);
            }
            else {
                getSupportLoaderManager().initLoader(0, bundle, this);
                mLoaderIsInit = true;
            }
        }
        else{
            if(mLoaderIsInit) {
                getSupportLoaderManager().restartLoader(0, null, this);
            }
            else {
                getSupportLoaderManager().initLoader(0, null, this);
                mLoaderIsInit = true;
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(
                "content://" + SettingsConstants.AUTHORITY + "/" + Constants.KEY_LAYER_CADASTRE);
        if(args == null) {
            return new CursorLoader(this, uri, null, null, null, null);
        }
        else {
            return new CursorLoader(this, uri, null, " " + args.getString(KEY_QUERY), null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(null != data)
            mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cadastre_select, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        if(null != item) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            if(null == searchView) {
                searchView = new SearchView(getSupportActionBar().getThemedContext());
                item = MenuItemCompat.setActionView(item, searchView);
            }
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    getSupportLoaderManager().restartLoader(0, null, SelectTerritoryFromCadastreActivity.this);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_filter){
            if(!mFiltered){
                String fullQuery = "";
                for(Long fid : mDocumentFeature.getCadastreIds()){
                    if(!TextUtils.isEmpty(fullQuery))
                        fullQuery += " OR ";
                    fullQuery += com.nextgis.maplib.util.Constants.FIELD_ID + " = " + fid;
                }
                Bundle bundle = new Bundle();
                bundle.putString(KEY_QUERY, fullQuery);
                getSupportLoaderManager().restartLoader(0, bundle, this);
            }
            else{
                getSupportLoaderManager().restartLoader(0, null, this);
            }
            mFiltered = !mFiltered;

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

        protected class ParcelCursorAdapter extends CursorAdapter {
        public ParcelCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.row_parcel, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvParcel = (TextView) view.findViewById(R.id.parcel_desc);

            String sParcelDesc = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV)) +
                    " " + getString(R.string.forestry) + ", " +
                    cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV)) +
                    " " + getString(R.string.district_forestry) + ", " + getString(R.string.parcel) + " " +
                    cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_PARCEL));
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(com.nextgis.maplib.util.Constants.FIELD_ID));
            // Populate fields with extracted properties
            tvParcel.setText(sParcelDesc);

            CheckBox box = (CheckBox) view.findViewById(R.id.check);
            box.setTag(id);
            if(mDocumentFeature.getCadastreIds().contains(id))
                box.setChecked(true);
            else
                box.setChecked(false);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Long id = (Long) buttonView.getTag();
                    if(isChecked) {
                        if(!mDocumentFeature.getCadastreIds().contains(id))
                            mDocumentFeature.getCadastreIds().add(id);
                    }
                    else
                        mDocumentFeature.getCadastreIds().remove(id);
                }
            });
        }
    }
}
