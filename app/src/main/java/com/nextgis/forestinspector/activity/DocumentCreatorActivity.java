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
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplibui.control.DateTime;

import java.io.File;
import java.util.Calendar;


public abstract class DocumentCreatorActivity
        extends FIActivity
        implements TargetingDialog.OnSelectTargetListener,
                   SignDialog.OnSignListener
{
    protected MainApplication     mApp;
    protected DocumentsLayer      mDocsLayer;
    protected DocumentEditFeature mEditFeature;

    protected Long mRestoredFeatureId;

    protected String mUserDesc;

    protected EditText mDocNumber;
    protected DateTime mCreationDateTime;
    protected EditText mAuthor;
    protected TextView mTerritory;

    protected boolean mIsNewTempFeature = false;


    protected abstract int getActivityCode();

    protected abstract int getDocType();

    protected abstract int getContentViewRes();

    protected abstract CharSequence getTitleString();

    protected abstract int getMenuRes();

    protected abstract void setControlViews();


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (null != mEditFeature) {
            outState.putLong(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState) {
            mRestoredFeatureId =
                    savedInstanceState.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
        }

        mApp = (MainApplication) getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDocsLayer = mApp.getDocsLayer();

        if (null == mDocsLayer || !loadEditFeature()) {
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            Toast.makeText(this, getString(R.string.error_db_query), Toast.LENGTH_LONG).show();
            return;
        }

        if (null != mEditFeature) {

            setContentView(getContentViewRes());

            String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

            setToolbar(R.id.main_toolbar);
            setTitle(getTitleString());

            mDocNumber = (EditText) findViewById(R.id.doc_num);

            mCreationDateTime = (DateTime) findViewById(R.id.creation_datetime);
            mCreationDateTime.init(null, null, null);

            mAuthor = (EditText) findViewById(R.id.author);

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

            if (mIsNewTempFeature) {
                mDocNumber.setText(getNewNumber(sUserPassId));
                mCreationDateTime.setCurrentDate();
                mAuthor.setText(mUserDesc + getString(R.string.passid_is) + " " + sUserPassId);
            }

            setControlViews();

            if (mIsNewTempFeature && !saveTempEditFeature()) {
                Toast.makeText(this, getString(R.string.error_db_update), Toast.LENGTH_LONG).show();
            }
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
        if (null == mEditFeature) {
            return;
        }

        mApp.clearAllTemps();
        mEditFeature = null;
    }


    @Override
    protected void onPause()
    {
        super.onPause();

        if (!saveTempEditFeature()) {
            Toast.makeText(this, getString(R.string.error_db_update), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (null == mEditFeature && !loadEditFeature()) {
            Toast.makeText(this, getString(R.string.error_db_query), Toast.LENGTH_LONG).show();
            return;
        }

        restoreControlsFromFeature();
        showTargetingDialog();
    }


    protected void showTargetingDialog()
    {
        String vector = (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_VECTOR);

        if (null == vector) {
            FragmentManager fm = getSupportFragmentManager();

            TargetingDialog fragment =
                    (TargetingDialog) fm.findFragmentByTag(Constants.FRAGMENT_TARGETING_DIALOG);

            if (fragment == null) {
                TargetingDialog dialog = new TargetingDialog();
                dialog.setOnSelectTargetListener(this);
                dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_TARGETING_DIALOG);
            }
        }
    }


    @Override
    public void onSelectTarget(String objectId)
    {
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_VECTOR, objectId);
    }


    protected void saveControlsToFeature()
    {
        if (null == mEditFeature) {
            return;
        }

        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_NUMBER, mDocNumber.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_DATE, mCreationDateTime.getValue());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_AUTHOR, mAuthor.getText().toString());
        mEditFeature.setFieldValue(
                Constants.FIELD_DOCUMENTS_TERRITORY, mTerritory.getText().toString());
    }


    protected void restoreControlsFromFeature()
    {
        if (null == mEditFeature) {
            return;
        }

        mDocNumber.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_NUMBER));
        mCreationDateTime.setValue(
                mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_DATE));
        mAuthor.setText(
                (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_AUTHOR));

        // TODO: restore DocumentEditFeature.mParcelIds from FIELD_DOCUMENTS_TERRITORY
        if (mEditFeature.getParcelIds().size() > 0) {
            mTerritory.setText(
                    (String) mEditFeature.getFieldValue(Constants.FIELD_DOCUMENTS_TERRITORY));
        }
    }


    protected boolean loadEditFeature()
    {
        mIsNewTempFeature = false;

        Bundle extras = getIntent().getExtras();

        Long featureId = null;
        if (null != mRestoredFeatureId) {
            featureId = mRestoredFeatureId;
        } else if (null != extras && extras.containsKey(
                com.nextgis.maplib.util.Constants.FIELD_ID)) {
            featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
        } // if featureId == null -- get new temp feature

        mEditFeature = mApp.getEditFeature(featureId);

        if (null != mEditFeature && mApp.isNewTempFeature()) {
            mIsNewTempFeature = true;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long userId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
            mUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");

            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_TYPE, getDocType());
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_STATUS, Constants.DOCUMENT_STATUS_NEW);
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOC_ID, com.nextgis.maplib.util.Constants.NOT_FOUND);
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_USER_ID, userId);
            mEditFeature.setFieldValue(
                    Constants.FIELD_DOCUMENTS_USER, mUserDesc);
        }

        if (null == mEditFeature) {
            mIsNewTempFeature = false;
            return false;
        }

        return true;
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
        intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mEditFeature.getId());
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
                    mEditFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));
        }
    }


    protected boolean saveTempEditFeature()
    {
        if (null == mEditFeature) {
            return true;
        }

        saveControlsToFeature();

        return (mDocsLayer.updateFeatureWithAttachesWithFlags(mEditFeature) > 0);
    }


    protected boolean saveEditFeature()
    {
        if (!saveTempEditFeature()) {
            return false;
        }

        mDocsLayer.setFeatureWithAttachesTempFlag(mEditFeature, false);
        mDocsLayer.setFeatureWithAttachesNotSyncFlag(mEditFeature, true);

        return true;
    }


    protected boolean saveToSendEditFeature()
    {
        if (!saveTempEditFeature()) {
            return false;
        }

        mDocsLayer.setFeatureWithAttachesTempFlag(mEditFeature, false);
        mDocsLayer.setFeatureWithAttachesNotSyncFlag(mEditFeature, false);

        return true;
    }


    protected void save()
    {
        if (saveEditFeature()) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
        }
    }


    protected void signAndSend()
    {
        //check required field
        if (mEditFeature.getGeometry() == null || TextUtils.isEmpty(
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

        if (saveTempEditFeature()) {
            //show dialog with sign and save / edit buttons
            SignDialog signDialog = new SignDialog();
            signDialog.setOnSignListener(this);
            signDialog.show(getSupportFragmentManager(), Constants.FRAGMENT_SIGN_DIALOG);
        } else {
            Toast.makeText(this, getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSign(File signatureFile)
    {
        AttachItem attachItem = mDocsLayer.getNewTempAttach(mEditFeature);

        if (null == attachItem) {
            Toast.makeText(this, getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
            return;
        }

        attachItem.setDisplayName(signatureFile.getName());
        attachItem.setMimetype("image/png");
        attachItem.setDescription(Constants.SIGN_DESCRIPTION);

        long featureId = mEditFeature.getId();
        long attachId = Long.parseLong(attachItem.getAttachId());

        boolean res = mDocsLayer.insertAttachFile(featureId, attachId, signatureFile);

        if (res) {
            res = saveToSendEditFeature();
        }

        if (res) {
            res = mDocsLayer.setDocumentStatus(featureId, Constants.DOCUMENT_STATUS_FOR_SEND);
        }

        if (res) {
            res = mDocsLayer.addChangeNew(mEditFeature);
        }

        if (res) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
        }
    }
}
