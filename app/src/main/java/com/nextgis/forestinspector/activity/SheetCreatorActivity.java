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

package com.nextgis.forestinspector.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.dialog.SignDialog;
import com.nextgis.forestinspector.dialog.TargetingDialog;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplibui.control.DateTime;

import java.util.Calendar;


/**
 * Form of sheet
 */
public class SheetCreatorActivity
        extends FIActivity
        implements TargetingDialog.OnSelectListener
{
    protected static final int SHEET_ACTIVITY = 1102;

    protected DocumentEditFeature mNewFeature;
    protected DocumentsLayer      mDocsLayer;

    protected EditText mDocNumber;
    protected EditText mAuthor;
    protected DateTime mDateTime;
    protected TextView mTerritory;
    protected String   mUserDesc;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication) getApplication();
        MapBase map = app.getMap();
        mDocsLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                mDocsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        // TODO: 04.08.15 save restore bundle new feature id to fill values, previous added

        if (null != mDocsLayer) {
            mNewFeature = app.getTempFeature();

            if (null != mNewFeature && Constants.DOC_TYPE_SHEET !=
                                       mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_TYPE)) {
                app.setTempFeature(null);
                mNewFeature = null;
            }

            if (mNewFeature == null) {
                mNewFeature = new DocumentEditFeature(
                        com.nextgis.maplib.util.Constants.NOT_FOUND, mDocsLayer.getFields());
                app.setTempFeature(mNewFeature);

                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_TYPE, Constants.DOC_TYPE_SHEET);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_STATUS, Constants.DOCUMENT_STATUS_SEND);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOC_ID, com.nextgis.maplib.util.Constants.NOT_FOUND);
            }
        }

        if (null != mNewFeature) {

            setContentView(R.layout.activity_sheet_creator);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            mUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");
            String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

            setToolbar(R.id.main_toolbar);
            setTitle(getText(R.string.sheet_title));

            mDocNumber = (EditText) findViewById(R.id.doc_num);
            mDocNumber.setText(getNewNumber(sUserPassId));

            mAuthor = (EditText) findViewById(R.id.author);
            mAuthor.setText(mUserDesc + getString(R.string.passid_is) + " " + sUserPassId);

            mDateTime = (DateTime) findViewById(R.id.create_datetime);
            mDateTime.init(null, null, null);
            mDateTime.setCurrentDate();

            mTerritory = (TextView) findViewById(R.id.territory);
            mTerritory.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onAddTerritory();
                        }
                    });

            // setup buttons
            Button createSheetBtn = (Button) findViewById(R.id.create_sheet);
            createSheetBtn.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onFillSheet();
                        }
                    });


            Button saveAndSign = (Button) findViewById(R.id.sign_and_save);
            saveAndSign.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onSign();
                        }
                    });

            saveControlsToFeature();
        }
    }


    @Override
    public void onBackPressed()
    {
        MainApplication app = (MainApplication) getApplication();
        app.setTempFeature(null);
        mNewFeature = null;

        super.onBackPressed();
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        saveControlsToFeature();
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        restoreControlsFromFeature();

        // TODO: not show dialog if 0 records

        String vector = (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_VECTOR);

        if (null == vector) {
            FragmentManager fm = getSupportFragmentManager();

            TargetingDialog fragment =
                    (TargetingDialog) fm.findFragmentByTag(Constants.FRAGMENT_TARGETING_DIALOG);

            if (fragment == null) {
                TargetingDialog dialog = new TargetingDialog();
                dialog.setOnSelectListener(this);
                dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_TARGETING_DIALOG);
            }
        }
    }


    protected void saveControlsToFeature()
    {
        if (null != mNewFeature) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_NUMBER, mDocNumber.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DATE, mDateTime.getValue());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_AUTHOR, mAuthor.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_USER, mUserDesc);
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_TERRITORY, mTerritory.getText().toString());
        }
    }


    protected void restoreControlsFromFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        mDocNumber.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_NUMBER));
        mDateTime.setValue(
                mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE));
        mAuthor.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_AUTHOR));
        mTerritory.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY));
    }


    protected String getNewNumber(String passId)
    {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        return passId + "/" + month + "-" + year;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.sheet_creator, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_sheet:
                onFillSheet();
                return true;
            case R.id.action_territory:
                onAddTerritory();
                return true;
            case R.id.action_sign:
                onSign();
                return true;
            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == SHEET_ACTIVITY) {
            mTerritory.setText(
                    mNewFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));
        }
    }


    private void onAddTerritory()
    {
        Intent intent = new Intent(this, SelectTerritoryActivity.class);
        startActivityForResult(intent, SHEET_ACTIVITY);
    }


    private void onFillSheet()
    {
        Intent intent = new Intent(this, SheetFillerActivity.class);
        startActivity(intent);
    }


    private void onSign()
    {
        //check required field
        if (mNewFeature.getGeometry() == null ||
            TextUtils.isEmpty(mTerritory.getText().toString())) {
            Toast.makeText(this, getString(R.string.error_territory_must_be_set), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        //number
        if (TextUtils.isEmpty(mDocNumber.getText().toString())) {
            Toast.makeText(this, getString(R.string.error_number_mast_be_set), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        //user
        if (TextUtils.isEmpty(mAuthor.getText().toString())) {
            Toast.makeText(this, getString(R.string.error_author_must_be_set), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (mNewFeature.getSubFeaturesCount(Constants.KEY_LAYER_SHEET) <= 0) {
            Toast.makeText(
                    this, getString(R.string.error_sheet_not_contain_entries), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        saveControlsToFeature();

        //show dialog with sign and save / edit buttons
        SignDialog signDialog = new SignDialog();
        signDialog.show(getSupportFragmentManager(), Constants.FRAGMENT_SIGN_DIALOG);
    }


    @Override
    public void onSelect(String objectId)
    {
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_VECTOR, objectId);
    }
}
