/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2016. NextGIS, info@nextgis.com
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

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.dialog.LayerListDialog;
import com.nextgis.forestinspector.fragment.MapFragment;
import com.nextgis.forestinspector.util.Constants;


public class MapActivity
        extends FIActivity
{
    public static final String PARAM_GEOMETRY     = "param_geometry";
    public static final String LOCATION_LATITUDE  = "location_latitude";
    public static final String LOCATION_LONGITUDE = "location_longitude";

    protected MainApplication mApp;
    protected MapFragment     mMapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setToolbar(R.id.main_toolbar);

        mApp = (MainApplication) getApplication();

        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        mMapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);
        if (mMapFragment == null) {
            mMapFragment = new MapFragment();
        }

        ft.replace(R.id.map_fragment, mMapFragment, Constants.FRAGMENT_MAP).commit();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mMapFragment.setSelectedLocationVisible(true);
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mMapFragment.setSelectedLocationVisible(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.layers_props:
                showLayersProps();
                return true;
            case R.id.action_ok:
                returnLocation();
                return true;
            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void showLayersProps()
    {
        LayerListDialog dialog = new LayerListDialog();
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_LAYER_LIST);
    }


    protected void returnLocation()
    {
        Location location = mMapFragment.getSelectedLocation();

        if (location == null) {
            Toast.makeText(this, R.string.error_no_location, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(LOCATION_LATITUDE, location.getLatitude());
        intent.putExtra(LOCATION_LONGITUDE, location.getLongitude());
        setResult(RESULT_OK, intent);

        finish();
    }
}
