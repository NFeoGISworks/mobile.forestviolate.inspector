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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.nextgis.forestinspector.R;
import com.nextgis.maplibui.activity.NGActivity;


/**
 * Base class for NextGIS preferences activity
 */
// http://stackoverflow.com/questions/32070186/how-to-use-the-v7-v14-preference-support-library
// http://developer.android.com/intl/ru/reference/android/support/v7/preference/PreferenceFragmentCompat.html
// http://stackoverflow.com/a/32540395/4727406
public abstract class NGPreferenceActivity
        extends NGActivity
{
    protected boolean mIsPaused = false;


    protected abstract String getFragmentTag();

    protected abstract PreferenceFragmentCompat getPreferenceFragment();


    protected void setCurrentThemePref()
    {
        // do nothing
    }


    protected void refreshCurrentTheme()
    {
        // do nothing
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setToolbar(R.id.main_toolbar);

        ViewGroup root = ((ViewGroup) findViewById(android.R.id.content));
        if (null != root) {
            View content = root.getChildAt(0);
            if (null != content) {
                LinearLayout toolbarContainer = (LinearLayout) View.inflate(
                        this, R.layout.activity_settings, null);

                root.removeAllViews();
                toolbarContainer.addView(content);
                root.addView(toolbarContainer);

                Toolbar toolbar = (Toolbar) toolbarContainer.findViewById(R.id.main_toolbar);
                toolbar.setTitleTextColor(getResources().getColor(R.color.textColorPrimary_Dark));
                toolbar.getBackground().setAlpha(255);
                toolbar.setTitle(getTitle());
                toolbar.setNavigationIcon(R.drawable.ic_action_home_light);

                toolbar.setNavigationOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                NGPreferenceActivity.this.finish();
                            }
                        });


            }
        }

        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            final FragmentManager fm = getSupportFragmentManager();

            PreferenceFragmentCompat fragment =
                    (PreferenceFragmentCompat) fm.findFragmentByTag(getFragmentTag());
            if (fragment == null) {
                fragment = getPreferenceFragment();
            }

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.setting_fragment, fragment, getFragmentTag());
            ft.commit();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
