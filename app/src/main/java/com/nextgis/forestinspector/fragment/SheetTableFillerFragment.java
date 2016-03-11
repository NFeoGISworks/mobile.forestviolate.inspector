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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.DaveKoelle.AlphanumComparator;
import com.justsimpleinfo.Table.Table;
import com.justsimpleinfo.Table.TableData;
import com.justsimpleinfo.Table.TableRowData;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.MapActivity;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.dialog.UnitEditorDialog;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.GeoPolygon;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.IOException;
import java.text.DecimalFormat;
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
        implements GpsEventListener
{
    public static final int REQUEST_LOCATION = 1;

    protected final static int CREATE_TABLE_DONE   = 0;
    protected final static int CREATE_TABLE_OK     = 1;
    protected final static int CREATE_TABLE_FAILED = 2;

    protected Table mTable;

    protected GpsEventSource mGpsEventSource;
    protected Location       mLocation;

    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;

    protected AppCompatSpinner mHeightView;
    protected AppCompatSpinner mCategoryView;
    protected TextView         mUnitView;
    protected TextView         mTableWarning;
    protected LinearLayout     mTableLayout;

    protected String mUnitText;

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

        IGISApplication app = (IGISApplication) getActivity().getApplication();
        mGpsEventSource = app.getGpsEventSource();

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

        createLocationPanelView(view);

        mTableLayout = (LinearLayout) view.findViewById(R.id.table_layout);
        mTableWarning = (TextView) view.findViewById(R.id.table_warning);

        mHeightView = (AppCompatSpinner) view.findViewById(R.id.height);
        mHeightView.setAdapter(mHeightAdapter);

        mCategoryView = (AppCompatSpinner) view.findViewById(R.id.category);
        mCategoryView.setAdapter(mCategoryAdapter);

        mUnitView = (TextView) view.findViewById(R.id.unit);

        if (null != mUnitText) {
            mUnitView.setText(mUnitText);
        }

        mUnitView.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        final UnitEditorDialog dialog = new UnitEditorDialog();
                        dialog.setUnitDesc(mUnitView.getText().toString());
                        dialog.setOnPositiveClickedListener(
                                new UnitEditorDialog.OnPositiveClickedListener()
                                {
                                    @Override
                                    public void onPositiveClicked()
                                    {
                                        mUnitText = dialog.getText();
                                        mUnitView.setText(mUnitText);
                                    }
                                });
                        dialog.show(
                                getActivity().getSupportFragmentManager(),
                                Constants.FRAGMENT_UNIT_EDITOR_DIALOG);
                    }
                });

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


    @Override
    public void onPause()
    {
        mGpsEventSource.removeListener(this);
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mGpsEventSource.addListener(this);
    }


    protected void createLocationPanelView(View view)
    {
        mLatView = (TextView) view.findViewById(R.id.latitude_view);
        mLongView = (TextView) view.findViewById(R.id.longitude_view);
        mAltView = (TextView) view.findViewById(R.id.altitude_view);
        mAccView = (TextView) view.findViewById(R.id.accuracy_view);

        final ImageButton refreshLocation = (ImageButton) view.findViewById(R.id.refresh);

        refreshLocation.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        RotateAnimation rotateAnimation = new RotateAnimation(
                                0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(700);
                        rotateAnimation.setRepeatCount(0);
                        refreshLocation.startAnimation(rotateAnimation);

                        mLocation = mGpsEventSource.getLastKnownLocation();
                        setLocationText(mLocation);
                    }
                });


        ImageButton openMap = (ImageButton) view.findViewById(R.id.open_map);
        openMap.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        DocumentFeature feature = getDocumentFeature();
                        GeoGeometry geometry = null;
                        Intent intent = new Intent(getActivity(), MapActivity.class);

                        if (null != feature) {
                            geometry = feature.getGeometry();
                        }

                        if (null == geometry) {
                            final SharedPreferences prefs =
                                    PreferenceManager.getDefaultSharedPreferences(getActivity());
                            float minX =
                                    prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
                            float minY =
                                    prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
                            float maxX =
                                    prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
                            float maxY =
                                    prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);

                            GeoPolygon polygon = new GeoPolygon();
                            polygon.add(new GeoPoint(minX, minY));
                            polygon.add(new GeoPoint(minX, maxY));
                            polygon.add(new GeoPoint(maxX, maxY));
                            polygon.add(new GeoPoint(maxX, minY));

                            geometry = polygon;
                        }

                        try {
                            intent.putExtra(MapActivity.PARAM_GEOMETRY, geometry.toBlob());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        startActivityForResult(intent, REQUEST_LOCATION);
                    }
                });

        if (null != mLocation) {
            setLocationText(mLocation);
        } else {
            mLocation = mGpsEventSource.getLastKnownLocation();
            setLocationText(mLocation);
        }
    }


    protected void setLocationText(Location location)
    {
        if (null == mLatView || null == mLongView || null == mAccView || null == mAltView) {
            return;
        }

        if (null == location) {

            mLatView.setText(
                    getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                            getString(com.nextgis.maplibui.R.string.n_a));
            mLongView.setText(
                    getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                            getString(com.nextgis.maplibui.R.string.n_a));
            mAltView.setText(
                    getString(com.nextgis.maplibui.R.string.altitude_caption_short) + ": " +
                            getString(com.nextgis.maplibui.R.string.n_a));
            mAccView.setText(
                    getString(com.nextgis.maplibui.R.string.accuracy_caption_short) + ": " +
                            getString(com.nextgis.maplibui.R.string.n_a));

            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int nFormat = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_SECONDS);
        int nFraction = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FRACTION,
                Constants.DEFAULT_COORDINATES_FRACTION_DIGITS);
        DecimalFormat df = new DecimalFormat("0.0");

        mLatView.setText(
                getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                        LocationUtil.formatLatitude(
                                location.getLatitude(), nFormat, nFraction, getResources()));

        mLongView.setText(
                getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                        LocationUtil.formatLongitude(
                                location.getLongitude(), nFormat, nFraction, getResources()));

        double altitude = location.getAltitude();
        mAltView.setText(
                getString(com.nextgis.maplibui.R.string.altitude_caption_short) + ": " +
                        df.format(altitude) + " " +
                        getString(com.nextgis.maplibui.R.string.unit_meter));

        float accuracy = location.getAccuracy();
        mAccView.setText(
                getString(com.nextgis.maplibui.R.string.accuracy_caption_short) + ": " +
                        df.format(accuracy) + " " +
                        getString(com.nextgis.maplibui.R.string.unit_meter));
    }


    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK) {
            double latitude = data.getDoubleExtra(MapActivity.LOCATION_LATITUDE, 0);
            double longitude = data.getDoubleExtra(MapActivity.LOCATION_LONGITUDE, 0);

            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            setLocationText(location);
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {

    }


    @Override
    public void onBestLocationChanged(Location location)
    {

    }


    @Override
    public void onGpsStatusChanged(int event)
    {

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


    public boolean saveTableData()
    {
        if (null == mLocation) {
            Log.d(Constants.FITAG, "saveTableData(), null == mLocation");
            Toast.makeText(
                    getActivity(), R.string.coordinates_not_defined_specify_on_map,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        MainApplication app = (MainApplication) getActivity().getApplication();
        DocumentsLayer docLayer = app.getDocsLayer();
        if (null == docLayer) {
            Log.d(Constants.FITAG, "saveTableData() error, null == docLayer");
            return false;
        }
        VectorLayer subDocLayer = (VectorLayer) docLayer.getLayerByName(Constants.KEY_LAYER_SHEET);
        if (null == subDocLayer) {
            Log.d(Constants.FITAG, "saveTableData() error, null == subDocLayer");
            return false;
        }
        DocumentFeature docFeature = getDocumentFeature();

        String height = mHeightView.getSelectedItem().toString();
        String category = mCategoryView.getSelectedItem().toString();
        String unit = mUnitView.getText().toString();
        TableData tableData = mTable.getTableData();

        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
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

        return true;
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
