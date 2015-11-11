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

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.CheckListAdapter;
import com.nextgis.forestinspector.adapter.VehicleListAdapter;
import com.nextgis.forestinspector.dialog.VehicleFillDialog;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;


public class VehicleActivity
        extends CheckListActivity
        implements VehicleFillDialog.OnAddVehicleListener
{

    @Override
    protected int getContentViewId()
    {
        return R.layout.activity_vehicle;
    }


    @Override
    protected CheckListAdapter getAdapter()
    {
        return new VehicleListAdapter(this, mDocumentFeature);
    }


    @Override
    protected void onListItemClick(int position)
    {
        VehicleFillDialog dialog = new VehicleFillDialog();
        dialog.setOnAddVehicleListener(this);
        dialog.setFeature((Feature) mAdapter.getItem(position));
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_VEHICLE_FILL_DIALOG);
    }


    @Override
    protected void add()
    {
        VehicleFillDialog dialog = new VehicleFillDialog();
        dialog.setOnAddVehicleListener(this);
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_VEHICLE_FILL_DIALOG);
    }


    @Override
    public void onAddVehicle()
    {
        mAdapter.notifyDataSetChanged();
    }
}
