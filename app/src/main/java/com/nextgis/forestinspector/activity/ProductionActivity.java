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
import android.view.View;
import android.widget.ListView;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.ProductionListAdapter;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;

/**
 * Created by bishop on 07.08.15.
 */
public class ProductionActivity extends FIActivity implements IDocumentFeatureSource{
    protected DocumentEditFeature mDocumentFeature;
    protected ProductionListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_production);
        setToolbar(R.id.main_toolbar);

        final View add = findViewById(R.id.add);
        if (null != add) {
            add.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            add();
                        }
                    });
        }

        MainApplication app = (MainApplication) getApplication();
        mDocumentFeature = app.getTempFeature();

        mAdapter = new ProductionListAdapter(this, mDocumentFeature);
        ListView list = (ListView) findViewById(R.id.productionList);
        list.setAdapter(mAdapter);
    }

    protected void add(){

    }

    @Override
    public DocumentFeature getFeature() {
        return mDocumentFeature;
    }
}
