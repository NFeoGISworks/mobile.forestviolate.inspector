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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;

import java.util.List;


/**
 * The list of sheet items
 */
public class SheetViewListAdapter
        extends BaseAdapter
{
    protected List<Feature> mFeatures;
    protected Context       mContext;


    public SheetViewListAdapter(
            Context context,
            List<Feature> features)
    {
        mContext = context;
        mFeatures = features;
    }


    @Override
    public int getCount()
    {
        if (null == mFeatures) {
            return 0;
        }
        return mFeatures.size();
    }


    @Override
    public Object getItem(int position)
    {
        return mFeatures.get(position);
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent)
    {
        View view = convertView;
        if (null == view) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.row_sheet_item, null);
        }

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.check);
        checkBox.setVisibility(View.GONE);


        Feature item = (Feature) getItem(position);

        TextView unit = (TextView) view.findViewById(R.id.unit);
        unit.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_UNIT));

        TextView species = (TextView) view.findViewById(R.id.species);
        species.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_SPECIES));

        TextView category = (TextView) view.findViewById(R.id.category);
        category.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_CATEGORY));

        TextView thickness = (TextView) view.findViewById(R.id.thickness);
        thickness.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_THICKNESS));

        TextView height = (TextView) view.findViewById(R.id.height);
        height.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_HEIGHTS));

        TextView count = (TextView) view.findViewById(R.id.count);
        count.setText(": " + item.getFieldValueAsString(Constants.FIELD_SHEET_COUNT));

        return view;
    }
}
