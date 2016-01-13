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
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.util.Constants;


public class ParcelCursorAdapter
        extends CursorAdapter
{
    private static LayoutInflater mInflater = null;

    protected Context             mContext;
    protected DocumentEditFeature mEditFeature;


    public ParcelCursorAdapter(
            Context context,
            Cursor cursor,
            int flags,
            DocumentEditFeature editFeature)
    {
        super(context, cursor, flags);

        mContext = context;
        mEditFeature = editFeature;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(
            Context context,
            Cursor cursor,
            ViewGroup parent)
    {
        View view = mInflater.inflate(R.layout.item_parcel, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        view.setTag(viewHolder);

        viewHolder.mParcelDesc = (TextView) view.findViewById(R.id.parcel_desc);
        viewHolder.mCheckBox = (CheckBox) view.findViewById(R.id.check);

        return view;
    }


    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(
            View view,
            Context context,
            Cursor cursor)
    {
        if (null == view) {
            view = mInflater.inflate(R.layout.item_parcel, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();


        // Find fields to populate in inflated template
        String sParcelDesc =
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV)) +
                        " " + mContext.getString(R.string.forestry) + ", " +
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV)) +
                " " + mContext.getString(R.string.district_forestry) + ", " +
                mContext.getString(R.string.parcel) + " " +
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_PARCEL));
        long id = cursor.getLong(
                cursor.getColumnIndexOrThrow(com.nextgis.maplib.util.Constants.FIELD_ID));
        // Populate fields with extracted properties
        viewHolder.mParcelDesc.setText(sParcelDesc);


        viewHolder.mCheckBox.setTag(id);
        if (mEditFeature.getParcelIds().contains(id)) {
            viewHolder.mCheckBox.setChecked(true);
        } else {
            viewHolder.mCheckBox.setChecked(false);
        }
        viewHolder.mCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView,
                            boolean isChecked)
                    {
                        Long id = (Long) buttonView.getTag();
                        if (isChecked) {
                            if (!mEditFeature.getParcelIds().contains(id)) {
                                mEditFeature.getParcelIds().add(id);
                            }
                        } else {
                            mEditFeature.getParcelIds().remove(id);
                        }
                    }
                });
    }

    public static class ViewHolder
    {
        public TextView mParcelDesc;
        public CheckBox mCheckBox;
    }
}
