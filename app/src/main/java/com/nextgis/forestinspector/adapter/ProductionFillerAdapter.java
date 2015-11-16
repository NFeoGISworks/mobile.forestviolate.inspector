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


public class ProductionFillerAdapter
        extends ListFillerAdapter
{
    public ProductionFillerAdapter(DocumentFeature feature)
    {
        super(feature);
    }


    @Override
    protected String getLayerName()
    {
        return Constants.KEY_LAYER_PRODUCTION;
    }


    @Override
    protected int getItemViewResId()
    {
        return R.layout.row_production_item;
    }


    @Override
    protected CheckListAdapter.ViewHolder getViewHolder(View itemView)
    {
        return new ProductionFillerAdapter.ViewHolder(itemView, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(
            CheckListAdapter.ViewHolder holder,
            int position)
    {
        super.onBindViewHolder(holder, position);

        ProductionFillerAdapter.ViewHolder viewHolder = (ProductionFillerAdapter.ViewHolder) holder;

        Feature item = mFeatures.get(position);

        viewHolder.mSpecies.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_SPECIES));

        viewHolder.mType.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_TYPE));

        viewHolder.mLength.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_LENGTH));

        viewHolder.mThickness.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_THICKNESS));

        viewHolder.mCount.setText(
                ": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_COUNT));
    }


    public static class ViewHolder
            extends ListFillerAdapter.ViewHolder
    {
        TextView mSpecies;
        TextView mType;
        TextView mLength;
        TextView mThickness;
        TextView mCount;


        public ViewHolder(
                View itemView,
                OnItemClickListener listener)
        {
            super(itemView, listener);

            mSpecies = (TextView) itemView.findViewById(R.id.species);
            mType = (TextView) itemView.findViewById(R.id.type);
            mLength = (TextView) itemView.findViewById(R.id.length);
            mThickness = (TextView) itemView.findViewById(R.id.thickness);
            mCount = (TextView) itemView.findViewById(R.id.count);
        }
    }
}
