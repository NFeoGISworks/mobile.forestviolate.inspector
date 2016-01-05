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
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplibui.control.DateTime;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;

import static com.nextgis.maplib.util.Constants.TAG;


public abstract class DocumentCreatorActivity
        extends FIActivity
        implements TargetingDialog.OnSelectListener
{
    protected DocumentEditFeature mNewFeature;
    protected DocumentsLayer      mDocsLayer;

    protected String mUserDesc;

    protected EditText mDocNumber;
    protected DateTime mCreationDateTime;
    protected EditText mAuthor;
    protected TextView mTerritory;


    protected abstract int getActivityCode();

    protected abstract int getDocType();

    protected abstract int getContentViewRes();

    protected abstract CharSequence getTitleString();

    protected abstract int getMenuRes();

    protected abstract void setControlViews();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication) getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

            if (null != mNewFeature && getDocType() != mNewFeature.getFieldValue(
                    Constants.FIELD_DOCUMENTS_TYPE)) {
                app.setTempFeature(null);
                mNewFeature = null;
            }

            if (mNewFeature == null) {
                mNewFeature = new DocumentEditFeature(
                        com.nextgis.maplib.util.Constants.NOT_FOUND, mDocsLayer.getFields());
                app.setTempFeature(mNewFeature);

                long userId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
                mUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");

                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_TYPE, getDocType());
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_STATUS, Constants.DOCUMENT_STATUS_SEND);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOC_ID, com.nextgis.maplib.util.Constants.NOT_FOUND);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_USER_ID, userId);
                mNewFeature.setFieldValue(
                        Constants.FIELD_DOCUMENTS_USER, mUserDesc);
            }
        }

        if (null != mNewFeature) {

            setContentView(getContentViewRes());

            String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

            setToolbar(R.id.main_toolbar);
            setTitle(getTitleString());

            mDocNumber = (EditText) findViewById(R.id.doc_num);
            mDocNumber.setText(getNewNumber(sUserPassId));

            mCreationDateTime = (DateTime) findViewById(R.id.creation_datetime);
            mCreationDateTime.init(null, null, null);
            mCreationDateTime.setCurrentDate();

            mAuthor = (EditText) findViewById(R.id.author);
            mAuthor.setText(mUserDesc + getString(R.string.passid_is) + " " + sUserPassId);

            mTerritory = (TextView) findViewById(R.id.territory);
            mTerritory.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            addTerritory();
                        }
                    });

            Button save = (Button) findViewById(R.id.save);
            save.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            save();
                        }
                    });

            Button signAndSend = (Button) findViewById(R.id.sign_and_send);
            signAndSend.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            signAndSend();
                        }
                    });

            setControlViews();

            saveControlsToFeature();
        }
    }


    @Override
    public void onBackPressed()
    {
        clearTempFeature();

        super.onBackPressed();
    }


    @Override
    public void finish()
    {
        clearTempFeature();

        super.finish();
    }


    protected void clearTempFeature()
    {
        if (null != mNewFeature) {
            MainApplication app = (MainApplication) getApplication();
            app.setTempFeature(null);
            mNewFeature = null;
        }
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
        showTargetingDialog();
    }


    protected void showTargetingDialog()
    {
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
        if (null == mNewFeature) {
            return;
        }

        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_NUMBER, mDocNumber.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE, mCreationDateTime.getValue());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_AUTHOR, mAuthor.getText().toString());
        mNewFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_TERRITORY, mTerritory.getText().toString());
    }


    protected void restoreControlsFromFeature()
    {
        if (null == mNewFeature) {
            return;
        }

        mDocNumber.setText(
                (String) mNewFeature.getFieldValue(Constants.FIELD_DOCUMENTS_NUMBER));
        mCreationDateTime.setValue(
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


    protected void loadPhotoAttaches(DocumentEditFeature feature)
    {
        if (null == feature) {
            return;
        }

        feature.clearAttachments();

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(getMenuRes(), menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_territory:
                addTerritory();
                return true;

            case R.id.action_save:
                save();
                return true;

            case R.id.action_sign:
                signAndSend();
                return true;

            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void addTerritory()
    {
        Intent intent = new Intent(this, SelectTerritoryActivity.class);
        startActivityForResult(intent, getActivityCode());
    }


    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == getActivityCode()) {
            mTerritory.setText(
                    mNewFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));
        }
    }


    protected void save()
    {
        if (mNewFeature.getGeometry() == null || TextUtils.isEmpty(
                mTerritory.getText().toString())) {
            Toast.makeText(this, getString(R.string.error_territory_must_be_set), Toast.LENGTH_LONG)
                    .show();
        }

        MainApplication app = (MainApplication) getApplication();
        MapBase mapBase = app.getMap();
        DocumentsLayer documentsLayer = null;
        //get documents layer
        for (int i = 0; i < mapBase.getLayerCount(); i++) {
            ILayer layer = mapBase.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                documentsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if (null == documentsLayer) {
            Toast.makeText(
                    this, getString(R.string.error_documents_layer_not_found), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        DocumentEditFeature feature = app.getTempFeature();

        // TODO: if feature exist then update else insert
        if (documentsLayer.insert(feature, false)) {
            //remove temp feature
            app.setTempFeature(null);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
        }
    }


    protected void signAndSend()
    {
        //check required field
        if (mNewFeature.getGeometry() == null || TextUtils.isEmpty(
                mTerritory.getText().toString())) {
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
