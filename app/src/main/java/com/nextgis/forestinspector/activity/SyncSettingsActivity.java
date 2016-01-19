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

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.fragment.SyncSettingsFragment;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.activity.NGWLoginActivity;
import com.nextgis.maplibui.activity.NGWSettingsActivity;
import com.nextgis.maplibui.util.ControlHelper;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.util.List;

import static com.nextgis.maplibui.util.SettingsConstantsUI.KEY_PREF_SYNC_PERIOD_SEC_LONG;


public class SyncSettingsActivity
        extends NGWSettingsActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // without headers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = getIntent();
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            intent.putExtra(
                    PreferenceActivity.EXTRA_SHOW_FRAGMENT, SyncSettingsFragment.class.getName());
        }

        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication) getApplication();
        setOnDeleteAccountListener(app);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return SyncSettingsFragment.class.getName().equals(fragmentName);
    }


    @Override
    protected void createView()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            MainApplication app = (MainApplication) getApplication();
            Account account = app.getAccount();

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
            fillAccountPreferences(screen, account);

            setPreferenceScreen(screen);
        }
    }


    @Override
    public void fillAccountPreferences(
            PreferenceScreen screen,
            Account account)
    {
        final IGISApplication application = (IGISApplication) getApplicationContext();

        // add sync settings group
        PreferenceCategory syncCategory = new PreferenceCategory(this);
        syncCategory.setTitle(com.nextgis.maplibui.R.string.sync);
        screen.addPreference(syncCategory);

        // add auto sync property
        addAutoSyncProperty(application, account, syncCategory);

        // add time for periodic sync
        addPeriodicSyncTime(application, account, syncCategory);

        // add actions group
        PreferenceCategory actionCategory = new PreferenceCategory(this);
        actionCategory.setTitle(com.nextgis.maplibui.R.string.actions);
        screen.addPreference(actionCategory);

        if (null == account) {
            // add "Add account" action
            addAddAccountAction(actionCategory);

        } else {
            // add "Edit account" action
            addEditAccountAction(application, account, actionCategory);

            // add "Delete account" action
            addDeleteAccountAction(application, account, actionCategory);
        }
    }


    @Override
    protected void addAutoSyncProperty(
            final IGISApplication application,
            final Account account,
            PreferenceCategory syncCategory)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(
                Constants.PREFERENCES, Constants.MODE_MULTI_PROCESS);

        CheckBoxPreference enablePeriodicSync = new CheckBoxPreference(this);
        enablePeriodicSync.setKey(SettingsConstantsUI.KEY_PREF_SYNC_PERIODICALLY);
        enablePeriodicSync.setTitle(com.nextgis.maplibui.R.string.auto_sync);

        boolean isAccountSyncEnabled = isAccountSyncEnabled(account, application.getAuthority());
        enablePeriodicSync.setChecked(isAccountSyncEnabled);

        enablePeriodicSync.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        boolean isChecked = (boolean) newValue;
                        setAccountSyncEnabled(account, application.getAuthority(), isChecked);
                        return true;
                    }
                });

        long timeStamp =
                sharedPreferences.getLong(SettingsConstants.KEY_PREF_LAST_SYNC_TIMESTAMP, 0);
        if (isAccountSyncEnabled && timeStamp > 0) {
            enablePeriodicSync.setSummary(ControlHelper.getSyncTime(this, timeStamp));
        } else {
            enablePeriodicSync.setSummary(com.nextgis.maplibui.R.string.auto_sync_summary);
        }

        syncCategory.addPreference(enablePeriodicSync);
    }


    @Override
    protected void addPeriodicSyncTime(
            final IGISApplication application,
            final Account account,
            PreferenceCategory syncCategory)
    {
        String prefValue = "" + Constants.DEFAULT_SYNC_PERIOD;

        if (null != account) {
            List<PeriodicSync> syncs =
                    ContentResolver.getPeriodicSyncs(account, application.getAuthority());

            if (null != syncs && !syncs.isEmpty()) {
                for (PeriodicSync sync : syncs) {
                    Bundle bundle = sync.extras;
                    long period =
                            bundle.getLong(KEY_PREF_SYNC_PERIOD_SEC_LONG, Constants.NOT_FOUND);
                    if (period > 0) {
                        prefValue = "" + period;
                        break;
                    }
                }
            }
        }

        final ListPreference timeInterval = new ListPreference(this);
        timeInterval.setKey(SettingsConstantsUI.KEY_PREF_SYNC_PERIOD);
        timeInterval.setTitle(R.string.sync_interval);
        timeInterval.setDialogTitle(com.nextgis.maplibui.R.string.sync_set_interval);
        timeInterval.setEntries(R.array.sync_periods);
        timeInterval.setEntryValues(R.array.sync_periods_val);

        final CharSequence[] keys = timeInterval.getEntries();
        final CharSequence[] values = timeInterval.getEntryValues();

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(prefValue)) {
                timeInterval.setValueIndex(i);
                timeInterval.setSummary(keys[i]);
                break;
            }
        }

        timeInterval.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        long value = Long.parseLong(newValue.toString());
                        int id = ((ListPreference) preference).findIndexOfValue(
                                (String) newValue);
                        CharSequence summary = ((ListPreference) preference).getEntries()[id];
                        preference.setSummary(summary);

                        preference.getSharedPreferences().edit().putLong(
                                SettingsConstantsUI.KEY_PREF_SYNC_PERIOD_SEC_LONG, value).commit();

                        MainApplication app = (MainApplication) getApplication();

                        final Account account = app.getAccount(getString(R.string.account_name));
                        ContentResolver.addPeriodicSync(
                                account, app.getAuthority(), Bundle.EMPTY, value);

                        return true;
                    }
                });

        syncCategory.addPreference(timeInterval);
        timeInterval.setDependency(SettingsConstantsUI.KEY_PREF_SYNC_PERIODICALLY);
    }


    protected void addAddAccountAction(PreferenceCategory actionCategory)
    {
        Preference preference = new Preference(this);
        preference.setTitle(com.nextgis.maplibui.R.string.add_account);
        preference.setSummary(com.nextgis.maplibui.R.string.add_account_summary);

        Intent intent = new Intent(this, SyncLoginActivity.class);
        intent.putExtra(NGWLoginActivity.FOR_NEW_ACCOUNT, true);
        preference.setIntent(intent);

        actionCategory.addPreference(preference);
    }


    @Override
    protected void addEditAccountAction(
            final IGISApplication application,
            final Account account,
            PreferenceCategory actionCategory)
    {
        Preference preferenceEdit = new Preference(this);
        preferenceEdit.setTitle(R.string.edit_account);
        preferenceEdit.setSummary(R.string.edit_account_summary);

        String url = application.getAccountUrl(account);
        String login = application.getAccountLogin(account);

        Intent intent = new Intent(this, SyncLoginActivity.class);
        intent.putExtra(NGWLoginActivity.FOR_NEW_ACCOUNT, false);
        intent.putExtra(NGWLoginActivity.ACCOUNT_URL_TEXT, url);
        intent.putExtra(NGWLoginActivity.ACCOUNT_LOGIN_TEXT, login);
        intent.putExtra(NGWLoginActivity.CHANGE_ACCOUNT_URL, false);
        intent.putExtra(NGWLoginActivity.CHANGE_ACCOUNT_LOGIN, false);
        preferenceEdit.setIntent(intent);

        actionCategory.addPreference(preferenceEdit);
    }


    @Override
    protected void deleteAccountLayers(
            final IGISApplication application,
            Account account)
    {
        super.deleteAccountLayers(application, account);
        MapBase map = application.getMap();
        FileUtil.deleteRecursive(map.getPath());
    }


    @Override
    protected void onDeleteAccount()
    {
        // do nothing
    }


    @Override
    public void onAccountsUpdated(Account[] accounts)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            MainApplication app = (MainApplication) getApplication();
            Account account = app.getAccount();

            PreferenceScreen screen = getPreferenceScreen();

            if (null != screen) {
                screen.removeAll();
                fillAccountPreferences(screen, account);
            }
