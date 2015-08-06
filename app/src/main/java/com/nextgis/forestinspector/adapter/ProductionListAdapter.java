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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;

import java.util.List;

/**
 * Created by bishop on 07.08.15.
 */
public class ProductionListAdapter extends BaseAdapter {
    protected List<Feature> mFeatures;
    protected Context mContext;
    protected DocumentFeature mFeature;

    public ProductionListAdapter(Context context, DocumentFeature feature) {
        mContext = context;
        mFeature = feature;
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_PRODUCTION);
    }

    @Override
    public void notifyDataSetChanged() {
        mFeatures = mFeature.getSubFeatures(Constants.KEY_LAYER_PRODUCTION);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(null == mFeatures)
            return 0;
        return mFeatures.size();
    }

    @Override
    public Object getItem(int position) {
        return mFeatures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.row_production_item, null);
        }

        Feature item = (Feature) getItem(position);

        TextView species = (TextView) v.findViewById(R.id.species);
        species.setText(": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_SPECIES));

        TextView thickness = (TextView) v.findViewById(R.id.thickness);
        thickness.setText(": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_DIAMETER));

        TextView count = (TextView) v.findViewById(R.id.count);
        count.setText(": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_COUNT));

        TextView category = (TextView) v.findViewById(R.id.category);
        category.setText(": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_TYPE));

        TextView length = (TextView) v.findViewById(R.id.length);
        length.setText(": " + item.getFieldValueAsString(Constants.FIELD_PRODUCTION_LENGTH));

        return v;
    }
}
