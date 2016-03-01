/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2016. NextGIS, info@nextgis.com
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

package com.nextgis.forestinspector.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.justsimpleinfo.Table.Table;
import com.justsimpleinfo.Table.TableData;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.SheetTableFillerActivity;


public class SheetTableFillerFragment
        extends Fragment
        implements SheetTableFillerActivity.OnSaveTableDataListener
{
    LinearLayout mTableLayout;
    Table        mTable;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        mTable = new Table(getActivity());
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sheet_table_filler, null);

        mTableLayout = (LinearLayout) view.findViewById(R.id.table_layout);
        mTableLayout.addView(mTable);

        return view;
    }


    @Override
    public void onDestroyView()
    {
        mTableLayout.removeView(mTable);
        super.onDestroyView();
    }


    @Override
    public void onSaveTableDataListener()
    {
        TableData tableData = mTable.getTableData();
        // TODO:
    }
}
