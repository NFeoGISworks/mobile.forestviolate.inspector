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

import android.accounts.Account;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.NoteCreatorActivity;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplibui.control.DateTime;


public class NoteListFillerDialog
        extends ListFillerDialog
        implements NoteCreatorActivity.OnCreateNoteListener
{
    protected Object mStartDateTime;
    protected Object mEndDateTime;
    protected String mNoteText;

    protected DateTime mStartDateTimeView;
    protected DateTime mEndDateTimeView;
    protected EditText mNoteTextView;


    @Override
    protected int getDialogViewResId()
    {
        return R.layout.dialog_note_list_filler;
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_NOTES;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != mFeature) {
            mStartDateTime = mFeature.getFieldValue(Constants.FIELD_NOTES_DATE_BEG);
            mEndDateTime = mFeature.getFieldValue(Constants.FIELD_NOTES_DATE_END);
            mNoteText = (String) mFeature.getFieldValue(Constants.FIELD_NOTES_DESCRIPTION);
        }
    }


    @Override
    protected void setFieldViews(View parentView)
    {
        mStartDateTimeView = (DateTime) parentView.findViewById(R.id.start_datetime);
        mStartDateTimeView.init(null, null, null);
        if (null != mStartDateTime) {
            mStartDateTimeView.setValue(mStartDateTime);
            mStartDateTime = null;
        } else {
            mStartDateTimeView.setCurrentDate();
        }

        mEndDateTimeView = (DateTime) parentView.findViewById(R.id.end_datetime);
        mEndDateTimeView.init(null, null, null);
        if (null != mEndDateTime) {
            mEndDateTimeView.setValue(mEndDateTime);
            mEndDateTime = null;

        } else {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            String noteInitTerm =
                    sharedPreferences.getString(SettingsConstants.KEY_PREF_NOTE_INITIAL_TERM, "1");

            final long WEEK_MILLIS = 3600000 * 24 * 7;
            long millis;

            switch (noteInitTerm) {
                case "1":
                default:
                    millis = WEEK_MILLIS;     // 1 week
                    break;
                case "2":
                    millis = WEEK_MILLIS * 2; // 2 weeks
                    break;
                case "4":
                    millis = WEEK_MILLIS * 4; // 4 weeks
                    break;
                case "8":
                    millis = WEEK_MILLIS * 8; // 8 weeks
                    break;
            }

            Long endTime = ((Long) mStartDateTimeView.getValue()) + millis;
            mEndDateTimeView.setValue(endTime);
        }

        mNoteTextView = (EditText) parentView.findViewById(R.id.note_text);
        if (null != mNoteText) {
            mNoteTextView.setText(mNoteText);
            mNoteText = null;
        }
    }


    @Override
    public void onPause()
    {
        mStartDateTime = mStartDateTimeView.getValue();
        mEndDateTime = mEndDateTimeView.getValue();
        mNoteText = mNoteTextView.getText().toString();

        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (null != mStartDateTime) {
            mStartDateTimeView.setValue(mStartDateTime);
        }

        if (null != mEndDateTime) {
            mEndDateTimeView.setValue(mEndDateTime);
        }

        if (null != mNoteText) {
            mNoteTextView.setText(mNoteText);
        }
    }


    @Override
    protected boolean isCorrectValues()
    {
        if (!super.isCorrectValues()) {
            return false;
        }

        if (TextUtils.isEmpty(mNoteTextView.getText().toString())) {
            Toast.makeText(
                    getActivity(), getString(R.string.error_invalid_input), Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        return true;
    }


    @Override
    public void saveData()
    {
        MapBase map = MapBase.getInstance();
        VectorLayer notesLayer = (VectorLayer) map.getLayerByPathName(Constants.KEY_LAYER_NOTES);
        if (null == notesLayer) {
            return;
        }


        Feature feature;
        if (null != mFeature) {
            feature = mFeature;
        } else {
            feature = new Feature(
                    com.nextgis.maplib.util.Constants.NOT_FOUND, notesLayer.getFields());
        }


        GeoPoint pt = new GeoPoint(mLocation.getLongitude(), mLocation.getLatitude());
        pt.setCRS(GeoConstants.CRS_WGS84);
        pt.project(GeoConstants.CRS_WEB_MERCATOR);
        GeoMultiPoint geometryValue = new GeoMultiPoint();
        geometryValue.add(pt);

        feature.setGeometry(geometryValue);
        setFeatureFieldsValues(feature);

        String pathName = notesLayer.getPath().getName();
        if (null != mFeature) {
            Uri uri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" + pathName + "/" +
                    feature.getId());
            if (notesLayer.update(uri, feature.getContentValues(false), null, null) <= 0) {
                Log.d(Constants.FITAG, "update feature into " + pathName + " failed");
            }

        } else {
            Uri uri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/" + pathName);
            if (notesLayer.insert(uri, feature.getContentValues(false)) == null) {
                Log.d(Constants.FITAG, "insert feature into " + pathName + " failed");
            }
        }
    }


    @Override
    protected void setFeatureFieldsValues(Feature feature)
    {
        MainApplication app = (MainApplication) getActivity().getApplication();
        Account account = app.getAccount(getString(R.string.account_name));
        String login = app.getAccountLogin(account);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int userId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);

        feature.setFieldValue(
                Constants.FIELD_NOTES_LOGIN, login);
        feature.setFieldValue(
                Constants.FIELD_NOTES_USER_ID, userId);
        feature.setFieldValue(
                Constants.FIELD_NOTES_DATE_BEG, mStartDateTimeView.getValue());
        feature.setFieldValue(
                Constants.FIELD_NOTES_DATE_END, mEndDateTimeView.getValue());
        feature.setFieldValue(
                Constants.FIELD_NOTES_DESCRIPTION, mNoteTextView.getText().toString());
    }


    @Override
    public void onCreateNote()
    {
        addData();
    }
}
