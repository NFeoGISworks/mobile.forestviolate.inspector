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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.ProductionActivity;

/**
 * Created by bishop on 07.08.15.
 */
public class ProductionInputDialog
        extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();

        View view = View.inflate(context, R.layout.dialog_production, null);

        final EditText species = (EditText)view.findViewById(R.id.species);
        final EditText cat = (EditText)view.findViewById(R.id.cat);
        final EditText length = (EditText)view.findViewById(R.id.length);
        final EditText thickness = (EditText)view.findViewById(R.id.thickness);
        final EditText count = (EditText)view.findViewById(R.id.count);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.add_pruduction))
                .setView(view)
                .setNegativeButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAdd(species.getText().toString(), cat.getText().toString(),
                            Double.parseDouble(length.getText().toString()),
                            Double.parseDouble(thickness.getText().toString()),
                            Integer.parseInt(count.getText().toString()));
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void onAdd(String species, String cat, double length, double thickness, int count) {
        ProductionActivity activity = (ProductionActivity) getActivity();
        if(null != activity)
            activity.addProduction(species, cat, length, thickness, count);
    }
}
