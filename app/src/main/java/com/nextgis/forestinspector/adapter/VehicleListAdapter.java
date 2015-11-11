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

package com.nextgis.forestinspector.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.CheckListActivity;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;


public class VehicleListAdapter
        extends CheckListAdapter
{
    public VehicleListAdapter(
            CheckListActivity activity,
            DocumentFeature feature)
    {
        super(activity, feature);
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_VEHICLES);
    }


    @Override
    public void notifyDataSetChanged()
    {
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_VEHICLES);
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        Feature item = (Feature) getItem(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_NAME));

        TextView desc = (TextView) view.findViewById(R.id.desc);
        desc.setText(": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_DESCRIPTION));

        TextView nums = (TextView) view.findViewById(R.id.nums);
        nums.setText(": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_ENGINE_NUM));

        TextView user = (TextView) view.findViewById(R.id.user);
        user.setText(": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_USER));

        return view;
    }


    @Override
    protected int getRowItemResource()
    {
        return R.layout.row_vehicle_item;
    }
}