//        } else {
//            invalidateHeaders();
//            setSelection(0);
        }
    }


    @Override
    protected Preference addDeleteAccountAction(
            final IGISApplication application,
            Account account,
            PreferenceCategory actionCategory)
    {
        Preference preferenceDelete = super.addDeleteAccountAction(
                application, account, actionCategory);

        if (null == preferenceDelete) {
            return null;
        }

        MapBase map = application.getMap();
        final boolean isChanges = map.isChanges();
        final boolean haveFeaturesNotSyncFlag = map.haveFeaturesNotSyncFlag();

        final Preference.OnPreferenceClickListener oldClickListener =
                preferenceDelete.getOnPreferenceClickListener();

        final AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(SyncSettingsActivity.this);

        dialogBuilder.setIcon(com.nextgis.maplibui.R.drawable.ic_action_warning)
                .setTitle(R.string.warning_not_sent_data)
                .setMessage(
                        isChanges
                        ? R.string.warning_not_sent_data_msg
                        : R.string.warning_saved_not_sent_data_msg)

                .setNegativeButton(
                        R.string.delete, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {
                                if (null != oldClickListener) {
                                    oldClickListener.onPreferenceClick(null);
                                }
                            }
                        });

        if (isChanges) {
            dialogBuilder.setPositiveButton(
                    R.string.send, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which)
                        {
                            MainApplication app = (MainApplication) application;
                            app.runSync();
                            finish();
                        }
                    });
        } else {
            dialogBuilder.setPositiveButton(R.string.cancel, null);
        }


        preferenceDelete.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        if (isChanges || haveFeaturesNotSyncFlag) {
                            dialogBuilder.show();
                        } else {
                            oldClickListener.onPreferenceClick(null);
                        }

                        return true;
                    }
                });


        return preferenceDelete;
    }
}
