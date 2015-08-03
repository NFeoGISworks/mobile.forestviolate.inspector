/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.dialog.SignDialog;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplibui.control.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Form of indictment
 */
public class IndictmentActivity extends FIActivity{
    protected DocumentEditFeature mNewFeature;
    protected DocumentsLayer mDocsLayer;
    protected EditText mIndictmentNumber;
    protected EditText mAuthor;
    protected DateTime mDateTime;
    protected Spinner mViolationTypeSpinner;
    protected Spinner mForestCatTypeSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication)getApplication();
        MapBase map = app.getMap();
        mDocsLayer = null;
        for(int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                mDocsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if(null != mDocsLayer)
            mNewFeature = new DocumentEditFeature(com.nextgis.maplib.util.Constants.NOT_FOUND, mDocsLayer.getFields());

        if(null != mNewFeature){

            app.setTempFeature(mNewFeature);

            setContentView(R.layout.activity_indictment);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String sUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");
            String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

            setToolbar(R.id.main_toolbar);
            setTitle(getText(R.string.indictment));

            mIndictmentNumber = (EditText) findViewById(R.id.indictment_num);
            mIndictmentNumber.setText(getNewNumber(sUserPassId));

            mAuthor = (EditText) findViewById(R.id.author);
            mAuthor.setText(sUserDesc + getString(R.string.passid_is) + " " + sUserPassId);

            mDateTime = (DateTime)findViewById(R.id.create_datetime);
            mDateTime.init(null, null);
            mDateTime.setCurrentDate();

            NGWLookupTable violationTypeTable = (NGWLookupTable) mDocsLayer.getLayerByName(Constants.KEY_LAYER_VIOLATE_TYPES);
            if (null != violationTypeTable) {
                Map<String, String> data = violationTypeTable.getData();
                List<String> violationTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    violationTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        violationTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mViolationTypeSpinner = (Spinner) findViewById(R.id.violation_type);
                mViolationTypeSpinner.setAdapter(adapter);
            }

            NGWLookupTable forestCatTypeTable = (NGWLookupTable) mDocsLayer.getLayerByName(Constants.KEY_LAYER_FOREST_CAT_TYPES);
            if (null != forestCatTypeTable) {
                Map<String, String> data = forestCatTypeTable.getData();
                List<String> violationTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    violationTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        violationTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mForestCatTypeSpinner = (Spinner) findViewById(R.id.forest_cat_type);
                mForestCatTypeSpinner.setAdapter(adapter);
            }

            TextView territory = (TextView) findViewById(R.id.territory);
            territory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddTerritory();
                }
            });

            // setup buttons
            Button createSheetBtn = (Button) findViewById(R.id.create_sheet);
            createSheetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFillSheet();
                }
            });

            Button createProduction = (Button) findViewById(R.id.create_production);
            createProduction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFillProduction();
                }
            });

            Button createVehicle = (Button) findViewById(R.id.create_vehicles);
            createVehicle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFillVehicle();
                }
            });

            Button createPhotoTable = (Button) findViewById(R.id.create_phototable);
            createPhotoTable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFillPhoto();
                }
            });

            Button saveAndSign = (Button) findViewById(R.id.sign_and_save);
            saveAndSign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSign();
                }
            });
        }
    }

    protected String getNewNumber(String passId){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        return passId + "/" + month + "-" + year;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.indictment_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sheet) {
            onFillSheet();
            return true;
        }
        else if (id == R.id.action_vehicle) {
            onFillVehicle();
            return true;
        }
        else if (id == R.id.action_production) {
            onFillProduction();
            return true;
        }
        else if (id == R.id.action_photo) {
            onFillPhoto();
            return true;
        }
        else if (id == R.id.action_territory) {
            onAddTerritory();
            return true;
        }
        else if (id == R.id.action_sign) {
            onSign();
            return true;
        }
        else if (id == R.id.action_cancel) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onAddTerritory() {
        Intent intent = new Intent(this, SelectTerritoryActivity.class);
        startActivity(intent);
    }

    private void onFillProduction() {

    }

    private void onSign() {
        //check required field
        if(mNewFeature.getSubFeaturesCount(Constants.KEY_LAYER_TERRITORY) == 0){
            Toast.makeText(this, getString(R.string.error_territory_mast_be_set), Toast.LENGTH_LONG).show();
            return;
        }

        //number
        String sNumber = mIndictmentNumber.getText().toString();
        if(TextUtils.isEmpty(sNumber)){
            Toast.makeText(this, getString(R.string.error_number_mast_be_set), Toast.LENGTH_LONG).show();
            return;
        }
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_NUMBER, sNumber);

        //user
        String sAuthor =  mAuthor.getText().toString();
        if(TextUtils.isEmpty(sNumber)){
            Toast.makeText(this, getString(R.string.error_author_mast_be_set), Toast.LENGTH_LONG).show();
            return;
        }
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_AUTHOR, sAuthor);

        //date
        long nDate = (long) mDateTime.getValue();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DATE, nDate);

        EditText place = (EditText) findViewById(R.id.place);
        String sPlace = place.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_PLACE, sPlace);

        String sViolationType = mViolationTypeSpinner.getSelectedItem().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE, sViolationType);

        EditText law = (EditText) findViewById(R.id.code_num);
        String sLaw = law.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_LAW, sLaw);

        String sForestCatType = mForestCatTypeSpinner.getSelectedItem().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE, sForestCatType);

        EditText who = (EditText) findViewById(R.id.who);
        String sWho = who.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_USER_PICK, sWho);

        EditText when = (EditText) findViewById(R.id.when);
        String sWhen = when.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DATE_VIOLATE, sWhen);

        EditText crime = (EditText) findViewById(R.id.crime);
        String sCrime = crime.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_CRIME, sCrime);

        EditText people_info = (EditText) findViewById(R.id.detector_say);
        String sPeopleInfo = people_info.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DESC_DETECTOR, sPeopleInfo);

        EditText crime_say = (EditText) findViewById(R.id.crime_say);
        String sCrimeSay = crime_say.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DESC_CRIME, sCrimeSay);

        EditText people_say = (EditText) findViewById(R.id.author_say);
        String sPeopleSay = people_say.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DESC_AUTHOR, sPeopleSay);

        EditText description = (EditText) findViewById(R.id.description);
        String sDescription = description.getText().toString();
        mNewFeature.setFieldValue(Constants.FIELD_DOCUMENTS_DESCRIPTION, sDescription);

        //show dialog with sign and save / edit buttons
        SignDialog signDialog = new SignDialog();
        signDialog.show(getSupportFragmentManager(), "sign_dialog");
    }

    private void onFillPhoto() {

    }

    private void onFillVehicle() {

    }

    private void onFillSheet() {

    }
}
