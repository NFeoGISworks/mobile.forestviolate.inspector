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

package com.nextgis.forestinspector.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.DaveKoelle.AlphanumComparator;
import com.justsimpleinfo.Table.Table;
import com.justsimpleinfo.Table.TableData;
import com.justsimpleinfo.Table.TableRowData;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static com.nextgis.maplib.util.Constants.TAG;


public class SheetTableFillerFragment
        extends Fragment
{
    protected final static int CREATE_TABLE_DONE   = 0;
    protected final static int CREATE_TABLE_OK     = 1;
    protected final static int CREATE_TABLE_FAILED = 2;

    protected Table mTable;

    protected AppCompatSpinner mHeightView;
    protected AppCompatSpinner mCategoryView;
    protected EditText         mUnitView;
    protected TextView         mTableWarning;
    protected LinearLayout     mTableLayout;

    protected ArrayAdapter<String> mHeightAdapter;
    protected ArrayAdapter<String> mCategoryAdapter;

    protected OnAddTreeStubsListener mOnAddTreeStubsListener;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer docs = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docs = (DocumentsLayer) layer;
                break;
            }
        }

        if (null != docs) {
            mHeightAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_HEIGHT_TYPES, true);
            mCategoryAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_TREES_TYPES, false);
        }
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sheet_table_filler, null);
        mTableLayout = (LinearLayout) view.findViewById(R.id.table_layout);
        mTableWarning = (TextView) view.findViewById(R.id.table_warning);

        mHeightView = (AppCompatSpinner) view.findViewById(R.id.height);
        mHeightView.setAdapter(mHeightAdapter);

        mCategoryView = (AppCompatSpinner) view.findViewById(R.id.category);
        mCategoryView.setAdapter(mCategoryAdapter);

        mUnitView = (EditText) view.findViewById(R.id.unit);

        if (null != mTable) {
            mTableWarning.setVisibility(View.GONE);
            mTableLayout.addView(mTable);
            return view;
        }

        final Handler handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case CREATE_TABLE_DONE:
                        break;

                    case CREATE_TABLE_OK:
                        mTable = (Table) msg.obj;
                        mTableWarning.setVisibility(View.GONE);
                        mTableLayout.addView(mTable);
                        break;

                    case CREATE_TABLE_FAILED:
                        Toast.makeText(
                                getActivity(),
                                "SheetTableFillerFragment create table ERROR: " + msg.obj,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        RunnableFuture<Table> future = new FutureTask<Table>(
                new Callable<Table>()
                {
                    @Override
                    public Table call()
                            throws Exception
                    {
                        return new Table(getActivity());
                    }
                })
        {
            @Override
            protected void done()
            {
                super.done();
                handler.sendEmptyMessage(CREATE_TABLE_DONE);
            }


            @Override
            protected void set(Table result)
            {
                super.set(result);
                Message msg = handler.obtainMessage(CREATE_TABLE_OK, result);
                msg.sendToTarget();
            }


            @Override
            protected void setException(Throwable t)
            {
                super.setException(t);

                String error = t.getLocalizedMessage();
                Log.d(TAG, error);
                t.printStackTrace();

                Message msg = handler.obtainMessage(CREATE_TABLE_FAILED, error);
                msg.sendToTarget();
            }
        };

        new Thread(future).start();

        return view;
    }


    @Override
    public void onDestroyView()
    {
        mTableLayout.removeView(mTable);
        super.onDestroyView();
    }


    protected ArrayAdapter<String> getArrayAdapter(
            DocumentsLayer docsLayer,
            String layerKey,
            boolean numberSort)
    {
        NGWLookupTable table = (NGWLookupTable) docsLayer.getLayerByName(layerKey);

        if (null != table) {
            Map<String, String> data = table.getData();
            List<String> dataArray = new ArrayList<>();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                dataArray.add(entry.getKey());
            }

            if (numberSort) {
                Collections.sort(dataArray, new AlphanumComparator());
            } else {
                Collections.sort(dataArray);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.simple_spinner_item, dataArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            return adapter;
        }

        return null;
    }


    public void saveTableData()
    {
        MainApplication app = (MainApplication) getActivity().getApplication();
        DocumentsLayer docLayer = app.getDocsLayer();
        if (null == docLayer) {
            return;
        }
        VectorLayer subDocLayer = (VectorLayer) docLayer.getLayerByName(Constants.KEY_LAYER_SHEET);
        if (null == subDocLayer) {
            return;
        }
        DocumentFeature docFeature = getDocumentFeature();

        String height = mHeightView.getSelectedItem().toString();
        String category = mCategoryView.getSelectedItem().toString();
        String unit = mUnitView.getText().toString(); // TODO: empty?
        TableData tableData = mTable.getTableData();

//        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
        GeoPoint pt = new GeoPoint(0, 0); // TODO: mLocation
        pt.setCRS(GeoConstants.CRS_WGS84);
        pt.project(GeoConstants.CRS_WEB_MERCATOR);
        GeoMultiPoint geometryValue = new GeoMultiPoint();
        geometryValue.add(pt);

        int rowCount = tableData.size();
        boolean isDataAdded = false;

        for (int i = 0; i < rowCount; ++i) {
            TableRowData rowData = tableData.get(i);
            int columnCount = rowData.size();

            for (int j = 1; j < columnCount; ++j) {
                Integer treeCount = (Integer) rowData.get(j);

                if (treeCount > 0) {
                    TreeStub treeStub = new TreeStub();
                    treeStub.mHeight = height;
                    treeStub.mCategory = category;
                    treeStub.mUnit = unit;
                    treeStub.mSpecies = mTable.getSpecies().get(j - 1);
                    treeStub.mThickness = (Integer) rowData.get(0);
                    treeStub.mCount = treeCount;

                    Feature subFeature = docLayer.getNewTempSubFeature(docFeature, subDocLayer);
                    subFeature.setGeometry(geometryValue);
                    setFeatureFieldsValues(subFeature, treeStub);
                    subDocLayer.updateFeatureWithFlags(subFeature);
                    isDataAdded = true;
                }
            }
        }

        if (isDataAdded && null != mOnAddTreeStubsListener) {
            mOnAddTreeStubsListener.onAddTreeStubs();
        }
    }


    protected DocumentFeature getDocumentFeature()
    {
        Bundle extras = getActivity().getIntent().getExtras();
        if (null != extras && extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
            MainApplication app = (MainApplication) getActivity().getApplication();
            return app.getEditFeature(featureId);
        }
        return null;
    }


    protected void setFeatureFieldsValues(
            Feature feature,
            TreeStub treeStub)
    {
        feature.setFieldValue(
                Constants.FIELD_SHEET_HEIGHTS, treeStub.mHeight);
        feature.setFieldValue(
                Constants.FIELD_SHEET_CATEGORY, treeStub.mCategory);
        feature.setFieldValue(
                Constants.FIELD_SHEET_UNIT, treeStub.mUnit);
        feature.setFieldValue(
                Constants.FIELD_SHEET_SPECIES, treeStub.mSpecies);
        feature.setFieldValue(
                Constants.FIELD_SHEET_THICKNESS, treeStub.mThickness);
        feature.setFieldValue(
                Constants.FIELD_SHEET_COUNT, treeStub.mCount);
    }


    public void setOnAddTreeStubsListener(OnAddTreeStubsListener listener)
    {
        mOnAddTreeStubsListener = listener;
    }


    public interface OnAddTreeStubsListener
    {
        void onAddTreeStubs();
    }


    protected class TreeStub
    {
        String  mHeight;
        String  mCategory;
        String  mUnit;
        String  mSpecies;
        Integer mThickness;
        Integer mCount;
    }
}
