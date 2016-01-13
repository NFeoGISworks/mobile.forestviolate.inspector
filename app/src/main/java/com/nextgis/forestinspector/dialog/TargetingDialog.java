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

import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.SimpleDividerItemDecoration;
import com.nextgis.forestinspector.adapter.TargetingListAdapter;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.text.DecimalFormat;
import java.util.Locale;


public class TargetingDialog
        extends StyledDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                   TargetingListAdapter.OnSelectionChangedListener,
                   GpsEventListener
{
    protected static String NOT_SELECTED = "-1";

    protected static int MSG_PRESS_SKIP = 1;

    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;

    protected GpsEventSource mGpsEventSource;
    protected Location       mLocation;

    protected RecyclerView           mListView;
    protected TargetingListAdapter   mAdapter;
    protected OnSelectTargetListener mOnSelectTargetListener;

    protected SwitchCompat mSwitchFilter;
    protected boolean mShowAllTargets = false;
    protected boolean mIsInitialView  = true;

    protected Handler mHandler;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setCancelable(false);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);

        IGISApplication app = (IGISApplication) getActivity().getApplication();
        mGpsEventSource = app.getGpsEventSource();
        mLocation = mGpsEventSource.getLastKnownLocation();

        mAdapter = new TargetingListAdapter(mContext, null);
        mAdapter.setSingleSelectable(true);
        mAdapter.addOnSelectionChangedListener(this);

        // handler for commit a fragment after data is loaded
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == MSG_PRESS_SKIP) {
                    mButtonNegative.performClick();
                }
            }
        };

        mShowAllTargets = (null == mLocation);
        runLoader();
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflateThemedLayout(R.layout.dialog_targeting);

        createLocationPanelView(view);

        mListView = (RecyclerView) view.findViewById(R.id.list);
        mListView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mListView.setHasFixedSize(true);
        mListView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mListView.setAdapter(mAdapter);

        mSwitchFilter = (SwitchCompat) view.findViewById(R.id.switch_filter);
        mSwitchFilter.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView,
                            boolean isChecked)
                    {
                        setSwitchFilterText(!isChecked);
                        setShowAllTargets(!isChecked);
                    }
                });

        if (null == mLocation) {
            mSwitchFilter.setEnabled(false);
            setShowAllTargets(true);
        }

        setSwitchFilterState(!mShowAllTargets);


        if (isThemeDark()) {
            setIcon(R.drawable.ic_track_changes);
        } else {
            setIcon(R.drawable.ic_track_changes);
        }

        setTitle(R.string.targeting_selection);
        setView(view, false);
        setPositiveText(R.string.ok);
        setNegativeText(R.string.skip);

        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        onSelectTargeting();
                    }
                });

        setOnNegativeClickedListener(
                new OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        onCancelTargeting();
                    }
                });


        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mButtonPositive.setEnabled(mAdapter.hasSelectedItems());

        return rootView;
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

                        boolean locationWasUnknown = (null == mLocation);

                        mLocation = mGpsEventSource.getLastKnownLocation();
                        setLocationText(mLocation);


                        if (null == mLocation) {
                            mSwitchFilter.setEnabled(false);
                            setShowAllTargets(true);
                        } else {
                            mSwitchFilter.setEnabled(true);
                            if (locationWasUnknown) {
                                setShowAllTargets(false);
                            }
                        }

                        setSwitchFilterState(!mShowAllTargets);
                    }
                });


        setLocationText(mLocation);
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
        DecimalFormat df = new DecimalFormat("0.0");

        mLatView.setText(
                getString(com.nextgis.maplibui.R.string.latitude_caption_short) + ": " +
                        LocationUtil.formatLatitude(
                                location.getLatitude(), nFormat, getResources()));

        mLongView.setText(
                getString(com.nextgis.maplibui.R.string.longitude_caption_short) + ": " +
                        LocationUtil.formatLongitude(
                                location.getLongitude(), nFormat, getResources()));

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


    protected void setShowAllTargets(boolean showAllTargets)
    {
        if (mShowAllTargets != showAllTargets) {
            mShowAllTargets = showAllTargets;
            runLoader();
        }
    }


    protected void setSwitchFilterText(boolean showNearestTargetsText)
    {
        if (showNearestTargetsText) {
            mSwitchFilter.setText(mContext.getText(R.string.show_nearest_targets));
        } else {
            mSwitchFilter.setText(mContext.getText(R.string.show_all_targets));
        }
    }


    protected void setSwitchFilterState(boolean isChecked)
    {
        setSwitchFilterText(!isChecked);
        mSwitchFilter.setChecked(isChecked);
    }


    private void runLoader()
    {
        Loader loader = getLoaderManager().getLoader(0);
        if (null != loader && loader.isStarted()) {
            getLoaderManager().restartLoader(0, null, this);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args)
    {
        Uri uri = Uri.parse(
                "content://" + SettingsConstants.AUTHORITY + "/" + Constants.KEY_LAYER_FV);

        String[] projection = {
                com.nextgis.maplib.util.Constants.FIELD_ID,
                Constants.FIELD_FV_OBJECTID,
                Constants.FIELD_FV_STATUS,
                Constants.FIELD_FV_DATE,
                Constants.FIELD_FV_FORESTRY,
                Constants.FIELD_FV_PRECINCT,
                Constants.FIELD_FV_REGION,
                Constants.FIELD_FV_TERRITORY};

        String sortOrder = Constants.FIELD_FV_DATE + " DESC";

        String selection =
                Constants.FIELD_FV_STATUS + " = " + Constants.FV_STATUS_NEW_FOREST_CHANGE;

        if (!mShowAllTargets && mLocation != null) {
            GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
            pt.setCRS(GeoConstants.CRS_WGS84);
            pt.project(GeoConstants.CRS_WEB_MERCATOR);

            double minX = pt.getX() - Constants.DOCS_VECTOR_SCOPE;
            double minY = pt.getY() - Constants.DOCS_VECTOR_SCOPE;
            double maxX = pt.getX() + Constants.DOCS_VECTOR_SCOPE;
            double maxY = pt.getY() + Constants.DOCS_VECTOR_SCOPE;

            selection += " AND " + String.format(
                    Locale.US, "bbox=[%f,%f,%f,%f]", minX, minY, maxX, maxY);
        }

        return new CursorLoader(getActivity(), uri, projection, selection, null, sortOrder);
    }


    @Override
    public void onLoadFinished(
            Loader<Cursor> loader,
            Cursor data)
    {
        mAdapter.swapCursor(data);

        if (mIsInitialView) {
            if (mAdapter.getItemCount() == 0) {
                setShowAllTargets(true);
                setSwitchFilterState(false);
            }
            mIsInitialView = false;

        } else if (mShowAllTargets && mAdapter.getItemCount() == 0) {
            mHandler.sendEmptyMessage(MSG_PRESS_SKIP);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mAdapter.swapCursor(null);
    }


    protected void onSelectTargeting()
    {
        Integer id = mAdapter.getCurrentSingleSelectedItemId();


        Cursor cursor = mAdapter.getItem(id);

        String objectId =
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_FV_OBJECTID));

        if (null != mOnSelectTargetListener) {
            mOnSelectTargetListener.onSelectTarget(objectId);
        }
    }


    protected void onCancelTargeting()
    {
        if (null != mOnSelectTargetListener) {
            mOnSelectTargetListener.onSelectTarget(NOT_SELECTED);
        }
    }


    public void setOnSelectTargetListener(OnSelectTargetListener listener)
    {
        mOnSelectTargetListener = listener;
    }


    @Override
    public void onSelectionChanged(
            int position,
            boolean selection)
    {
        mButtonPositive.setEnabled(mAdapter.hasSelectedItems());
    }


    public interface OnSelectTargetListener
    {
        void onSelectTarget(String objectId);
    }
}
