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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.fragment.FIPreferenceFragment;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplibui.activity.NGPreferenceActivity;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.util.List;

import static com.nextgis.maplibui.service.TrackerService.isTrackerServiceRunning;


/**
 * Application preference
 */
public class FIPreferencesActivity
        extends NGPreferenceActivity
{
    protected boolean mIsPaused = false;


    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();

        if (action != null) {

            switch (action) {
                case SettingsConstantsUI.ACTION_PREFS_GENERAL:
                    addPreferencesFromResource(R.xml.preferences_general);

                    final ListPreference theme =
                            (ListPreference) findPreference(SettingsConstantsUI.KEY_PREF_THEME);
                    initializeTheme(this, theme);

                    final ListPreference noteInitTerm = (ListPreference) findPreference(
                            SettingsConstants.KEY_PREF_NOTE_INITIAL_TERM);
                    initializeNoteInitTerm(noteInitTerm);
                    break;

                case SettingsConstantsUI.ACTION_PREFS_MAP:
                    addPreferencesFromResource(R.xml.preferences_map);

                    final ListPreference lpCoordinateFormat = (ListPreference) findPreference(
                            SettingsConstantsUI.KEY_PREF_COORD_FORMAT);
                    initializeCoordinateFormat(lpCoordinateFormat);
                    break;

                case SettingsConstantsUI.ACTION_PREFS_LOCATION:
                    addPreferencesFromResource(R.xml.preferences_location);

                    final ListPreference lpLocationAccuracy = (ListPreference) findPreference(
                            com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_SOURCE);
                    initializeLocationAccuracy(lpLocationAccuracy, false);

                    final ListPreference minTimeLoc = (ListPreference) findPreference(
                            com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_TIME);
                    final ListPreference minDistanceLoc = (ListPreference) findPreference(
                            com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE);
                    initializeLocationMins(minTimeLoc, minDistanceLoc, false);

                    final EditTextPreference accurateMaxCount = (EditTextPreference) findPreference(
                            com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_ACCURATE_COUNT);
                    initializeAccurateTaking(accurateMaxCount);
                    break;
            }

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Load the legacy preferences headers
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (mIsPaused) {
            startActivity(getIntent());
            finish();
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mIsPaused = true;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return FIPreferenceFragment.class.getName().equals(fragmentName);
        //return super.isValidFragment(fragmentName);
    }


    public static void initializeTheme(
            final FIPreferencesActivity activity,
            final ListPreference theme)
    {
        if (null != theme) {
            theme.setSummary(theme.getEntry());

            theme.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            activity.startActivity(activity.getIntent());
                            activity.finish();
                            return true;
                        }
                    });
        }
    }


    public static void initializeNoteInitTerm(ListPreference noteInitTerm)
    {
        if (null != noteInitTerm) {
            int id = noteInitTerm.findIndexOfValue(noteInitTerm.getValue());
            CharSequence summary = noteInitTerm.getEntries()[id];
            noteInitTerm.setSummary(summary);

            noteInitTerm.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            int id = ((ListPreference) preference).findIndexOfValue(
                                    (String) newValue);
                            CharSequence summary = ((ListPreference) preference).getEntries()[id];
                            preference.setSummary(summary);

                            return true;
                        }
                    });
        }
    }


    public static void initializeCoordinateFormat(ListPreference lpCoordinateFormat)
    {
        if (null != lpCoordinateFormat) {
            lpCoordinateFormat.setSummary(lpCoordinateFormat.getEntry());

            lpCoordinateFormat.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            int value = Integer.parseInt(newValue.toString());
                            CharSequence summary =
                                    ((ListPreference) preference).getEntries()[value];
                            preference.setSummary(summary);

                            String preferenceKey = preference.getKey() + "_int";
                            preference.getSharedPreferences()
                                    .edit()
                                    .putInt(preferenceKey, value)
                                    .commit();

                            return true;
                        }
                    });
        }
    }


    public static void initializeLocationAccuracy(
            final ListPreference listPreference,
            final boolean isTracks)
    {
        if (listPreference != null) {
            Context ctx = listPreference.getContext();
            CharSequence[] entries = new CharSequence[3];
            entries[0] = ctx.getString(R.string.pref_location_accuracy_gps);
            entries[1] = ctx.getString(R.string.pref_location_accuracy_cell);
            entries[2] = ctx.getString(R.string.pref_location_accuracy_gps) +
                    " & " +
                    ctx.getString(R.string.pref_location_accuracy_cell);
            listPreference.setEntries(entries);
            listPreference.setSummary(listPreference.getEntry());

            listPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener()
                    {
                        @Override
                        public boolean onPreferenceChange(
                                Preference preference,
                                Object newValue)
                        {
                            int value = Integer.parseInt(newValue.toString());
                            CharSequence summary =
                                    ((ListPreference) preference).getEntries()[value - 1];
                            preference.setSummary(summary);

                            sectionWork(preference.getContext(), isTracks);

                            return true;
                        }
                    });
        }
    }


    public static void initializeLocationMins(
            ListPreference minTime,
            final ListPreference minDistance,
            final boolean isTracks)
    {
        final Context context = minDistance.getContext();
        minTime.setSummary(getMinSummary(context, minTime.getEntry(), minTime.getValue()));
        minDistance.setSummary(
                getMinSummary(context, minDistance.getEntry(), minDistance.getValue()));

        minTime.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        int id = ((ListPreference) preference).findIndexOfValue((String) newValue);
                        preference.setSummary(
                                getMinSummary(
                                        context, ((ListPreference) preference).getEntries()[id],
                                        (String) newValue));

                        String preferenceKey = isTracks
                                               ? com.nextgis.maplib.util.SettingsConstants.KEY_PREF_TRACKS_MIN_TIME
                                               : com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_TIME;
                        preference.getSharedPreferences()
                                .edit()
                                .putString(preferenceKey, (String) newValue)
                                .commit();

                        sectionWork(preference.getContext(), isTracks);

                        return true;
                    }
                });

        minDistance.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        int id = ((ListPreference) preference).findIndexOfValue((String) newValue);
                        preference.setSummary(
                                getMinSummary(
                                        context, ((ListPreference) preference).getEntries()[id],
                                        (String) newValue));

                        String preferenceKey = isTracks
                                               ? com.nextgis.maplib.util.SettingsConstants.KEY_PREF_TRACKS_MIN_DISTANCE
                                               : com.nextgis.maplib.util.SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE;
                        preference.getSharedPreferences()
                                .edit()
                                .putString(preferenceKey, (String) newValue)
                                .commit();

                        sectionWork(preference.getContext(), isTracks);

                        return true;
                    }
                });
    }


    public static void initializeAccurateTaking(EditTextPreference accurateMaxCount)
    {
        accurateMaxCount.setSummary(accurateMaxCount.getText());

        accurateMaxCount.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        preference.setSummary((CharSequence) newValue);
                        return true;
                    }
                });
    }


    protected static void sectionWork(
            Context context,
            boolean isTracks)
    {
        if (!isTracks) {
            Activity parent = (Activity) context;
            MainApplication application = (MainApplication) parent.getApplication();
            application.getGpsEventSource().updateActiveListeners();
        } else {
            if (isTrackerServiceRunning(context)) {
                Toast.makeText(
                        context, context.getString(R.string.tracks_reload), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private static String getMinSummary(
            Context context,
            CharSequence newEntry,
            String newValue)
    {
        int value = 0;

        try {
            value = Integer.parseInt(newValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String addition = newEntry + "";
        addition += value == 0 ? context.getString(R.string.frequentest) : "";

        return addition;
    }
}
