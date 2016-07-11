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

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.activity.SelectTerritoryActivity;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.dialog.InputParcelTextDialog;
import com.nextgis.forestinspector.overlay.EditTerritoryOverlay;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplibui.api.EditEventListener;
import com.nextgis.maplibui.fragment.BottomToolbar;
import com.nextgis.maplibui.util.ConstantsUI;


public class MapEditFragment
        extends MapFragment
        implements EditEventListener,
                   SelectTerritoryActivity.EventListener
{
    public static final String SHOW_EDIT_MODE_PANEL = "show_edit_mode_panel";

    protected DocumentEditFeature mEditFeature;

    protected EditTerritoryOverlay mTerritoryOverlay;
    protected float                mTolerancePX;

    protected boolean mShowEditModePanel = false;


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SHOW_EDIT_MODE_PANEL, mShowEditModePanel);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState) {
            mShowEditModePanel = savedInstanceState.getBoolean(SHOW_EDIT_MODE_PANEL);
        }

        Bundle extras = getActivity().getIntent().getExtras();

        if (null != extras && extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);

            MainApplication app = (MainApplication) getActivity().getApplication();
            mEditFeature = app.getEditFeature(featureId);
        }
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mTolerancePX =
                getActivity().getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;

        mTerritoryOverlay = new EditTerritoryOverlay(getActivity(), mMap, mEditFeature.getId());
        mMap.addOverlay(mTerritoryOverlay);

        mTerritoryOverlay.addListener(this);

        return view;
    }


    @Override
    public void onDestroyView()
    {
        mTerritoryOverlay.removeListener(this);

        super.onDestroyView();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        BottomToolbar toolbar = activity.getBottomToolbar();

        if (toolbar.getVisibility() == View.VISIBLE) {
            if (null != mStatusPanel) {
                mStatusPanel.setVisibility(View.GONE);
                mStatusPanel.removeAllViews();
            }
        }
    }


    @Override
    public void updateTerritory(GeoGeometry geometry)
    {
        if (null != geometry) {
            mTerritoryOverlay.setMode(EditTerritoryOverlay.MODE_HIGHLIGHT);
        }

        super.updateTerritory(geometry);
    }


    protected BottomToolbar setEditToolbarsView()
    {
        SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        View mainButton = activity.getFAB();
        if (null != mainButton) {
            mainButton.setVisibility(View.GONE);
        }

        if (null != mStatusPanel) {
            mStatusPanel.setVisibility(View.GONE);
            mStatusPanel.removeAllViews();
        }

        mShowEditModePanel = true;

        BottomToolbar toolbar = activity.getBottomToolbar();
        toolbar.setVisibility(View.VISIBLE);
        toolbar.getBackground().setAlpha(128);
        Menu menu = toolbar.getMenu();
        if (null != menu) {
            menu.clear();
        }

        return toolbar;
    }


    protected void setNormalToolbarsView()
    {
        SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        final BottomToolbar toolbar = activity.getBottomToolbar();
        if (null != toolbar) {
            toolbar.setVisibility(View.GONE);
        }

        View mainButton = activity.getFAB();
        if (null != mainButton) {
            mainButton.setVisibility(View.VISIBLE);
        }

        mShowEditModePanel = false;

        if (null != mStatusPanel) {
            if (mShowStatusPanel) {
                mStatusPanel.setVisibility(View.VISIBLE);
                fillStatusPanel(null);
            } else {
                mStatusPanel.setVisibility(View.GONE);
                mStatusPanel.removeAllViews();
            }
        }
    }

    public void addByHand()
    {
        BottomToolbar toolbar = setEditToolbarsView();
        mTerritoryOverlay.setEditModeToolbar(toolbar);
    }


    public void addByWalk()
    {
        BottomToolbar toolbar = setEditToolbarsView();
        mTerritoryOverlay.setEditByWalkModeToolbar(toolbar);
    }


    protected void finishEditSession(boolean closeActivity)
    {
        setNormalToolbarsView();

        if (mTerritoryOverlay.getMode() == EditTerritoryOverlay.MODE_EDIT) {
            mTerritoryOverlay.setMode(EditTerritoryOverlay.MODE_HIGHLIGHT);

            // Ask for text from user input or intersect with parcels
            InputParcelTextDialog dialog = new InputParcelTextDialog();
            dialog.setCloseActivity(closeActivity);
            dialog.show(getFragmentManager(), "input_parcel_text");

        } else if (closeActivity) {
            getActivity().finish();
        }
    }


    protected void finishEditByWalkSession(boolean closeActivity)
    {
        mTerritoryOverlay.stopGeometryByWalk();
        mTerritoryOverlay.setEditModeToolbar(null); // to edit mode toolbar

        finishEditSession(closeActivity);
    }


    protected void cancelEditSession()
    {
        setNormalToolbarsView();
        mTerritoryOverlay.setMode(EditTerritoryOverlay.MODE_NONE);

        SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        activity.clearTerritoryGeometry();
    }


    @Override
    public void onStartEditSession()
    {

    }


    @Override
    public void onFinishEditSession()
    {
        finishEditSession(false);
    }


    @Override
    public void onFinishEditByWalkSession()
    {
        finishEditByWalkSession(false);
    }


    @Override
    public void onApply()
    {
        switch (mTerritoryOverlay.getMode()) {
            case EditTerritoryOverlay.MODE_EDIT:
            default:
                finishEditSession(true);
                break;

            case EditTerritoryOverlay.MODE_EDIT_BY_WALK:
                finishEditByWalkSession(true);
                break;
        }
    }


    @Override
    public void onCancel()
    {
        cancelEditSession();
    }


    @Override
    public void onLongPress(MotionEvent event)
    {
        if (mTerritoryOverlay.getMode() != EditTerritoryOverlay.MODE_HIGHLIGHT) {
            return;
        }

        double dMinX = event.getX() - mTolerancePX;
        double dMaxX = event.getX() + mTolerancePX;
        double dMinY = event.getY() - mTolerancePX;
        double dMaxY = event.getY() + mTolerancePX;

        GeoEnvelope mapEnv = mMap.screenToMap(new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY));
        if (null == mapEnv) {
            return;
        }

        if (null != mEditFeature) {
            GeoGeometry geometry = mEditFeature.getGeometry();
            if (geometry.intersects(mapEnv)) {
                addByHand();
            }
        }
    }


    @Override
    protected void fillStatusPanel(Location location)
    {
        if (mShowEditModePanel) {
            return;
        }

        super.fillStatusPanel(location);
    }
}
