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
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.InputFilterMinMaxInteger;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;


public class SheetListFillerDialog
        extends ListFillerDialog
{
    protected String mUnit;
    protected String mSpecies;
    protected String mCategory;
    protected String mThickness;
    protected String mHeight;
    protected String mCount;

    protected EditText         mUnitView;
    protected AppCompatSpinner mSpeciesView;
    protected AppCompatSpinner mCategoryView;
    protected AppCompatSpinner mThicknessView;
    protected AppCompatSpinner mHeightView;
    protected EditText         mCountView;

    protected ArrayAdapter<String> mSpeciesAdapter;
    protected ArrayAdapter<String> mCategoryAdapter;
    protected ArrayAdapter<String> mThicknessAdapter;
    protected ArrayAdapter<String> mHeightAdapter;


    @Override
    protected int getDialogViewResId()
    {
        return R.layout.dialog_sheet_list_filler;
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_SHEET;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != mFeature) {
            mUnit = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_UNIT);
            mSpecies = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_SPECIES);
            mCategory = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_CATEGORY);
            mThickness = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_THICKNESS);
            mHeight = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_HEIGHTS);
            mCount = mFeature.getFieldValueAsString(Constants.FIELD_SHEET_COUNT);
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
            mCategoryAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_TREES_TYPES, false);
            mThicknessAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_THICKNESS_TYPES, true);
            mHeightAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_HEIGHT_TYPES, true);
        }
    }


    @Override
    protected void setFieldViews(View parentView)
    {
        mUnitView = (EditText) parentView.findViewById(R.id.unit);
        if (null != mUnit) {
            mUnitView.setText(mUnit);
            mUnit = null;
        }

        mSpeciesView = (AppCompatSpinner) parentView.findViewById(R.id.species);
        mSpeciesView.setAdapter(mSpeciesAdapter);
        if (null != mSpecies) {
            setListSelection(mSpeciesView, mSpeciesAdapter, mSpecies);
            mSpecies = null;
        }

        mCategoryView = (AppCompatSpinner) parentView.findViewById(R.id.category);
        mCategoryView.setAdapter(mCategoryAdapter);
        if (null != mCategory) {
            setListSelection(mCategoryView, mCategoryAdapter, mCategory);
            mCategory = null;
        }

        mThicknessView = (AppCompatSpinner) parentView.findViewById(R.id.thickness);
        mThicknessView.setAdapter(mThicknessAdapter);
        if (null != mThickness) {
            setListSelection(mThicknessView, mThicknessAdapter, mThickness);
            mThickness = null;
        }

        mHeightView = (AppCompatSpinner) parentView.findViewById(R.id.height);
        mHeightView.setAdapter(mHeightAdapter);
        if (null != mHeight) {
            setListSelection(mHeightView, mHeightAdapter, mHeight);
            mHeight = null;
        }

        mCountView = (EditText) parentView.findViewById(R.id.count);
        mCountView.setFilters(new InputFilter[] {new InputFilterMinMaxInteger(1, 1000)});
        if (null != mCount) {
            mCountView.setText(mCount);
            mCount = null;
        }
    }


    @Override
    protected void setFeatureFieldsValues(Feature feature)
    {
        Integer thicknessValue = Integer.parseInt(mThicknessView.getSelectedItem().toString());

        Integer countValue = TextUtils.isEmpty(mCountView.getText())
                             ? 1
                             : Integer.parseInt(mCountView.getText().toString());

        feature.setFieldValue(
                Constants.FIELD_SHEET_UNIT, mUnitView.getText().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_SPECIES, mSpeciesView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_CATEGORY, mCategoryView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_THICKNESS, thicknessValue);
        feature.setFieldValue(
                Constants.FIELD_SHEET_HEIGHTS, mHeightView.getSelectedItem().toString());
        feature.setFieldValue(
                Constants.FIELD_SHEET_COUNT, countValue);
    }
}
