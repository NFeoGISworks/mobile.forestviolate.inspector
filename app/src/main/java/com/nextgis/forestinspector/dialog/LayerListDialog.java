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

package com.nextgis.forestinspector.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.LayerListAdapter;
import com.nextgis.forestinspector.adapter.SimpleDividerItemDecoration;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class LayerListDialog
        extends StyledDialogFragment
{
    protected RecyclerView     mList;
    protected LayerListAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        MainApplication app = (MainApplication) activity.getApplication();
        MapDrawable map = (MapDrawable) app.getMap();
        mAdapter = new LayerListAdapter(activity, map);
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflateThemedLayout(R.layout.fragment_list);

        mList = (RecyclerView) view.findViewById(R.id.list);
        mList.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mList.setHasFixedSize(true);
        mList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mList.setAdapter(mAdapter);


        if (isThemeDark()) {
            setIcon(R.drawable.ic_maps_layers);
        } else {
            setIcon(R.drawable.ic_maps_layers);
        }

        setView(view, false);

        setTitle(R.string.layer_props);
        setPositiveText(R.string.ok);

        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        // close
                    }
                });

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }
}
