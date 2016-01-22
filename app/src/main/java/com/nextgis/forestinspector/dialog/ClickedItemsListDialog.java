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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.ClickedItemsListAdapter;
import com.nextgis.forestinspector.adapter.DocumentsListItem;
import com.nextgis.forestinspector.adapter.DocumentsListLoader;
import com.nextgis.forestinspector.adapter.SimpleDividerItemDecoration;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.util.List;


public class ClickedItemsListDialog
        extends StyledDialogFragment
        implements LoaderManager.LoaderCallbacks<List<DocumentsListItem>>
{
    protected List<Long> mDocsIds;
    protected List<Long> mNotesIds;
    protected List<Long> mTargetsIds;

    protected MapBase mMap;

    protected ClickedItemsListAdapter mAdapter;

    protected ClickedItemsListAdapter.OnListItemClickListener mAdapterOnListItemClickListener;

    public void setDocsIds(List<Long> docsIds)
    {
        mDocsIds = docsIds;
    }


    public void setNotesIds(List<Long> notesIds)
    {
        mNotesIds = notesIds;
    }


    public void setTargetsIds(List<Long> targetsIds)
    {
        mTargetsIds = targetsIds;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);

        MainApplication app = (MainApplication) getActivity().getApplication();
        mMap = app.getMap();

        mAdapter = new ClickedItemsListAdapter(getActivity());
        mAdapter.setOnListItemClickListener(mAdapterOnListItemClickListener);

        runLoader();
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflateThemedLayout(R.layout.fragment_main);

        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);

        list.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        list.setHasFixedSize(false);
        list.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        list.setAdapter(mAdapter);

        if (isThemeDark()) {
            setIcon(R.drawable.abc_ic_menu_selectall_mtrl_alpha);
        } else {
            setIcon(R.drawable.abc_ic_menu_selectall_mtrl_alpha);
        }

        setView(view, false);

        setTitle(R.string.selected_items);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    private void runLoader()
    {
        Loader loader = getLoaderManager().getLoader(Constants.CLICKED_LOADER);
        if (null != loader && loader.isStarted()) {
            getLoaderManager().restartLoader(Constants.CLICKED_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(Constants.CLICKED_LOADER, null, this);
        }
    }


    @Override
    public Loader<List<DocumentsListItem>> onCreateLoader(
            int id,
            Bundle args)
    {
        return new DocumentsListLoader(getActivity(), mDocsIds, mNotesIds, mTargetsIds);
    }


    @Override
    public void onLoadFinished(
            Loader<List<DocumentsListItem>> loader,
            List<DocumentsListItem> documents)
    {
        mAdapter.setDocuments(documents);
    }


    @Override
    public void onLoaderReset(Loader<List<DocumentsListItem>> loader)
    {
        mAdapter.setDocuments(null);
    }


    public void setAdapderOnListItemClickListener(
            ClickedItemsListAdapter.OnListItemClickListener onListItemClickListener)
    {
        mAdapterOnListItemClickListener = onListItemClickListener;
    }
}
