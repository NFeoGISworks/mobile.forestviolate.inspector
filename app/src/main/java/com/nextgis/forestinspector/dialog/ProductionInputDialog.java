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
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.ProductionActivity;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.LocationUtil;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.styled_dialog.StyledDialogFragment;

import java.text.DecimalFormat;


public class ProductionInputDialog
        extends StyledDialogFragment
        implements GpsEventListener
{
    protected TextView mLatView;
    protected TextView mLongView;
    protected TextView mAltView;
    protected TextView mAccView;
    protected Location mLocation;

    protected GpsEventSource gpsEventSource;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        super.onCreate(savedInstanceState);

        IGISApplication app = (IGISApplication) getActivity().getApplication();
        gpsEventSource = app.getGpsEventSource();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_production, null);

        createLocationPanelView(view);

        final EditText species = (EditText) view.findViewById(R.id.species);
        final EditText cat = (EditText) view.findViewById(R.id.cat);
        final EditText length = (EditText) view.findViewById(R.id.length);
        final EditText thickness = (EditText) view.findViewById(R.id.thickness);
        final EditText count = (EditText) view.findViewById(R.id.count);


        setThemeDark(isAppThemeDark());

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_image_edit);
        } else {
            setIcon(R.drawable.ic_action_image_edit);
        }

        setView(view);
        setTitle(R.string.add_production);
        setNegativeText(R.string.cancel);
        setPositiveText(R.string.add);

        setOnNegativeClickedListener(
                new SheetFillDialog.OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });

        setOnPositiveClickedListener(
                new SheetFillDialog.OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        if (TextUtils.isEmpty(length.getText()) ||
                            TextUtils.isEmpty(thickness.getText()) ||
                            TextUtils.isEmpty(count.getText())) {
                            Toast.makeText(
                                    getActivity(), getString(R.string.error_invalid_input),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            onAdd(
                                    species.getText().toString(), cat.getText().toString(),
                                    Double.parseDouble(length.getText().toString()),
                                    Double.parseDouble(thickness.getText().toString()),
                                    Integer.parseInt(count.getText().toString()));
                        }
                    }
                });

        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onPause()
    {
        gpsEventSource.removeListener(this);
        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        gpsEventSource.addListener(this);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    private void onAdd(
            String species,
            String cat,
            double length,
            double thickness,
            int count)
    {
        ProductionActivity activity = (ProductionActivity) getActivity();
        if (null != activity && mLocation != null) {
            GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
            pt.setCRS(GeoConstants.CRS_WGS84);
            pt.project(GeoConstants.CRS_WEB_MERCATOR);
            GeoMultiPoint mpt = new GeoMultiPoint();
            mpt.add(pt);
            activity.addProduction(mpt, species, cat, length, thickness, count);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_no_location), Toast.LENGTH_SHORT)
                    .show();
        }
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

                        Location location = gpsEventSource.getLastKnownLocation();
                        setLocationText(location);
                    }
                });

        setLocationText(gpsEventSource.getLastKnownLocation());
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
}
