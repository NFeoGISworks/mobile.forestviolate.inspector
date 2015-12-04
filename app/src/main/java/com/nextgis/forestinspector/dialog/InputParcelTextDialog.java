/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 * ****************************************************************************
 * Copyright (c) 2015. NextGIS, info@nextgis.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.SelectTerritoryActivity;

/**
 * Created by bishop on 05.12.15.
 */
public class InputParcelTextDialog
        extends DialogFragment {

    protected final String KEY_TEXT = "text";
    protected EditText mParcelTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        String parcelText;
        if (null != savedInstanceState) {
            parcelText = savedInstanceState.getString(KEY_TEXT);
        }
        else{
            parcelText = activity.getTerritoryText();
        }


        View view = View.inflate(activity, R.layout.dialog_choose_parcels_text, null);

        mParcelTextView = (EditText) view.findViewById(R.id.ed_parcel_text);
        if(!TextUtils.isEmpty(parcelText))
            mParcelTextView.setText(parcelText);

        RadioGroup radiogroup = (RadioGroup) view.findViewById(R.id.ck_group);
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.ck_from_parcels:
                        mParcelTextView.setEnabled(false);
                        break;
                    case R.id.ck_input_text:
                        mParcelTextView.setEnabled(true);
                        break;
                }
            }
        });

        final RadioButton rbFillFromParcels = (RadioButton) view.findViewById(R.id.ck_from_parcels);


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.input_territory_text).setView(view).setPositiveButton(
                R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog,
                            int id) {
                        if(rbFillFromParcels.isChecked()){
                            activity.setTerritoryTextByGeom();
                        }
                        else{
                            activity.setTerritoryText(mParcelTextView.getText().toString());
                        }

                    }
                }).setNegativeButton(
                R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog,
                            int id) {
                        // User cancelled the dialog
                        activity.clearTerritoryGeometry();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(KEY_TEXT, mParcelTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }
}