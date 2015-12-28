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
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplibui.control.DateTime;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;

import static com.nextgis.maplib.util.Constants.TAG;


/**
 * Form of indictment
 */
public class FieldWorksCreatorActivity
        extends FIActivity
        implements TargetingDialog.OnSelectListener
{
    protected static final int FIELD_WORKS_ACTIVITY = 1104;

    protected DocumentEditFeature mNewFeature;
    protected DocumentsLayer      mDocsLayer;

    protected EditText mIndictmentNumber;
    protected EditText mAuthor, mWhen, mLaw;
    protected EditText mWho, mPlace, mCrime, mCrimeSay;
    protected EditText mDetectorSay, mAuthorSay, mDescription;
    protected DateTime mDateTime;
    protected DateTime mDatePick;
    protected Spinner  mViolationTypeSpinner;
    protected Spinner  mForestCatTypeSpinner;
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

            if (null != mNewFeature && Constants.DOC_TYPE_INDICTMENT != mNewFeature.getFieldValue(
                    Constants.FIELD_DOCUMENTS_TYPE)) {
                app.setTempFeature(null);
                mNewFeature = null;
            }

            if (mNewFeature == null) {
                mNewFeature = new DocumentEditFeature(
                        com.nextgis.maplib.util.Constants.NOT_FOUND, mDocsLayer.getFields());
                app.setTempFeature(mNewFeature);

                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_TYPE, Constants.DOC_TYPE_INDICTMENT);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_STATUS, Constants.DOCUMENT_STATUS_SEND);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOC_ID, com.nextgis.maplib.util.Constants.NOT_FOUND);

                loadPhotoAttaches(mNewFeature);
            }
        }

        if (null != mNewFeature) {

            setContentView(R.layout.activity_field_works_creator);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            mUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");
            String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

            setToolbar(R.id.main_toolbar);
            setTitle(getText(R.string.field_works_title));

/*
            mIndictmentNumber = (EditText) findViewById(R.id.doc_num);
            mIndictmentNumber.setText(getNewNumber(sUserPassId));

            mAuthor = (EditText) findViewById(R.id.author);
            mAuthor.setText(mUserDesc + getString(R.string.passid_is) + " " + sUserPassId);

            mDateTime = (DateTime) findViewById(R.id.creation_datetime);
            mDateTime.init(null, null, null);
            mDateTime.setCurrentDate();

            mPlace = (EditText) findViewById(R.id.place);
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
                List<String> violationTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    violationTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, violationTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mViolationTypeSpinner = (Spinner) findViewById(R.id.violation_type);
                mViolationTypeSpinner.setAdapter(adapter);
            }

            NGWLookupTable forestCatTypeTable = (NGWLookupTable) mDocsLayer.getLayerByName(
                    Constants.KEY_LAYER_FOREST_CAT_TYPES);
            if (null != forestCatTypeTable) {
                Map<String, String> data = forestCatTypeTable.getData();
                List<String> forestCatTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    forestCatTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, forestCatTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mForestCatTypeSpinner = (Spinner) findViewById(R.id.forest_cat_type);
                mForestCatTypeSpinner.setAdapter(adapter);
            }

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
*/
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
/*
        if (null != mNewFeature) {
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_NUMBER, mIndictmentNumber.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DATE, mDateTime.getValue());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_AUTHOR, mAuthor.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_PLACE, mPlace.getText().toString());

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
                    Constants.FIELD_DOCUMENTS_USER, mUserDesc);
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DATE_PICK, mDatePick.getValue());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_USER_PICK, mWho.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DESC_DETECTOR, mDetectorSay.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_DESC_CRIME, mCrimeSay.getText().toString());
            mNewFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_TERRITORY, mTerritory.getText().toString());
        }
*/
    }


    protected void restoreControlsFromFeature()
    {
/*
        if (null == mNewFeature) {
            return;
        }

        mIndictmentNumber.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_NUMBER));
        mDateTime.setValue(
                mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE));
        mAuthor.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_AUTHOR));
        mPlace.setText(
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

        mLaw.setText((String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_LAW));
        if (null != mViolationTypeSpinner) {
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
        mTerritory.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY));
*/
    }


    protected void loadPhotoAttaches(DocumentEditFeature feature)
    {
        MainApplication app = (MainApplication) getApplicationContext();
        File photoDir = app.getDocFeatureFolder();

        if (!photoDir.isDirectory()) {
            throw new IllegalArgumentException("photoDir is not directory");
        }

        File[] photoFiles = photoDir.listFiles(
                new FilenameFilter()
                {
                    @Override
                    public boolean accept(
                            final File dir,
                            final String name)
                    {
                        Log.d(
                                TAG, "loadPhotoAttaches(), FilenameFilter, dir: " +
                                     dir.getAbsolutePath() + ", name: " + name);

                        if (name.matches(".*\\.jpg")) {
                            Log.d(
                                    TAG,
                                    "loadPhotoAttaches(), FilenameFilter, name.matches: " + true);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

        for (File photoFile : photoFiles) {
            AttachItem photoAttach = new AttachItem("-1", photoFile.getName(), "image/jpeg", "");
            feature.addAttachment(photoAttach);
        }
    }


    protected String getNewNumber(String passId)
    {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        return passId + "/" + month + "-" + year;
    }


/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.indictment_creator, menu);
        return true;
    }
*/


/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sheet) {
            onFillSheet();
            return true;
        } else if (id == R.id.action_vehicle) {
            onFillVehicle();
            return true;
        } else if (id == R.id.action_production) {
            onFillProduction();
            return true;
        } else if (id == R.id.action_photo) {
            onFillPhoto();
            return true;
        } else if (id == R.id.action_territory) {
            onAddTerritory();
            return true;
        } else if (id == R.id.action_sign) {
            onSign();
            return true;
        } else if (id == R.id.action_cancel) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == FIELD_WORKS_ACTIVITY) {
            mTerritory.setText(
                    mNewFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));
        }
    }


    private void onAddTerritory()
    {
        Intent intent = new Intent(this, SelectTerritoryActivity.class);
        startActivityForResult(intent, FIELD_WORKS_ACTIVITY);
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
        if (TextUtils.isEmpty(mIndictmentNumber.getText().toString())) {
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
