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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IActivityWithMap;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.mapui.MapViewOverlays;


public class MapViewFragment
        extends TabFragment
        implements MapViewEventListener
{

    protected MapViewOverlays      mMap;
    protected FloatingActionButton mivZoomIn;
    protected FloatingActionButton mivZoomOut;
    protected RelativeLayout       mMapRelativeLayout;

    protected DocumentFeature mDocFeature;

    // http://stackoverflow.com/a/29621490
    protected boolean mFragmentResume    = false;
    protected boolean mFragmentVisible   = false;
    protected boolean mFragmentOnCreated = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        Activity activity = getActivity();
        Bundle extras = activity.getIntent().getExtras();

        if (null == extras || !extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)
                || !extras.getBoolean(Constants.DOCUMENT_VIEWER)) {
            throw new RuntimeException("MapViewFragment, extras have not required data");
        }

        long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);

        MainApplication app = (MainApplication) activity.getApplication();
        DocumentsLayer docs = app.getDocsLayer();
        mDocFeature = docs.getFeatureWithAttaches(featureId);
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        Activity activity = getActivity();
        MainApplication app = (MainApplication) activity.getApplication();

        mMap = new MapViewOverlays(activity, (MapDrawable) app.getMap());
        mMap.setId(999);

        //search relative view of map, if not found - add it
        mMapRelativeLayout = (RelativeLayout) view.findViewById(R.id.maprl);

        // http://stackoverflow.com/a/29621490
        if (!mFragmentResume && mFragmentVisible) {   // only when first time fragment is created
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


        return view;
    }


    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);

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


    public void visibleState()
    {
        addMapView();
        setMenu(true);
    }


    public void invisibleState()
    {
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
        if (null != mMap) {
            mMap.removeListener(this);
        }

        super.onPause();
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

        if (null != mMap) {
            GeoEnvelope geoEnvelope = mDocFeature.getGeometry().getEnvelope();
            mMap.zoomToExtent(geoEnvelope);
            mMap.addListener(this);
        }
    }


    public void refresh()
    {
        if (null != mMap) {
            mMap.drawMapDrawable();
        }
    }


    @Override
    public void onLongPress(MotionEvent event)
    {

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
}
