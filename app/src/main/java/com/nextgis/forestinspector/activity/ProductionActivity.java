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

import android.widget.BaseAdapter;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.CheckListAdapter;
import com.nextgis.forestinspector.adapter.ProductionListAdapter;
import com.nextgis.forestinspector.dialog.ProductionInputDialog;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;

/**
 * Created by bishop on 07.08.15.
 */
public class ProductionActivity extends CheckListActivity{


    @Override
    protected int getContentViewId() {
        return R.layout.activity_production;
    }

    protected void add(){
        //create input dialog
        ProductionInputDialog inputDialog = new ProductionInputDialog();
        inputDialog.show(getSupportFragmentManager(), "production_input_dialog");
    }

    @Override
    protected CheckListAdapter getAdapter() {
        return new ProductionListAdapter(this, mDocumentFeature);
    }

    protected void contentsChanged(){
        if(null != mAdapter)
            mAdapter.notifyDataSetChanged();
    }

    public void addProduction(String species, String cat, double length, double thickness, int count){

        //get production layer
        MapBase map = MapBase.getInstance();
        DocumentsLayer documentsLayer = (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if(null == documentsLayer)
            return;

        VectorLayer productionLayer = (VectorLayer) documentsLayer.getLayerByName(Constants.KEY_LAYER_PRODUCTION);
        if(null == productionLayer)
            return;

        Feature feature = new Feature(com.nextgis.maplib.util.Constants.NOT_FOUND, productionLayer.getFields());
        feature.setFieldValue(Constants.FIELD_PRODUCTION_SPECIES, species);
        feature.setFieldValue(Constants.FIELD_PRODUCTION_TYPE, cat);
        feature.setFieldValue(Constants.FIELD_PRODUCTION_LENGTH, length);
        feature.setFieldValue(Constants.FIELD_PRODUCTION_DIAMETER, thickness);
        feature.setFieldValue(Constants.FIELD_PRODUCTION_COUNT, count);
        mDocumentFeature.addSubFeature(Constants.KEY_LAYER_PRODUCTION, feature);
        contentsChanged();
    }
}
