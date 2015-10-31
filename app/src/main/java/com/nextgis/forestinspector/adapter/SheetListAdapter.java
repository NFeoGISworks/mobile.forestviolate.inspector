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
import android.view.ViewGroup;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.CheckListActivity;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;


public class SheetListAdapter
        extends CheckListAdapter
{
    public SheetListAdapter(
            CheckListActivity activity,
            DocumentFeature feature)
    {
        super(activity, feature);
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_SHEET);
    }


    @Override
    public void notifyDataSetChanged()
    {
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_SHEET);
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

        TextView unit = (TextView) view.findViewById(R.id.unit);
        unit.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_UNIT));

        TextView species = (TextView) view.findViewById(R.id.species);
        species.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_SPECIES));

        TextView category = (TextView) view.findViewById(R.id.category);
        category.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_CATEGORY));

        TextView thickness = (TextView) view.findViewById(R.id.thickness);
        thickness.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_THICKNESS));

        TextView height = (TextView) view.findViewById(R.id.height);
        height.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_HEIGHTS));

        TextView count = (TextView) view.findViewById(R.id.count);
        count.setText(item.getFieldValueAsString(Constants.FIELD_SHEET_COUNT));

        return view;
    }


    @Override
    protected int getRowItemResource()
    {
        return R.layout.row_sheet_item;
    }
}
