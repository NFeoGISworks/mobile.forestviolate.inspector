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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.FIPreferencesActivity;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplibui.util.SettingsConstantsUI;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FIPreferenceFragment
        extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        String settings = getArguments().getString(SettingsConstantsUI.PREFS_SETTINGS);

        switch (settings) {
            case SettingsConstantsUI.ACTION_PREFS_GENERAL:
                addPreferencesFromResource(R.xml.preferences_general);

                final ListPreference theme =
                        (ListPreference) findPreference(SettingsConstantsUI.KEY_PREF_THEME);
                FIPreferencesActivity.initializeTheme((FIPreferencesActivity) getActivity(), theme);

                final ListPreference noteInitTerm = (ListPreference) findPreference(
                        SettingsConstants.KEY_PREF_NOTE_INITIAL_TERM);
                FIPreferencesActivity.initializeNoteInitTerm(noteInitTerm);
                break;

            case SettingsConstantsUI.ACTION_PREFS_MAP:
                addPreferencesFromResource(R.xml.preferences_map);

                final ListPreference lpCoordinateFormat = (ListPreference) findPreference(
                        SettingsConstantsUI.KEY_PREF_COORD_FORMAT);
                FIPreferencesActivity.initializeCoordinateFormat(lpCoordinateFormat);
                break;

            case SettingsConstantsUI.ACTION_PREFS_LOCATION:
                addPreferencesFromResource(R.xml.preferences_location);

                final ListPreference lpLocationAccuracy = (ListPreference) findPreference(
                        com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_SOURCE);
                FIPreferencesActivity.initializeLocationAccuracy(lpLocationAccuracy, false);

                final ListPreference minTimeLoc = (ListPreference) findPreference(
                        com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_TIME);
                final ListPreference minDistanceLoc = (ListPreference) findPreference(
                        com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE);
                FIPreferencesActivity.initializeLocationMins(minTimeLoc, minDistanceLoc, false);

                final EditTextPreference accurateMaxCount = (EditTextPreference) findPreference(
                        com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_ACCURATE_COUNT);
                FIPreferencesActivity.initializeAccurateTaking(accurateMaxCount);
                break;
        }
    }
}
