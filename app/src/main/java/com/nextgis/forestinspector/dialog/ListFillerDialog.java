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

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.DaveKoelle.AlphanumComparator;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IDocumentFeatureSource;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
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
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.nextgis.maplib.util.GeoConstants.GTMultiPoint;


public abstract class ListFillerDialog
        extends StyledDialogFragment
        implements GpsEventListener
{
    public static final String UNKNOWN_LOCATION = "-";

    protected Feature  mFeature;
    protected Location mFeatureLocation;

    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;

    protected GpsEventSource mGpsEventSource;
    protected Location       mLocation;

    protected OnAddListener mOnAddListener;


    protected abstract int getDialogViewResId();

    protected abstract void setFieldViews(View parentView);

    protected abstract String getLayerName();

    protected abstract void setFeatureFieldsValues(Feature feature);


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);


        IGISApplication app = (IGISApplication) getActivity().getApplication();
        mGpsEventSource = app.getGpsEventSource();

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
                    mFeatureLocation = new Location(ListFillerDialog.UNKNOWN_LOCATION);
                    break;
                }
            }
        }
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
                    mContext, android.R.layout.simple_spinner_item, dataArray);
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


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflateThemedLayout(getDialogViewResId());

        createLocationPanelView(view);

        setFieldViews(view);

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_edit_light);
        } else {
            setIcon(R.drawable.ic_action_edit_light);
        }

        setView(view, true);

        if (null != mFeature) {
            setTitle(R.string.change_data);
            setPositiveText(R.string.save);
        } else {
            setTitle(R.string.add_data);
            setPositiveText(R.string.add);
        }

        setNegativeText(R.string.cancel);

        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        addData();
                    }
                });

        setOnNegativeClickedListener(
                new OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });

        return super.onCreateView(inflater, container, savedInstanceState);
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


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
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
                        RotateAnimation rotateAnimation = new RotateAnimation(
                                0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(700);
                        rotateAnimation.setRepeatCount(0);
                        refreshLocation.startAnimation(rotateAnimation);

                        mFeatureLocation = mGpsEventSource.getLastKnownLocation();
                        setLocationText(mFeatureLocation);
                    }
                });


        if (null != mFeatureLocation) {
            if (mFeatureLocation.getProvider().equals(UNKNOWN_LOCATION)) {
                setLocationText(null);
            } else {
                setLocationText(mFeatureLocation);
            }

        } else {
            mFeatureLocation = mGpsEventSource.getLastKnownLocation();
            setLocationText(mFeatureLocation);
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


    public void addData()
    {
        if (!isCorrectValues()) {
            return;
        }

        saveData();

        if (null != mOnAddListener) {
            mOnAddListener.onAdd();
        }
    }


    protected boolean isCorrectValues()
    {
        if (null == mLocation) {
            Toast.makeText(getActivity(), getString(R.string.error_no_location), Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        return true;
    }


    public void saveData()
    {
        Activity activity = getActivity();
        IDocumentFeatureSource documentSource = null;
        if (activity instanceof IDocumentFeatureSource) {
            documentSource = (IDocumentFeatureSource) activity;
        }
        if (null == documentSource) {
            return;
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer docLayer =
                (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if (null == docLayer) {
            return;
        }

        VectorLayer subDocLayer = (VectorLayer) docLayer.getLayerByName(getLayerName());
        if (null == subDocLayer) {
            return;
        }


        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
        pt.setCRS(GeoConstants.CRS_WGS84);
        pt.project(GeoConstants.CRS_WEB_MERCATOR);
        GeoMultiPoint geometryValue = new GeoMultiPoint();
        geometryValue.add(pt);


        DocumentFeature docFeature = documentSource.getFeature();
        Feature subFeature;

        if (null != mFeature) {
            subFeature = mFeature;
        } else {
            subFeature = docLayer.getNewTempSubFeature(docFeature, subDocLayer);
        }

        subFeature.setGeometry(geometryValue);
        setFeatureFieldsValues(subFeature);

        subDocLayer.updateFeatureWithFlags(subFeature);
    }


    public void setOnAddListener(OnAddListener listener)
    {
        mOnAddListener = listener;
    }


    public interface OnAddListener
    {
        void onAdd();
    }
}
