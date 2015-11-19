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
import android.support.v7.widget.AppCompatSpinner;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.InputFilterMinMaxDouble;
import com.nextgis.forestinspector.util.InputFilterMinMaxInteger;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;


public class ProductionListFillerDialog
        extends ListFillerDialog
{
    protected String mSpecies;
    protected String mType;
    protected String mLength;
    protected String mThickness;
    protected String mCount;

    protected AppCompatSpinner mSpeciesView;
    protected AppCompatSpinner mTypeView;
    protected EditText         mLengthView;
    protected AppCompatSpinner mThicknessView;
    protected EditText         mCountView;

    protected ArrayAdapter<String> mSpeciesAdapter;
    protected ArrayAdapter<String> mTypeAdapter;
    protected ArrayAdapter<String> mThicknessAdapter;


    @Override
    protected int getDialogViewResId()
    {
        return R.layout.dialog_production_list_filler;
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_PRODUCTION;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != mFeature) {
            mSpecies = mFeature.getFieldValueAsString(Constants.FIELD_PRODUCTION_SPECIES);
            mType = mFeature.getFieldValueAsString(Constants.FIELD_PRODUCTION_TYPE);
            mLength = mFeature.getFieldValueAsString(Constants.FIELD_PRODUCTION_LENGTH);
            mThickness = mFeature.getFieldValueAsString(Constants.FIELD_PRODUCTION_THICKNESS);
            mCount = mFeature.getFieldValueAsString(Constants.FIELD_PRODUCTION_COUNT);
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer docs = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docs = (DocumentsLayer) layer;
                break;
            }
        }

        if (null != docs) {
            mSpeciesAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_SPECIES_TYPES, false);
            mTypeAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_TREES_TYPES, false);
            mThicknessAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_THICKNESS_TYPES, true);
        }
    }


    @Override
    protected void setFieldViews(View parentView)
    {
        mSpeciesView = (AppCompatSpinner) parentView.findViewById(R.id.species);
        mSpeciesView.setAdapter(mSpeciesAdapter);
        if (null != mSpecies) {
            setListSelection(mSpeciesView, mSpeciesAdapter, mSpecies);
            mSpecies = null;
        }

        mTypeView = (AppCompatSpinner) parentView.findViewById(R.id.type);
        mTypeView.setAdapter(mTypeAdapter);
        if (null != mType) {
            setListSelection(mTypeView, mTypeAdapter, mType);
            mType = null;
        }

        mLengthView = (EditText) parentView.findViewById(R.id.length);
        mLengthView.setFilters(new InputFilter[] {new InputFilterMinMaxDouble(0.0, 200.0)});
        if (null != mLength) {
            mLengthView.setText(mLength);
            mLength = null;
        }

        mThicknessView = (AppCompatSpinner) parentView.findViewById(R.id.thickness);
        mThicknessView.setAdapter(mThicknessAdapter);
        if (null != mThickness) {
            setListSelection(mThicknessView, mThicknessAdapter, mThickness);
            mThickness = null;
        }

        mCountView = (EditText) parentView.findViewById(R.id.count);
        mCountView.setFilters(new InputFilter[] {new InputFilterMinMaxInteger(1, 10000)});
        if (null != mCount) {
            mCountView.setText(mCount);
            mCount = null;
        }
    }


    @Override
    protected boolean isCorrectValues()
    {
        if (!super.isCorrectValues()) {
            return false;
        }

        if (TextUtils.isEmpty(mLengthView.getText().toString())) {
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
        Double lengthValue = Double.parseDouble(mLengthView.getText().toString());

        Double thicknessValue = Double.parseDouble(mThicknessView.getSelectedItem().toString());

        Integer countValue = TextUtils.isEmpty(mCountView.getText())
                             ? 1
                             : Integer.parseInt(mCountView.getText().toString());

        feature.setFieldValue(
                Constants.FIELD_PRODUCTION_SPECIES, mSpeciesView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_PRODUCTION_TYPE, mTypeView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_PRODUCTION_LENGTH, lengthValue);
        feature.setFieldValue(
                Constants.FIELD_PRODUCTION_THICKNESS, thicknessValue);
        feature.setFieldValue(
                Constants.FIELD_PRODUCTION_COUNT, countValue);
    }
}
