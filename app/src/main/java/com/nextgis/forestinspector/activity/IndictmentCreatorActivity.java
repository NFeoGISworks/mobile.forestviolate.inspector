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
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplibui.control.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Form of indictment
 */
public class IndictmentCreatorActivity
        extends DocumentCreatorActivity
{
    protected EditText mWhen;
    protected EditText mLaw;
    protected EditText mWho;
    protected EditText mCreationPlace;
    protected EditText mCrime;
    protected EditText mCrimeSay;
    protected EditText mDetectorSay;
    protected EditText mAuthorSay;
    protected EditText mDescription;
    protected DateTime mDatePick;
    protected Spinner  mViolationTypeSpinner;
    protected Spinner  mForestCatTypeSpinner;


    @Override
    protected int getActivityCode()
    {
        return Constants.INDICTMENT_ACTIVITY;
    }


    @Override
    protected int getDocType()
    {
        return Constants.DOC_TYPE_INDICTMENT;
    }


    @Override
    protected int getContentViewRes()
    {
        return R.layout.activity_indictment_creator;
    }


    @Override
    protected CharSequence getTitleString()
    {
        return getText(R.string.indictment);
    }


    @Override
    protected int getMenuRes()
    {
        return R.menu.indictment_creator;
    }


    @Override
    protected void setControlViews()
    {
        if (null != mEditFeature) {

            mCreationPlace = (EditText) findViewById(R.id.creation_place);
            mLaw = (EditText) findViewById(R.id.code_num);

            mDatePick = (DateTime) findViewById(R.id.date_pick);
            mDatePick.init(null, null, null);

            mWho = (EditText) findViewById(R.id.who);
            mWhen = (EditText) findViewById(R.id.when);
            mCrime = (EditText) findViewById(R.id.crime);
            mCrimeSay = (EditText) findViewById(R.id.crime_say);
            mDetectorSay = (EditText) findViewById(R.id.detector_say);
            mAuthorSay = (EditText) findViewById(R.id.author_say);
            mDescription = (EditText) findViewById(R.id.description);

            NGWLookupTable violationTypeTable =
                    (NGWLookupTable) mDocsLayer.getLayerByName(Constants.KEY_LAYER_VIOLATE_TYPES);
            if (null != violationTypeTable) {
                Map<String, String> data = violationTypeTable.getData();
                List<String> typeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    typeArray.add(entry.getKey());
                }
                Collections.sort(typeArray);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, typeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mViolationTypeSpinner = (Spinner) findViewById(R.id.violation_type);
                mViolationTypeSpinner.setAdapter(adapter);
            }

            NGWLookupTable forestCatTypeTable = (NGWLookupTable) mDocsLayer.getLayerByName(
                    Constants.KEY_LAYER_FOREST_CAT_TYPES);
            if (null != forestCatTypeTable) {
                Map<String, String> data = forestCatTypeTable.getData();
                List<String> typeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    typeArray.add(entry.getKey());
                }
                Collections.sort(typeArray);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, typeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mForestCatTypeSpinner = (Spinner) findViewById(R.id.forest_cat_type);
                mForestCatTypeSpinner.setAdapter(adapter);
            }

            Button createSheet = (Button) findViewById(R.id.create_sheet);
            createSheet.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillSheet();
                        }
                    });

            Button createProduction = (Button) findViewById(R.id.create_production);
            createProduction.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillProduction();
                        }
                    });

            Button createVehicle = (Button) findViewById(R.id.create_vehicles);
            createVehicle.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillVehicle();
                        }
                    });

            Button createPhotoTable = (Button) findViewById(R.id.create_phototable);
            createPhotoTable.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillPhotoTable();
                        }
                    });

            if (mIsNewTempFeature) {
                mDatePick.setCurrentDate();
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPhotoAttaches(mEditFeature);
    }


    protected void saveControlsToFeature()
    {
        if (null == mEditFeature) {
            return;
        }

        super.saveControlsToFeature();

        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_PLACE, mCreationPlace.getText().toString());

        if (null != mViolationTypeSpinner) {
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_VIOLATION_TYPE,
                    mViolationTypeSpinner.getSelectedItem().toString());
        }

        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_LAW, mLaw.getText().toString());

        if (null != mForestCatTypeSpinner) {
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE,
                    mForestCatTypeSpinner.getSelectedItem().toString());
        }

        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_AUTHOR, mAuthorSay.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESCRIPTION, mDescription.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_CRIME, mCrime.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE_VIOLATE, mWhen.getText().toString());
        //mEditFeature.setFieldValue(
        //        Constants.FIELD_DOCUMENTS_USER_TRANS, );
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE_PICK, mDatePick.getValue());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_USER_PICK, mWho.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_DETECTOR, mDetectorSay.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_CRIME, mCrimeSay.getText().toString());
    }


    protected void restoreControlsFromFeature()
    {
        if (null == mEditFeature) {
            return;
        }

        super.restoreControlsFromFeature();

        mCreationPlace.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_PLACE));

        ArrayAdapter<String> adapter;
        if (null != mViolationTypeSpinner) {
            String violationType =
                    (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE);
            adapter = (ArrayAdapter<String>) mViolationTypeSpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String adapterVal = adapter.getItem(i);
                if (adapterVal.equals(violationType)) {
                    mViolationTypeSpinner.setSelection(i);
                    break;
                }
            }
        }

        mLaw.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_LAW));

        if (null != mForestCatTypeSpinner) {
            String forestCat =
                    (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE);
            adapter = (ArrayAdapter<String>) mForestCatTypeSpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String adapterVal = adapter.getItem(i);
                if (adapterVal.equals(forestCat)) {
                    mForestCatTypeSpinner.setSelection(i);
                    break;
                }
            }
        }

        mAuthorSay.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));
        mDescription.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESCRIPTION));
        mCrime.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_CRIME));
        mWhen.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE_VIOLATE));
        mDatePick.setValue(
                mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE_PICK));
        mWho.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_USER_PICK));
        mDetectorSay.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));
        mCrimeSay.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_CRIME));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_sheet:
                fillSheet();
                return true;

            case R.id.action_vehicle:
                fillVehicle();
                return true;

            case R.id.action_production:
                fillProduction();
                return true;

            case R.id.action_photo:
                fillPhotoTable();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void fillSheet()
    {
        Intent intent = new Intent(this, SheetFillerActivity.class);
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        startActivity(intent);
    }


    private void fillProduction()
    {
        Intent intent = new Intent(this, ProductionFillerActivity.class);
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        startActivity(intent);
    }


    private void fillVehicle()
    {
        Intent intent = new Intent(this, VehicleFillerActivity.class);
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        startActivity(intent);
    }


    private void fillPhotoTable()
    {
        Intent intent = new Intent(this, PhotoTableFillerActivity.class);
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        startActivity(intent);
    }
}
