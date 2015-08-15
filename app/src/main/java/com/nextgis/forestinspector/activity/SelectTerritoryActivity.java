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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.IGISApplication;

/**
 * Created by bishop on 03.08.15.
 */
public class SelectTerritoryActivity extends FIActivity {
    protected DocumentEditFeature mDocumentFeature;
    protected EditText mTerritoryText;

    protected final int TERRITORY_ACTIVITY = 55;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_territory);
        setToolbar(R.id.main_toolbar);

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

        MainApplication app = (MainApplication) getApplication();
        mDocumentFeature = app.getTempFeature();

        mTerritoryText = (EditText) findViewById(R.id.territory);
    }

    private void addBySheet() {
        if(null == mDocumentFeature ||
                mDocumentFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) == 0){
            Toast.makeText(this, getString(R.string.error_sheet_is_empty), Toast.LENGTH_LONG).show();
        }
        mDocumentFeature.setUnionGeometryFromLayer(Constants.KEY_LAYER_SHEET);
    }

    private void addByWalk() {
        // TODO: 03.08.15 Digitize by walking
    }

    private void addByParcelList() {
        Intent intent = new Intent(this, SelectParcelsActivity.class);
        startActivityForResult(intent, TERRITORY_ACTIVITY);
    }

    private void addByHand() {
        // TODO: 03.08.15 Draw on map by hand
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == TERRITORY_ACTIVITY){
            // TODO: 04.08.15 Update map
            String text = mDocumentFeature.getTerritoryText(getString(R.string.forestry),
                    getString(R.string.district_forestry), getString(R.string.parcel),
                    getString(R.string.unit));

            mTerritoryText.setText(text);
            mDocumentFeature.setFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY, text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.territory_edit, menu);
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
            showSettings();
            return true;
        }
        else if (id == R.id.action_apply) {
            apply();
            return true;
        }
        else if (id == R.id.action_cancel) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void apply() {
        finish();
    }

    @Override
    protected void onPause() {
        mDocumentFeature.setFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY, mTerritoryText.getText().toString());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTerritoryText.setText(mDocumentFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));
    }

    private void showSettings() {
        IGISApplication app = (IGISApplication) getApplication();
        app.showSettings();
    }
}
