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

package com.nextgis.forestinspector.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.DaveKoelle.AlphanumComparator;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.SheetActivity;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.styled_dialog.StyledDialogFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.nextgis.maplib.util.GeoConstants.GTMultiPoint;


public class SheetFillDialog
        extends StyledDialogFragment
        implements GpsEventListener
{
    public static final String UNKNOWN_LOCATION = "-";

    protected Feature mFeature;

    protected Location mFeatureLocation;
    protected String   mUnit;
    protected String   mSpecies;
    protected String   mCategory;
    protected String   mThickness;
    protected String   mHeight;
    protected String   mCount;

    protected EditText         mUnitView;
    protected AppCompatSpinner mSpeciesView;
    protected AppCompatSpinner mCategoryView;
    protected AppCompatSpinner mThicknessView;
    protected AppCompatSpinner mHeightView;
    protected EditText         mCountView;

    protected ArrayAdapter<String> mSpeciesAdapter;
    protected ArrayAdapter<String> mCategoryAdapter;
    protected ArrayAdapter<String> mThicknessAdapter;
    protected ArrayAdapter<String> mHeightAdapter;

    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;
    protected Location mLocation;

    protected GpsEventSource gpsEventSource;

    protected DocumentsLayer mDocsLayer;

    protected OnAddTreesListener mOnAddTreesListener;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        super.onCreate(savedInstanceState);


        MainApplication app = (MainApplication) getActivity().getApplication();
        MapBase map = app.getMap();

        mDocsLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                mDocsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if (null != mDocsLayer) {
            mSpeciesAdapter = getArrayAdapter(Constants.KEY_LAYER_SPECIES_TYPES, false);
            mCategoryAdapter = getArrayAdapter(Constants.KEY_LAYER_TREES_TYPES, false);
            mThicknessAdapter = getArrayAdapter(Constants.KEY_LAYER_THICKNESS_TYPES, true);
            mHeightAdapter = getArrayAdapter(Constants.KEY_LAYER_HEIGHT_TYPES, true);
        }

        if (null != mFeature) {
            // TODO: make for another types
            GeoGeometry geometry = mFeature.getGeometry();
            switch (geometry.getType()) {
                case GTMultiPoint: {
                    GeoMultiPoint mpt = (GeoMultiPoint) geometry;
                    GeoPoint pt = new GeoPoint(mpt.get(0));
                    pt.setCRS(GeoConstants.CRS_WEB_MERCATOR);
                    pt.project(GeoConstants.CRS_WGS84);
                    mFeatureLocation = new Location("");
                    mFeatureLocation.setLatitude(pt.getY());
                    mFeatureLocation.setLongitude(pt.getX());
                    break;
                }
                default: {
                    mFeatureLocation = new Location(SheetFillDialog.UNKNOWN_LOCATION);
                    break;
                }
            }

            mUnit = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_UNIT);
            mSpecies = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_SPECIES);
            mCategory = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_CATEGORY);
            mThickness = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_THICKNESS);
            mHeight = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_HEIGHTS);
            mCount = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_COUNT);
        }
    }


    protected ArrayAdapter<String> getArrayAdapter(
            String layerKey,
            boolean numberSort)
    {
        NGWLookupTable table = (NGWLookupTable) mDocsLayer.getLayerByName(layerKey);

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


    protected void setListSelection(
            AppCompatSpinner view,
            ArrayAdapter<String> adapter,
            String value)
    {
        if (null == adapter) {
            return;
        }

        for (int i = 0, count = adapter.getCount(); i < count; ++i) {
            String item = adapter.getItem(i);
            if (item.equals(value)) {
                view.setSelection(i);
                break;
            }
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        MainApplication app = (MainApplication) getActivity().getApplication();
        gpsEventSource = app.getGpsEventSource();
        gpsEventSource.addListener(this);

        View view = View.inflate(getActivity(), R.layout.dialog_sheet_fill, null);

        createLocationPanelView(view);

        mUnitView = (EditText) view.findViewById(R.id.unit);
        if (null != mUnit) {
            mUnitView.setText(mUnit);
            mUnit = null;
        }

        mSpeciesView = (AppCompatSpinner) view.findViewById(R.id.species);
        mSpeciesView.setAdapter(mSpeciesAdapter);
        if (null != mSpecies) {
            setListSelection(mSpeciesView, mSpeciesAdapter, mSpecies);
            mSpecies = null;
        }

        mCategoryView = (AppCompatSpinner) view.findViewById(R.id.category);
        mCategoryView.setAdapter(mCategoryAdapter);
        if (null != mCategory) {
            setListSelection(mCategoryView, mCategoryAdapter, mCategory);
            mCategory = null;
        }

        mThicknessView = (AppCompatSpinner) view.findViewById(R.id.thickness);
        mThicknessView.setAdapter(mThicknessAdapter);
        if (null != mThickness) {
            setListSelection(mThicknessView, mThicknessAdapter, mThickness);
            mThickness = null;
        }

        mHeightView = (AppCompatSpinner) view.findViewById(R.id.height);
        mHeightView.setAdapter(mHeightAdapter);
        if (null != mHeight) {
            setListSelection(mHeightView, mHeightAdapter, mHeight);
            mHeight = null;
        }

        mCountView = (EditText) view.findViewById(R.id.count);
        mCountView.setFilters(new InputFilter[] {new InputFilterMinMax(1, 1000)});
        if (null != mCount) {
            mCountView.setText(mCount);
            mCount = null;
        }


        setThemeDark(isAppThemeDark());

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_image_edit);
        } else {
            setIcon(R.drawable.ic_action_image_edit);
        }

        setView(view);

        if (null != mFeature) {
            setTitle(R.string.change_data);
            setPositiveText(R.string.save);
        } else {
            setTitle(R.string.add_trees);
            setPositiveText(R.string.add);
        }

        setNegativeText(R.string.cancel);

        setOnPositiveClickedListener(
                new SheetFillDialog.OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        addTrees();

                        if (null != mOnAddTreesListener) {
                            mOnAddTreesListener.onAddTrees();
                        }
                    }
                });

        setOnNegativeClickedListener(
                new SheetFillDialog.OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });

        return super.onCreateDialog(savedInstanceState);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    @Override
    public void onDestroyView()
    {
        gpsEventSource.removeListener(this);
        super.onDestroyView();
    }


    protected void createLocationPanelView(View view)
    {
        mLatView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.latitude_view);
        mLongView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.longitude_view);
        mAltView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.altitude_view);
        mAccView = (TextView) view.findViewById(com.nextgis.maplibui.R.id.accuracy_view);

        FrameLayout accLocPanel =
                (FrameLayout) view.findViewById(com.nextgis.maplibui.R.id.accurate_location_panel);
        accLocPanel.setVisibility(View.GONE);

        final ImageButton refreshLocation =
                (ImageButton) view.findViewById(com.nextgis.maplibui.R.id.refresh);

        refreshLocation.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (null != mFeatureLocation) {
                            mFeatureLocation = null;
                        }

                        RotateAnimation rotateAnimation = new RotateAnimation(
                                0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(700);
                        rotateAnimation.setRepeatCount(0);
                        refreshLocation.startAnimation(rotateAnimation);

                        Location location = gpsEventSource.getLastKnownLocation();
                        setLocationText(location);
                    }
                });


        if (null != mFeatureLocation) {
            if (mFeatureLocation.getProvider().equals(UNKNOWN_LOCATION)) {
                setLocationText(null);
            } else {
                setLocationText(mFeatureLocation);
            }

        } else {
            setLocationText(gpsEventSource.getLastKnownLocation());
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

        mLocation = location;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int nFormat = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_SECONDS);
        DecimalFormat df = new DecimalFormat("0.0");

        mLatView.setText(
                getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                LocationUtil.formatLatitude(location.getLatitude(), nFormat, getResources()));

        mLongView.setText(
                getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                LocationUtil.formatLongitude(location.getLongitude(), nFormat, getResources()));

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


    public void setFeature(Feature feature)
    {
        mFeature = feature;
    }


    public void addTrees()
    {
        if (null == mLocation) {
            Toast.makeText(getActivity(), getString(R.string.error_no_location), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        SheetActivity activity = (SheetActivity) getActivity();
        if (null == activity) {
            return;
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer documentsLayer =
                (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if (null == documentsLayer) {
            return;
        }

        VectorLayer sheetLayer =
                (VectorLayer) documentsLayer.getLayerByName(Constants.KEY_LAYER_SHEET);
        if (null == sheetLayer) {
            return;
        }


        Integer thicknessValue = Integer.parseInt(mThicknessView.getSelectedItem().toString());

        Integer countValue = TextUtils.isEmpty(mCountView.getText())
                             ? 1
                             : Integer.parseInt(mCountView.getText().toString());

        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
        pt.setCRS(GeoConstants.CRS_WGS84);
        pt.project(GeoConstants.CRS_WEB_MERCATOR);
        GeoMultiPoint geometryValue = new GeoMultiPoint();
        geometryValue.add(pt);

        Feature feature;
        if (null != mFeature) {
            feature = mFeature;
        } else {
            feature = new Feature(
                    com.nextgis.maplib.util.Constants.NOT_FOUND, sheetLayer.getFields());
            activity.getFeature().addSubFeature(Constants.KEY_LAYER_SHEET, feature);
        }

        feature.setFieldValue(
                Constants.FIELD_SHEET_UNIT, mUnitView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_SPECIES, mSpeciesView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_CATEGORY, mCategoryView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_THICKNESS, thicknessValue);
        feature.setFieldValue(
                Constants.FIELD_SHEET_HEIGHTS, mHeightView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_COUNT, countValue);
        feature.setGeometry(geometryValue);
    }


    public void setOnAddTreesListener(OnAddTreesListener listener)
    {
        mOnAddTreesListener = listener;
    }


    public interface OnAddTreesListener
    {
        void onAddTrees();
    }


    // http://stackoverflow.com/a/14212734/4727406
    public class InputFilterMinMax
            implements InputFilter
    {
        private int min, max;


        public InputFilterMinMax(
                int min,
                int max)
        {
            this.min = min;
            this.max = max;
        }


        public InputFilterMinMax(
                String min,
                String max)
        {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }


        // http://stackoverflow.com/a/19072151/4727406
        @Override
        public CharSequence filter(
                CharSequence source,
                int start,
                int end,
                Spanned dest,
                int dstart,
                int dend)
        {
            try {
                // Remove the string out of destination that is to be replaced
                String newVal = dest.toString().substring(0, dstart) +
                                dest.toString().substring(dend, dest.toString().length());
                // Add the new string in
                newVal = newVal.substring(0, dstart) + source.toString() +
                         newVal.substring(dstart, newVal.length());
                int input = Integer.parseInt(newVal);
                if (isInRange(min, max, input)) {
                    return null;
                }
            } catch (NumberFormatException nfe) {
            }
            return "";
        }


        private boolean isInRange(
                int a,
                int b,
                int c)
        {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}
