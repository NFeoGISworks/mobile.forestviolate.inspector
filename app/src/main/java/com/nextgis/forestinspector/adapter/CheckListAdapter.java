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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.CheckListActivity;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.maplib.datasource.Feature;

import java.util.List;

/**
 * Created by bishop on 09.08.15.
 */
public abstract class CheckListAdapter extends BaseAdapter {
    protected List<Feature> mFeatures;
    protected CheckListActivity mActivity;
    protected DocumentFeature mFeature;

    public CheckListAdapter(CheckListActivity activity, DocumentFeature feature) {
        mActivity = activity;
        mFeature = feature;
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
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            v = inflater.inflate(getRowItemResource(), null);
        }

        CheckBox cb = (CheckBox) v.findViewById(R.id.check);
        if(null != cb) {
            cb.setChecked(mActivity.isChecked(position));
        }

        return v;
    }

    protected abstract int getRowItemResource();

    public void deleteFeature(int nFeaturePos){
        mFeatures.remove(nFeaturePos);
        notifyDataSetChanged();
    }
}
