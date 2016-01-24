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

import android.support.v7.preference.PreferenceFragmentCompat;
import com.nextgis.forestinspector.fragment.SyncSettingsFragment;
import com.nextgis.forestinspector.util.Constants;


public class SyncSettingsActivity
        extends NGPreferenceActivity
{
    @Override
    protected String getFragmentTag()
    {
        return Constants.FRAGMENT_SYNC_SETTINGS;
    }


    @Override
    protected PreferenceFragmentCompat getPreferenceFragment()
    {
        return new SyncSettingsFragment();
    }
}
