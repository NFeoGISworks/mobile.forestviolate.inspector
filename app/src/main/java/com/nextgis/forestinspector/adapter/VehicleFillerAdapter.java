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

package com.nextgis.forestinspector.adapter;

import android.view.View;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;


public class VehicleFillerAdapter
        extends ListFillerAdapter
{
    public VehicleFillerAdapter(DocumentFeature feature)
    {
        super(feature);
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_VEHICLES;
    }


    @Override
    protected int getItemViewResId()
    {
        return R.layout.row_vehicle_item;
    }


    @Override
    protected CheckListAdapter.ViewHolder getViewHolder(View itemView)
    {
        return new VehicleFillerAdapter.ViewHolder(itemView, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(
            CheckListAdapter.ViewHolder holder,
            int position)
    {
        super.onBindViewHolder(holder, position);

        VehicleFillerAdapter.ViewHolder viewHolder = (VehicleFillerAdapter.ViewHolder) holder;

        Feature item = mFeatures.get(position);

        viewHolder.mName.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_NAME));

        viewHolder.mDesc.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_DESCRIPTION));

        viewHolder.mNums.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_ENGINE_NUM));

        viewHolder.mUser.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_VEHICLE_USER));
    }


    public static class ViewHolder
            extends ListFillerAdapter.ViewHolder
    {
        TextView mName;
        TextView mDesc;
        TextView mNums;
        TextView mUser;


        public ViewHolder(
                View itemView,
                OnItemClickListener listener)
        {
            super(itemView, listener);

            mName = (TextView) itemView.findViewById(R.id.name);
            mDesc = (TextView) itemView.findViewById(R.id.desc);
            mNums = (TextView) itemView.findViewById(R.id.nums);
            mUser = (TextView) itemView.findViewById(R.id.user);
        }
    }
}
