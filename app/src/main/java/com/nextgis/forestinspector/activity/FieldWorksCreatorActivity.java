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
import java.util.List;
import java.util.Map;


/**
 * Form of field works doc
 */
public class FieldWorksCreatorActivity
        extends DocumentCreatorActivity
{
    protected EditText mCreationPlace;
    protected EditText mRepresentative;
    protected Spinner  mFieldWorkTypeSpinner;
    protected EditText mContract;
    protected Spinner  mContractTypeSpinner;
    protected DateTime mContractDate;
    protected EditText mContractNumber;
    protected EditText mCuttingAreaCleanQuality;
    protected EditText mCuttingAreaCultivationQuality;
    protected EditText mViolation;
    protected EditText mInfringerOrganisationName;
    protected EditText mInfringerFullName;
    protected EditText mInfringerLivingPlace;


    @Override
    protected int getActivityCode()
    {
        return Constants.FIELD_WORKS_ACTIVITY;
    }


    @Override
    protected int getDocType()
    {
        return Constants.DOC_TYPE_FIELD_WORKS;
    }


    @Override
    protected int getContentViewRes()
    {
        return R.layout.activity_field_works_creator;
    }


    @Override
    protected CharSequence getTitleString()
    {
        return getText(R.string.field_works_title);
    }


    @Override
    protected int getMenuRes()
    {
        return R.menu.field_works_creator;
    }


    @Override
    protected void setControlViews()
    {
        if (null != mNewFeature) {

            mCreationPlace = (EditText) findViewById(R.id.creation_place);
            mRepresentative = (EditText) findViewById(R.id.representative);

            NGWLookupTable fieldworkTypeTable =
                    (NGWLookupTable) mDocsLayer.getLayerByName(Constants.KEY_LAYER_FIELDWORK_TYPES);
            if (null != fieldworkTypeTable) {
                Map<String, String> data = fieldworkTypeTable.getData();
                List<String> typeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    typeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, typeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mFieldWorkTypeSpinner = (Spinner) findViewById(R.id.field_work_type);
                mFieldWorkTypeSpinner.setAdapter(adapter);
            }

            mContract = (EditText) findViewById(R.id.contract);

            NGWLookupTable controlAimTypeTable = (NGWLookupTable) mDocsLayer.getLayerByName(
                    Constants.KEY_LAYER_CONTRACT_TYPES);
            if (null != controlAimTypeTable) {
                Map<String, String> data = controlAimTypeTable.getData();
                List<String> typeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    typeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, typeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mContractTypeSpinner = (Spinner) findViewById(R.id.contract_type);
                mContractTypeSpinner.setAdapter(adapter);
            }

            // TODO: request only date
            mContractDate = (DateTime) findViewById(R.id.contract_date);
            mContractDate.init(null, null, null);
            mContractDate.setCurrentDate();

            mContractNumber = (EditText) findViewById(R.id.contract_number);
            mCuttingAreaCleanQuality = (EditText) findViewById(R.id.cutting_area_clean_quality);
            mCuttingAreaCultivationQuality =
                    (EditText) findViewById(R.id.cutting_area_cultivation_quality);
            mViolation = (EditText) findViewById(R.id.violation);

            Button createPhotoTable = (Button) findViewById(R.id.create_photo_table);
            createPhotoTable.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillPhotoTable();
                        }
                    });

            mInfringerOrganisationName = (EditText) findViewById(R.id.infringer_organisation_name);
            mInfringerFullName = (EditText) findViewById(R.id.infringer_full_name);
            mInfringerLivingPlace = (EditText) findViewById(R.id.infringer_living_place);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        loadPhotoAttaches(mNewFeature);
    }


    @Override
    protected void saveControlsToFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        super.saveControlsToFeature();

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_PLACE, mCreationPlace.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_USER_TRANS, mRepresentative.getText().toString());

        if (null != mFieldWorkTypeSpinner) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE,
                    mFieldWorkTypeSpinner.getSelectedItem().toString());
        }

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_USER_PICK, mContract.getText().toString());

        if (null != mContractTypeSpinner) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DATE_VIOLATE, // not DATE, is String
                    mContractTypeSpinner.getSelectedItem().toString());
        }

//        mNewFeature.setFieldValue( // TODO new filed, type DATE
//                                   Constants., mContractDate.getValue());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_LAW, mContractNumber.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_DETECTOR,
                mCuttingAreaCleanQuality.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_AUTHOR,
                mCuttingAreaCultivationQuality.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_VIOLATION_TYPE, mViolation.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESC_CRIME,
                mInfringerOrganisationName.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_CRIME, mInfringerFullName.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DESCRIPTION, mInfringerLivingPlace.getText().toString());
    }


    @Override
    protected void restoreControlsFromFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        super.restoreControlsFromFeature();

        mCreationPlace.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_PLACE));
        mRepresentative.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_USER_TRANS));

        ArrayAdapter<String> adapter;
        if (null != mFieldWorkTypeSpinner) {
            String violationType =
                    (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE);
            adapter = (ArrayAdapter<String>) mFieldWorkTypeSpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String adapterVal = adapter.getItem(i);
                if (adapterVal.equals(violationType)) {
                    mFieldWorkTypeSpinner.setSelection(i);
                    break;
                }
            }
        }

        mContract.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_USER_PICK));

        if (null != mContractTypeSpinner) {
            String forestCat =
                    (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE_VIOLATE);
            adapter = (ArrayAdapter<String>) mContractTypeSpinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String adapterVal = adapter.getItem(i);
                if (adapterVal.equals(forestCat)) {
                    mContractTypeSpinner.setSelection(i);
                    break;
                }
            }
        }

//        mContractDate.setValue( // TODO new filed, type DATE
//                mNewFeature.getFieldValue(Constants.));
        mContractNumber.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_LAW));
        mCuttingAreaCleanQuality.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));
        mCuttingAreaCultivationQuality.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));
        mViolation.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE));
        mInfringerOrganisationName.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESC_CRIME));
        mInfringerFullName.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_CRIME));
        mInfringerLivingPlace.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DESCRIPTION));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_photo:
                fillPhotoTable();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void fillPhotoTable()
    {
        Intent intent = new Intent(this, PhotoTableFillerActivity.class);
        startActivity(intent);
    }
}
