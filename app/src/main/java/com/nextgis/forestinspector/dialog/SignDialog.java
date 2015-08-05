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

package com.nextgis.forestinspector.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;

/**
 * Created by bishop on 02.08.15.
 */
public class SignDialog
        extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();

        View view = View.inflate(context, R.layout.dialog_sign, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.sign_and_save))
        .setView(view)
        .setNegativeButton(R.string.fix, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCreateDocument();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void onCreateDocument(){
        //save picture to file and add it as attachment

        MainApplication app = (MainApplication) getActivity().getApplication();
        MapBase mapBase = app.getMap();
        DocumentsLayer documentsLayer = null;
        //get documents layer
        for(int i = 0; i < mapBase.getLayerCount(); i++){
            ILayer layer = mapBase.getLayer(i);
            if(layer instanceof DocumentsLayer){
                documentsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if(null == documentsLayer) {
            Toast.makeText(getActivity(),
                    getString(R.string.error_documents_layer_not_found), Toast.LENGTH_LONG).show();
            return;
        }

        if(documentsLayer.insert(app.getTempFeature())) {
            //remove temp feature
            app.setTempFeature(null);
        }
        else {
            Toast.makeText(getActivity(),
                    getString(R.string.error_db_insert), Toast.LENGTH_LONG).show();
        }
    }
}
