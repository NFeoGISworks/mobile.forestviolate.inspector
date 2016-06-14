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

package com.nextgis.forestinspector.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.nextgis.forestinspector.activity.SheetTableFillerActivity;
import com.nextgis.forestinspector.adapter.ListFillerAdapter;
import com.nextgis.forestinspector.adapter.SheetListFillerAdapter;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.dialog.ListFillerDialog;
import com.nextgis.forestinspector.dialog.SheetListFillerDialog;


public class SheetListFillerFragment
        extends ListFillerFragment
{
    protected static final int REQUEST_ITEMS = 1;

    @Override
    protected ListFillerAdapter getFillerAdapter(DocumentFeature feature)
    {
        return new SheetListFillerAdapter(feature);
    }


    @Override
    protected ListFillerDialog getFillerDialog()
    {
        return new SheetListFillerDialog();
    }


    @Override
    protected void addItem()
    {
        Bundle extras = getActivity().getIntent().getExtras();
        if (null != extras && extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);

            Intent intent = new Intent(getActivity(), SheetTableFillerActivity.class);
            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, featureId);
            startActivityForResult(intent, REQUEST_ITEMS);
        }
    }


    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == REQUEST_ITEMS && resultCode == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
