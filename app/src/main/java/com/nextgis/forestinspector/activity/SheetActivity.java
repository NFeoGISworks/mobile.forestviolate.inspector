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

package com.nextgis.forestinspector.activity;

import android.preference.PreferenceManager;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.CheckListAdapter;
import com.nextgis.forestinspector.adapter.SheetListAdapter;
import com.nextgis.forestinspector.dialog.SheetFillDialog;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class SheetActivity
        extends CheckListActivity
        implements SheetFillDialog.OnAddTreesListener
{

    @Override
    protected int getContentViewId()
    {
        return R.layout.activity_sheet;
    }


    @Override
    protected CheckListAdapter getAdapter()
    {
        return new SheetListAdapter(this, mDocumentFeature);
    }


    @Override
    protected void onListItemClick(int position)
    {
        final SheetFillDialog dialog = new SheetFillDialog();
        dialog.setOnAddTreesListener(this);
        dialog.setFeature((Feature) mAdapter.getItem(position));
        dialog.show(getSupportFragmentManager(), Constants.FRAGMENT_SHEET_FILL_DIALOG);
    }


    @Override
    protected void add()
    {
        final SheetFillDialog dialog = new SheetFillDialog();
        dialog.setOnAddTreesListener(this);
        dialog.show(
                getSupportFragmentManager(), Constants.FRAGMENT_SHEET_FILL_DIALOG);
    }


    @Override
    public void onAddTrees()
    {
        mAdapter.notifyDataSetChanged();
    }
}
