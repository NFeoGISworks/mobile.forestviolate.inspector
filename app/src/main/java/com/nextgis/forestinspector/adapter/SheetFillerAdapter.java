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


public class SheetFillerAdapter
        extends ListFillerAdapter
{
    public SheetFillerAdapter(DocumentFeature feature)
    {
        super(feature);
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_SHEET;
    }


    @Override
    protected int getItemViewResId()
    {
        return R.layout.row_sheet_item;
    }


    @Override
    protected CheckListAdapter.ViewHolder getViewHolder(View itemView)
    {
        return new SheetFillerAdapter.ViewHolder(itemView, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(
            CheckListAdapter.ViewHolder holder,
            int position)
    {
        super.onBindViewHolder(holder, position);

        SheetFillerAdapter.ViewHolder viewHolder = (SheetFillerAdapter.ViewHolder) holder;

        Feature item = mFeatures.get(position);

        viewHolder.mUnit.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_UNIT));

        viewHolder.mSpecies.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_SPECIES));

        viewHolder.mCategory.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_CATEGORY));

        viewHolder.mThickness.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_THICKNESS));

        viewHolder.mHeight.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_HEIGHTS));

        viewHolder.mCount.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_SHEET_COUNT));
    }


    public static class ViewHolder
            extends ListFillerAdapter.ViewHolder
    {
        TextView mUnit;
        TextView mSpecies;
        TextView mCategory;
        TextView mThickness;
        TextView mHeight;
        TextView mCount;


        public ViewHolder(
                View itemView,
                ListFillerAdapter.ViewHolder.OnItemClickListener listener)
        {
            super(itemView, listener);

            mUnit = (TextView) itemView.findViewById(R.id.unit);
            mSpecies = (TextView) itemView.findViewById(R.id.species);
            mCategory = (TextView) itemView.findViewById(R.id.category);
            mThickness = (TextView) itemView.findViewById(R.id.thickness);
            mHeight = (TextView) itemView.findViewById(R.id.height);
            mCount = (TextView) itemView.findViewById(R.id.count);
        }
    }
}
