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

package com.nextgis.forestinspector.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IActivityWithMap;
import com.nextgis.forestinspector.activity.MapActivity;
import com.nextgis.forestinspector.dialog.ClickedItemsInfoGetter;
import com.nextgis.forestinspector.overlay.SelectLocationOverlay;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.mapui.MapViewOverlays;
import com.nextgis.maplibui.overlay.CurrentLocationOverlay;
import com.nextgis.maplibui.util.ConstantsUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.IOException;


public class MapFragment
        extends Fragment
        implements MapViewEventListener,
                   GpsEventListener
{

    protected MapViewOverlays      mMap;
    protected FloatingActionButton mivZoomIn;
    protected FloatingActionButton mivZoomOut;
    protected RelativeLayout       mMapRelativeLayout;

    protected TextView mStatusSource, mStatusAccuracy, mStatusSpeed, mStatusAltitude,
            mStatusLatitude, mStatusLongitude;
    protected FrameLayout mStatusPanel;

    protected GpsEventSource         mGpsEventSource;
    protected CurrentLocationOverlay mCurrentLocationOverlay;
    protected SelectLocationOverlay  mSelectLocationOverlay;

    protected boolean     mShowStatusPanel;
    protected GeoPoint    mCurrentCenter;
    protected int         mCoordinatesFormat;
    protected int         mCoordinatesFraction;
    protected GeoEnvelope mEnvelopeParam;

    // http://stackoverflow.com/a/29621490
    protected boolean mFragmentResume    = false;
    protected boolean mFragmentVisible   = false;
    protected boolean mFragmentOnCreated = false;

    protected boolean mIsInViewPager = false;

    protected float mTolerancePX;


    public void setInViewPager(boolean isInViewPager)
    {
        mIsInViewPager = isInViewPager;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        MainApplication app = (MainApplication) getActivity().getApplication();
        mTolerancePX = app.getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;

        Intent intent = getActivity().getIntent();
        byte[] geometryParam = intent.getByteArrayExtra(MapActivity.PARAM_GEOMETRY);
        GeoGeometry geometry = null;

        if (null != geometryParam) {
            try {
                geometry = GeoGeometryFactory.fromBlob(geometryParam);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                geometry = null;
            }
        }

        if (null != geometry) {
            mEnvelopeParam = geometry.getEnvelope();
        }

    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MainApplication app = (MainApplication) getActivity().getApplication();

        mMap = new MapViewOverlays(getActivity(), (MapDrawable) app.getMap());
        mMap.setId(999);

        if (null != mEnvelopeParam) {
            mMap.zoomToExtent(mEnvelopeParam);
        }

        mGpsEventSource = app.getGpsEventSource();
        mCurrentLocationOverlay = new CurrentLocationOverlay(getActivity(), mMap);
        mCurrentLocationOverlay.setStandingMarker(R.drawable.ic_location_standing);
        mCurrentLocationOverlay.setMovingMarker(R.drawable.ic_location_moving);
        mSelectLocationOverlay = new SelectLocationOverlay(getActivity(), mMap);
        mSelectLocationOverlay.setVisibility(false);

        mMap.addOverlay(mSelectLocationOverlay);
        mMap.addOverlay(mCurrentLocationOverlay);

        //search relative view of map, if not found - add it
        mMapRelativeLayout = (RelativeLayout) view.findViewById(R.id.maprl);

        // http://stackoverflow.com/a/29621490
        if (!mIsInViewPager || !mFragmentResume
                && mFragmentVisible) { // only when first time fragment is created
            addMapView();
        }

        mMap.invalidate();

        mivZoomIn = (FloatingActionButton) view.findViewById(R.id.action_zoom_in);
        if (null != mivZoomIn) {
            mivZoomIn.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (mivZoomIn.isEnabled()) {
                                mMap.zoomIn();
                            }
                        }
                    });
        }

        mivZoomOut = (FloatingActionButton) view.findViewById(R.id.action_zoom_out);
        if (null != mivZoomOut) {
            mivZoomOut.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (mivZoomOut.isEnabled()) {
                                mMap.zoomOut();
                            }
                        }
                    });
        }

        mStatusPanel = (FrameLayout) view.findViewById(R.id.fl_status_panel);

        return view;
    }


    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);

        if (mIsInViewPager) {
            // http://stackoverflow.com/a/29621490
            if (visible && isResumed()) {               // only at fragment screen is resumed
                mFragmentResume = true;
                mFragmentVisible = false;
                mFragmentOnCreated = true;
                visibleState();

            } else if (visible) {                       // only at fragment onCreated
                mFragmentResume = false;
                mFragmentVisible = true;
                mFragmentOnCreated = true;

            } else if (/* !visible && */ mFragmentOnCreated) { // only when you go out of fragment screen
                mFragmentVisible = false;
                mFragmentResume = false;
                invisibleState();
            }
        }
    }


    public void visibleState()
    {
        addMapView();
        startGpsWork();
        setMenu(true);
    }


    public void invisibleState()
    {
        stopGpsWork();
        removeMapView();
        setMenu(false);
    }


    public void addMapView()
    {
        if (null != mMapRelativeLayout && mMapRelativeLayout.indexOfChild(mMap) == -1) {
            mMapRelativeLayout.addView(
                    mMap, 0, new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT));
        }
    }


    public void removeMapView()
    {
        if (null != mMapRelativeLayout && mMapRelativeLayout.indexOfChild(mMap) != -1) {
            mMapRelativeLayout.removeView(mMap);
        }
    }


    public void startGpsWork()
    {
        if (null != mCurrentLocationOverlay) {
            mCurrentLocationOverlay.updateMode(
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString(SettingsConstantsUI.KEY_PREF_SHOW_CURRENT_LOC, "3"));
            mCurrentLocationOverlay.startShowingCurrentLocation();
        }
        if (null != mGpsEventSource) {
            mGpsEventSource.addListener(this);
        }
    }


    public void stopGpsWork()
    {
        if (null != mCurrentLocationOverlay) {
            mCurrentLocationOverlay.stopShowingCurrentLocation();
        }
        if (null != mGpsEventSource) {
            mGpsEventSource.removeListener(this);
        }
    }


    protected void setMenu(boolean menuForMap)
    {
        Activity activity = getActivity();
        if (activity instanceof IActivityWithMap) {
            IActivityWithMap mapActivity = (IActivityWithMap) activity;
            mapActivity.setMenuForMap(menuForMap);
        }
    }


    @Override
    public void onDestroyView()
    {
        if (mMap != null) {
            mMap.removeListener(this);
            if (mMapRelativeLayout != null) {
                mMapRelativeLayout.removeView(mMap);
            }
        }

        super.onDestroyView();
    }


    protected void showMapButtons(
            boolean show,
            RelativeLayout rl)
    {
        if (null == rl) {
            return;
        }
        View v = rl.findViewById(R.id.action_zoom_out);
        if (null != v) {
            if (show) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }

        v = rl.findViewById(R.id.action_zoom_in);
        if (null != v) {
            if (show) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }
        }

        rl.invalidate();
    }


    @Override
    public void onPause()
    {
        stopGpsWork();

        if (null != mMap) {
            storeMapSettings();
            mMap.removeListener(this);
        }

        super.onPause();
    }


    public void storeMapSettings()
    {
        final SharedPreferences.Editor edit =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        edit.putFloat(SettingsConstants.KEY_PREF_ZOOM_LEVEL, mMap.getZoomLevel());
        GeoPoint point = mMap.getMapCenter();
        edit.putLong(SettingsConstants.KEY_PREF_SCROLL_X, Double.doubleToRawLongBits(point.getX()));
        edit.putLong(SettingsConstants.KEY_PREF_SCROLL_Y, Double.doubleToRawLongBits(point.getY()));

        edit.commit();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean showControls =
                prefs.getBoolean(SettingsConstants.KEY_PREF_SHOW_ZOOM_CONTROLS, false);
        showMapButtons(showControls, mMapRelativeLayout);

        Log.d(Constants.TAG, "KEY_PREF_SHOW_ZOOM_CONTROLS: " + (showControls ? "ON" : "OFF"));

        if (null != mMap) {

            if (null == mEnvelopeParam) {
                if (prefs.getBoolean(SettingsConstants.KEY_PREF_MAP_FIRST_VIEW, true)) {
                    //zoom to inspector extent
                    float minX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINX, -2000.0f);
                    float minY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMINY, -2000.0f);
                    float maxX = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXX, 2000.0f);
                    float maxY = prefs.getFloat(SettingsConstants.KEY_PREF_USERMAXY, 2000.0f);
                    mMap.zoomToExtent(new GeoEnvelope(minX, maxX, minY, maxY));

                    final SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean(SettingsConstants.KEY_PREF_MAP_FIRST_VIEW, false);
                    edit.commit();

                } else {
                    float mMapZoom;
                    try {
                        mMapZoom = prefs.getFloat(
                                SettingsConstants.KEY_PREF_ZOOM_LEVEL, mMap.getMinZoom());
                    } catch (ClassCastException e) {
                        mMapZoom = mMap.getMinZoom();
                    }

                    double mMapScrollX;
                    double mMapScrollY;
                    try {
                        mMapScrollX = Double.longBitsToDouble(
                                prefs.getLong(SettingsConstants.KEY_PREF_SCROLL_X, 0));
                        mMapScrollY = Double.longBitsToDouble(
                                prefs.getLong(SettingsConstants.KEY_PREF_SCROLL_Y, 0));
                    } catch (ClassCastException e) {
                        mMapScrollX = 0;
                        mMapScrollY = 0;
                    }
                    mMap.setZoomAndCenter(mMapZoom, new GeoPoint(mMapScrollX, mMapScrollY));
                }
            }
            mMap.addListener(this);
        }

        mCoordinatesFormat = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_DEGREES);
        mCoordinatesFraction = prefs.getInt(
                SettingsConstantsUI.KEY_PREF_COORD_FRACTION,
                com.nextgis.forestinspector.util.Constants.DEFAULT_COORDINATES_FRACTION_DIGITS);

        if (!mIsInViewPager || mFragmentResume) {
            startGpsWork();
        }

        mShowStatusPanel = prefs.getBoolean(SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL, false);

        if (null != mStatusPanel) {
            if (mShowStatusPanel) {
                mStatusPanel.setVisibility(View.VISIBLE);
                fillStatusPanel(null);
            } else {
                mStatusPanel.removeAllViews();
            }
        }

        mCurrentCenter = null;
    }


    public void refresh()
    {
        if (null != mMap) {
            mMap.drawMapDrawable();
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null) {
            if (mCurrentCenter == null) {
                mCurrentCenter = new GeoPoint();
            }

            mCurrentCenter.setCoordinates(location.getLongitude(), location.getLatitude());
            mCurrentCenter.setCRS(GeoConstants.CRS_WGS84);

            if (!mCurrentCenter.project(GeoConstants.CRS_WEB_MERCATOR)) {
                mCurrentCenter = null;
            }
        }

        fillStatusPanel(location);
    }


    @Override
    public void onBestLocationChanged(Location location)
    {

    }


    @Override
    public void onGpsStatusChanged(int event)
    {

    }


    @Override
    public void onLongPress(MotionEvent event)
    {
        if (getActivity() instanceof MapActivity) {
            return;
        }

        double minX = event.getX() - mTolerancePX;
        double maxX = event.getX() + mTolerancePX;
        double minY = event.getY() - mTolerancePX;
        double maxY = event.getY() + mTolerancePX;

        GeoEnvelope envelope = mMap.screenToMap(new GeoEnvelope(minX, maxX, minY, maxY));
        if (null == envelope) {
            return;
        }

        ClickedItemsInfoGetter infoGetter =
                new ClickedItemsInfoGetter((AppCompatActivity) getActivity(), envelope);
        infoGetter.showInfo();
    }


    @Override
    public void onSingleTapUp(MotionEvent event)
    {

    }


    @Override
    public void panStart(MotionEvent e)
    {

    }


    @Override
    public void panMoveTo(MotionEvent e)
    {

    }


    @Override
    public void panStop()
    {

    }


    @Override
    public void onLayerAdded(int id)
    {

    }


    @Override
    public void onLayerDeleted(int id)
    {

    }


    @Override
    public void onLayerChanged(int id)
    {

    }


    @Override
    public void onExtentChanged(
            float zoom,
            GeoPoint center)
    {
        setZoomInEnabled(mMap.canZoomIn());
        setZoomOutEnabled(mMap.canZoomOut());
    }


    @Override
    public void onLayersReordered()
    {

    }


    @Override
    public void onLayerDrawFinished(
            int id,
            float percent)
    {

    }


    @Override
    public void onLayerDrawStarted()
    {

    }


    protected void setZoomInEnabled(boolean bEnabled)
    {
        if (mivZoomIn == null) {
            return;
        }

        mivZoomIn.setEnabled(bEnabled);
    }


    protected void setZoomOutEnabled(boolean bEnabled)
    {
        if (mivZoomOut == null) {
            return;
        }
        mivZoomOut.setEnabled(bEnabled);
    }


    protected void fillStatusPanel(Location location)
    {
        if (!mShowStatusPanel) //mStatusPanel.getVisibility() == FrameLayout.INVISIBLE)
        {
            return;
        }

        boolean needViewUpdate = true;
        boolean isCurrentOrientationOneLine =
                mStatusPanel.getChildCount() > 0 && ((LinearLayout) mStatusPanel.getChildAt(
                        0)).getOrientation() == LinearLayout.HORIZONTAL;

        View panel;
        if (!isCurrentOrientationOneLine) {
            panel = getActivity().getLayoutInflater()
                    .inflate(R.layout.status_panel_land, mStatusPanel, false);
            defineTextViews(panel);
        } else {
            panel = mStatusPanel.getChildAt(0);
            needViewUpdate = false;
        }

        fillTextViews(location);

        if (!isFitOneLine()) {
            panel = getActivity().getLayoutInflater()
                    .inflate(R.layout.status_panel, mStatusPanel, false);
            defineTextViews(panel);
            fillTextViews(location);
            needViewUpdate = true;
        }

        if (needViewUpdate) {
            mStatusPanel.removeAllViews();
            panel.getBackground().setAlpha(128);
            mStatusPanel.addView(panel);
        }
    }


    protected void fillTextViews(Location location)
    {
        if (null == location) {
            setDefaultTextViews();
        } else {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                String text = "";
                int satellites = location.getExtras() != null ? location.getExtras().getInt("satellites") : 0;
                if (satellites > 0)
                    text += satellites;

                mStatusSource.setText(text);
                mStatusSource.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_location), null, null, null);
            } else {
                mStatusSource.setText("");
                mStatusSource.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_signal_wifi), null, null, null);
            }

            mStatusAccuracy.setText(
                    String.format(
                            "%.1f %s", location.getAccuracy(), getString(R.string.unit_meter)));
            mStatusAltitude.setText(
                    String.format(
                            "%.1f %s", location.getAltitude(), getString(R.string.unit_meter)));
            mStatusSpeed.setText(
                    String.format(
                            "%.1f %s/%s", location.getSpeed() * 3600 / 1000,
                            getString(R.string.unit_kilometer), getString(R.string.unit_hour)));
            mStatusLatitude.setText(
                    formatCoordinate(location.getLatitude(), R.string.latitude_caption_short));
            mStatusLongitude.setText(
                    formatCoordinate(location.getLongitude(), R.string.longitude_caption_short));
        }
    }

    private String formatCoordinate(double value, int appendix) {
        return LocationUtil.formatCoordinate(value, mCoordinatesFormat, mCoordinatesFraction) + " " + getString(appendix);
    }

    protected void setDefaultTextViews()
    {
        mStatusSource.setCompoundDrawables(null, null, null, null);
        mStatusSource.setText("");
        mStatusAccuracy.setText(getString(R.string.n_a));
        mStatusAltitude.setText(getString(R.string.n_a));
        mStatusSpeed.setText(getString(R.string.n_a));
        mStatusLatitude.setText(getString(R.string.n_a));
        mStatusLongitude.setText(getString(R.string.n_a));
    }


    protected boolean isFitOneLine()
    {
        mStatusLongitude.measure(0, 0);
        mStatusLatitude.measure(0, 0);
        mStatusAltitude.measure(0, 0);
        mStatusSpeed.measure(0, 0);
        mStatusAccuracy.measure(0, 0);
        mStatusSource.measure(0, 0);

        int totalWidth = mStatusSource.getMeasuredWidth() + mStatusLongitude.getMeasuredWidth() +
                mStatusLatitude.getMeasuredWidth() + mStatusAccuracy.getMeasuredWidth() +
                mStatusSpeed.getMeasuredWidth() + mStatusAltitude.getMeasuredWidth();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return totalWidth < metrics.widthPixels;
    }


    private void defineTextViews(View panel)
    {
        mStatusSource = (TextView) panel.findViewById(R.id.tv_source);
        mStatusAccuracy = (TextView) panel.findViewById(R.id.tv_accuracy);
        mStatusSpeed = (TextView) panel.findViewById(R.id.tv_speed);
        mStatusAltitude = (TextView) panel.findViewById(R.id.tv_altitude);
        mStatusLatitude = (TextView) panel.findViewById(R.id.tv_latitude);
        mStatusLongitude = (TextView) panel.findViewById(R.id.tv_longitude);
    }


    public void locateCurrentPosition()
    {
        if (mCurrentCenter != null) {
            mMap.panTo(mCurrentCenter);
        } else {
            Toast.makeText(getActivity(), R.string.error_no_location, Toast.LENGTH_SHORT).show();
        }
    }


    public void zoomToExtent(GeoEnvelope envelope)
    {
        mMap.zoomToExtent(envelope);
    }


    public void setZoomAndCenter(
            float zoom,
            GeoPoint center)
    {
        mMap.setZoomAndCenter(zoom, center);
    }


    public void updateTerritory(GeoGeometry geometry)
    {
        if (null != geometry) {
            zoomToExtent(geometry.getEnvelope());
            storeMapSettings();
        }
    }


    public Location getSelectedLocation()
    {
        return mSelectLocationOverlay.getSelectedLocation();
    }


    public void setSelectedLocation(Location location)
    {
        mSelectLocationOverlay.setSelectedLocation(location);
    }


    public void setSelectedLocationVisible(boolean isVisible)
    {
        mSelectLocationOverlay.setVisibility(isVisible);
    }
}
