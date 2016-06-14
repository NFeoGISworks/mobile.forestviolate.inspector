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

import android.database.Cursor;
import android.view.View;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class TargetingListAdapter
        extends ListSelectorCursorAdapter
{
    protected int mDateColumn;
    protected int mRegionColumn;
    protected int mForestryColumn;
    protected int mPrecinctColumn;
    protected int mTerritoryColumn;


    public TargetingListAdapter(Cursor cursor)
    {
        super(cursor);
    }


    @Override
    protected void initCursor(Cursor cursor)
    {
        super.initCursor(cursor);

        if (cursor != null) {
            mDateColumn = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_DATE);
            mRegionColumn = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_REGION);
            mForestryColumn = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_FORESTRY);
            mPrecinctColumn = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_PRECINCT);
            mTerritoryColumn = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_TERRITORY);
        } else {
            mDateColumn = -1;
            mRegionColumn = -1;
            mForestryColumn = -1;
            mPrecinctColumn = -1;
            mTerritoryColumn = -1;
        }
    }


    @Override
    protected int getItemViewResId()
    {
        return R.layout.item_targeting;
    }


    @Override
    protected ListSelectorAdapter.ViewHolder getViewHolder(View itemView)
    {
        setOnItemClickListener(
                new ListSelectorAdapter.ViewHolder.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(int position)
                    {
                        setSelection(position, true);
                    }
                });

        return new TargetingListAdapter.ViewHolder(itemView, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(
            ListSelectorAdapter.ViewHolder holder,
            Cursor cursor)
    {
        TargetingListAdapter.ViewHolder viewHolder = (TargetingListAdapter.ViewHolder) holder;

        long date = cursor.getLong(mDateColumn);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        String dateStr = mDateFormat.format(calendar.getTime());

        String targetStr = dateStr + ",\n" +
                            cursor.getString(mRegionColumn) + " " +
                            cursor.getString(mForestryColumn) + "\n" +
                            cursor.getString(mPrecinctColumn) + " " +
                            cursor.getString(mTerritoryColumn);

        viewHolder.mTarget.setText(targetStr);
    }


    public static class ViewHolder
            extends ListFillerAdapter.ViewHolder
    {
        TextView mTarget;


        public ViewHolder(
                View itemView,
                OnItemClickListener listener)
        {
            super(itemView, listener, null);

            mTarget = (TextView) itemView.findViewById(R.id.target);
        }
    }
}
