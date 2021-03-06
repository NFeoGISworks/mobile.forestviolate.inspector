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

package com.nextgis.forestinspector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.fragment.SheetTableFillerFragment;
import com.nextgis.forestinspector.util.Constants;


public class SheetTableFillerActivity
        extends FIActivity
        implements SheetTableFillerFragment.OnAddTreeStubsListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sheet_list_filler);
        setToolbar(R.id.main_toolbar);

        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        SheetTableFillerFragment fillerFragment = (SheetTableFillerFragment) fm.findFragmentByTag(
                Constants.FRAGMENT_SHEET_TABLE_FILLER);

        if (null == fillerFragment) {
            fillerFragment = new SheetTableFillerFragment();
            fillerFragment.setOnAddTreeStubsListener(this);
        }

        ft.replace(R.id.table, fillerFragment, Constants.FRAGMENT_SHEET_TABLE_FILLER);
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.sheet_table_filler, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveTableData();
                return true;

            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void saveTableData()
    {
        final FragmentManager fm = getSupportFragmentManager();
        SheetTableFillerFragment fillerFragment = (SheetTableFillerFragment) fm.findFragmentByTag(
                Constants.FRAGMENT_SHEET_TABLE_FILLER);
        if (null != fillerFragment) {
            if (fillerFragment.saveTableData()) {
                finish();
            }
            return;
        }

        Log.d(Constants.FITAG, "saveTableData() error, null == fillerFragment");
    }


    @Override
    public void onAddTreeStubs()
    {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }
}
