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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.fragment.ListFillerFragment;


public abstract class ListFillerActivity
        extends FIActivity
        implements IDocumentFeatureSource
{
    protected DocumentEditFeature mEditFeature;


    protected abstract String getFragmentTag();

    protected abstract ListFillerFragment getListFillerFragment();


    protected int getContentViewId()
    {
        return R.layout.activity_list_filler;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (null != extras && extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);

            MainApplication app = (MainApplication) getApplication();
            mEditFeature = app.getEditFeature(featureId);


            setContentView(getContentViewId());
            setToolbar(R.id.main_toolbar);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ListFillerFragment fragment =
                    (ListFillerFragment) fm.findFragmentByTag(getFragmentTag());

            if (fragment == null) {
                fragment = getListFillerFragment();
            }

            ft.replace(R.id.fragment, fragment, getFragmentTag());
            ft.commit();
        }
    }


    @Override
    public DocumentFeature getFeature()
    {
        return mEditFeature;
    }
}
