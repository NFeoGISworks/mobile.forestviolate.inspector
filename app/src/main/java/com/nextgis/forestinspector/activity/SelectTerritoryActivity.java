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
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.IGISApplication;

/**
 * Created by bishop on 03.08.15.
 */
public class SelectTerritoryActivity extends FIActivity {
    protected DocumentFeature mDocumentFeature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_territory);
        setToolbar(R.id.main_toolbar);

        final View addByType = findViewById(R.id.add_by_type);
        if (null != addByType) {
            addByType.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addByType();
                        }
                    });
        }

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
    }

    private void addBySheet() {
        if(null == mDocumentFeature ||
                mDocumentFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) == 0){
            Toast.makeText(this, getString(R.string.error_sheet_is_empty), Toast.LENGTH_LONG).show();
        }
        // TODO: 03.08.15 Create convex hull on trees from sheet
    }

    private void addByWalk() {
        // TODO: 03.08.15 Digitize by walking
    }

    private void addByParcelList() {
        // TODO: 03.08.15 Show dialog with parcels list with checkboxes
        Intent intent = new Intent(this, SelectTerritoryFromCadastreActivity.class);
        startActivity(intent);
    }

    private void addByHand() {
        // TODO: 03.08.15 Draw on map by hand
    }

    private void addByType() {
        // TODO: 03.08.15 Type text. The connected territory list must set empty.
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
        // TODO: 03.08.15 Add new geometry to feature, add parcels to territory
        finish();
    }

    private void showSettings() {
        IGISApplication app = (IGISApplication) getApplication();
        app.showSettings();
    }
}
