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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.dialog.InputParcelTextDialog;
import com.nextgis.forestinspector.dialog.LayerListDialog;
import com.nextgis.forestinspector.fragment.MapEditFragment;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplibui.fragment.BottomToolbar;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class SelectTerritoryActivity
        extends FIActivity
{
    protected static final int TERRITORY_ACTIVITY = 55;

    protected DocumentEditFeature mEditFeature;
    protected TextView            mTerritoryText;
    protected View                mMainButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (null != extras && extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);

            MainApplication app = (MainApplication) getApplication();
            mEditFeature = app.getEditFeature(featureId);


            setContentView(R.layout.activity_select_territory);
            setToolbar(R.id.main_toolbar);

            mMainButton = findViewById(R.id.multiple_actions);

            final View addByHand = findViewById(R.id.add_by_hand);
            if (null != addByHand) {
                addByHand.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                addByHand();
                            }
                        });
            }

            final View addByParcelList = findViewById(R.id.add_by_parcel_list);
            if (null != addByParcelList) {
                addByParcelList.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                addByParcelList();
                            }
                        });
            }

            final View addBySheet = findViewById(R.id.add_by_sheet);
            if (null != addBySheet) {
                addBySheet.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                addBySheet();
                            }
                        });
            }

            final View addByWalk = findViewById(R.id.add_by_walk);
            if (null != addByWalk) {
                addByWalk.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                addByWalk();
                            }
                        });
            }

            MapEditFragment mapFragment =
                    (MapEditFragment) getSupportFragmentManager().findFragmentById(
                            R.id.map_fragment);
            if (null != mapFragment) { mapFragment.updateTerritory(mEditFeature.getGeometry()); }

            mTerritoryText = (TextView) findViewById(R.id.territory);

            getBottomToolbar().setVisibility(View.GONE);
        }
    }


    private void addBySheet()
    {
        if (null == mEditFeature
                || mEditFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) == 0) {

            Toast.makeText(this, getString(R.string.error_sheet_is_empty), Toast.LENGTH_LONG)
                    .show();
        }
        mEditFeature.setUnionGeometryFromLayer(Constants.KEY_LAYER_SHEET);

        MapEditFragment mapFragment =
                (MapEditFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (null != mapFragment) { mapFragment.updateTerritory(mEditFeature.getGeometry()); }
    }


    private void addByWalk()
    {
        // Digitize by walking
        MapEditFragment mapFragment =
                (MapEditFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (null != mapFragment) {
            mapFragment.addByWalk();
        }
    }


    private void addByParcelList()
    {
        Intent intent = new Intent(this, SelectParcelsActivity.class);
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        startActivityForResult(intent, TERRITORY_ACTIVITY);
    }


    private void addByHand()
    {
        //Draw on map by hand
        MapEditFragment mapFragment =
                (MapEditFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (null != mapFragment) {
            mapFragment.addByHand();
        }
    }


    public BottomToolbar getBottomToolbar()
    {
        return (BottomToolbar) findViewById(R.id.bottom_toolbar);
    }


    public View getFAB()
    {
        return mMainButton;
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == TERRITORY_ACTIVITY) {
            String text = mEditFeature.getTerritoryText(
                    getString(R.string.forestry), getString(R.string.district_forestry),
                    getString(R.string.parcel), getString(R.string.unit));

            setTerritoryText(text);

            MapEditFragment mapFragment =
                    (MapEditFragment) getSupportFragmentManager().findFragmentById(
                            R.id.map_fragment);
            if (null != mapFragment) { mapFragment.updateTerritory(mEditFeature.getGeometry()); }
        }
    }


    public void setTerritoryText(String text)
    {
        mTerritoryText.setText(text);
        mEditFeature.setFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY, text);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.territory_edit, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        switch (item.getItemId()) {
            case R.id.layers_props:
                showLayersProps();
                return true;

            case R.id.action_apply:
                apply();
                return true;

            case R.id.action_cancel:
                apply();
                return true;

            case R.id.action_settings:
                showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed()
    {
        apply();
        super.onBackPressed();
    }


    public void showLayersProps()
    {
        LayerListDialog dialog = new LayerListDialog();
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_LAYER_LIST);
    }


    private void apply()
    {
        MapEditFragment mapFragment =
                (MapEditFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (null != mapFragment) { mapFragment.onFinishEditSession(); }
        finish();
    }


    @Override
    protected void onPause()
    {
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_TERRITORY, mTerritoryText.getText().toString());
        super.onPause();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        mTerritoryText.setText(getTerritoryText());
    }


    private void showSettings()
    {
        IGISApplication app = (IGISApplication) getApplication();
        app.showSettings(SettingsConstantsUI.ACTION_PREFS_GENERAL);
    }


    public void showAskParcelTextDialog()
    {
        InputParcelTextDialog newFragment = new InputParcelTextDialog();
        newFragment.show(getSupportFragmentManager(), "input_parcel_text");
    }


    public void clearTerritoryGeometry()
    {
        mEditFeature.setGeometry(null);
    }


    public void setTerritoryTextByGeom()
    {
        String text = mEditFeature.getTerritoryTextByGeom(
                getString(R.string.forestry), getString(R.string.district_forestry),
                getString(R.string.parcel), getString(R.string.unit));
        setTerritoryText(text);
    }


    public String getTerritoryText()
    {
        return mEditFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY);
    }
}
