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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;


public class VehicleFillerDialog
        extends ListFillerDialog
{
    protected String mName;
    protected String mDesc;
    protected String mNums;
    protected String mUser;

    protected EditText mNameView;
    protected EditText mDescView;
    protected EditText mNumsView;
    protected EditText mUserView;


    @Override
    protected int getDialogViewResId()
    {
        return R.layout.dialog_vehicle_fill;
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_VEHICLES;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != mFeature) {
            mName = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_NAME);
            mDesc = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_DESCRIPTION);
            mNums = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_ENGINE_NUM);
            mUser = mFeature.getFieldValueAsString(Constants.FIELD_VEHICLE_USER);
        }
    }


    @Override
    protected void setFieldViews(View parentView)
    {
        mNameView = (EditText) parentView.findViewById(R.id.name);
        if (null != mName) {
            mNameView.setText(mName);
            mName = null;
        }

        mDescView = (EditText) parentView.findViewById(R.id.desc);
        if (null != mDesc) {
            mDescView.setText(mDesc);
            mDesc = null;
        }

        mNumsView = (EditText) parentView.findViewById(R.id.nums);
        if (null != mNums) {
            mNumsView.setText(mNums);
            mNums = null;
        }

        mUserView = (EditText) parentView.findViewById(R.id.user);
        if (null != mUser) {
            mUserView.setText(mUser);
            mUser = null;
        }
    }


    @Override
    protected boolean isCorrectValues()
    {
        if (!super.isCorrectValues()) {
            return false;
        }

        if (TextUtils.isEmpty(mNameView.getText().toString()) ||
            TextUtils.isEmpty(mDescView.getText().toString()) ||
            TextUtils.isEmpty(mNumsView.getText().toString()) ||
            TextUtils.isEmpty(mUserView.getText().toString())) {

            Toast.makeText(
                    getActivity(), getString(R.string.error_invalid_input), Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        return true;
    }


    @Override
    protected void setFeatureFieldsValues(Feature feature)
    {
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_NAME, mNameView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_DESCRIPTION, mDescView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_ENGINE_NUM, mNumsView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_VEHICLE_USER, mUserView.getText().toString());
    }
}
