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
        if (null != mNewFeature) {

            mCreationPlace = (EditText) findViewById(R.id.creation_place);
            mLaw = (EditText) findViewById(R.id.code_num);

            mDatePick = (DateTime) findViewById(R.id.date_pick);
            mDatePick.init(null, null, null);
            mDatePick.setCurrentDate();

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
                            onFillSheet();
                        }
                    });

            Button createProduction = (Button) findViewById(R.id.create_production);
            createProduction.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onFillProduction();
                        }
                    });

            Button createVehicle = (Button) findViewById(R.id.create_vehicles);
            createVehicle.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onFillVehicle();
                        }
                    });

            Button createPhotoTable = (Button) findViewById(R.id.create_phototable);
            createPhotoTable.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onFillPhoto();
                        }
                    });
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPhotoAttaches(mNewFeature);
    }


    protected void saveControlsToFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        super.saveControlsToFeature();

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_PLACE, mCreationPlace.getText().toString());

        if (null != mViolationTypeSpinner) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_VIOLATION_TYPE,
                    mViolationTypeSpinner.getSelectedItem().toString());
        }

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_LAW, mLaw.getText().toString());

        if (null != mForestCatTypeSpinner) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE,
                    mForestCatTypeSpinner.getSelectedItem().toString());
        }

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_AUTHOR, mAuthorSay.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESCRIPTION, mDescription.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_CRIME, mCrime.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE_VIOLATE, mWhen.getText().toString());
        //mNewFeature.setFieldValue(
        //        Constants.FIELD_DOCUMENTS_USER_TRANS, );
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE_PICK, mDatePick.getValue());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_USER_PICK, mWho.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_DETECTOR, mDetectorSay.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_CRIME, mCrimeSay.getText().toString());
    }


    protected void restoreControlsFromFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        super.restoreControlsFromFeature();

        mCreationPlace.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_PLACE));

        ArrayAdapter<String> adapter;
        if (null != mViolationTypeSpinner) {
            String violationType =
                    (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE);
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
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_LAW));

        if (null != mForestCatTypeSpinner) {
            String forestCat =
                    (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE);
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
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));
        mDescription.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESCRIPTION));
        mCrime.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_CRIME));
        mWhen.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE_VIOLATE));
        mDatePick.setValue(
                mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE_PICK));
        mWho.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_USER_PICK));
        mDetectorSay.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));
        mCrimeSay.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_CRIME));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_sheet:
                onFillSheet();
                return true;

            case R.id.action_vehicle:
                onFillVehicle();
                return true;

            case R.id.action_production:
                onFillProduction();
                return true;

            case R.id.action_photo:
                onFillPhoto();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void onFillSheet()
    {
        Intent intent = new Intent(this, SheetFillerActivity.class);
        startActivity(intent);
    }


    private void onFillProduction()
    {
        Intent intent = new Intent(this, ProductionFillerActivity.class);
        startActivity(intent);
    }


    private void onFillVehicle()
    {
        Intent intent = new Intent(this, VehicleFillerActivity.class);
        startActivity(intent);
    }


    private void onFillPhoto()
    {
        Intent intent = new Intent(this, PhotoTableFillerActivity.class);
        startActivity(intent);
    }
}
