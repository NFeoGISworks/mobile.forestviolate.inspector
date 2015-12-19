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

package com.nextgis.forestinspector.dialog;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.SimpleDividerItemDecoration;
import com.nextgis.forestinspector.adapter.TargetingListAdapter;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class TargetingDialog
        extends StyledDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
                   TargetingListAdapter.OnSelectionChangedListener
{
    protected static String KEY_QUERY    = "query";
    protected static String NOT_SELECTED = "-1";

    protected RecyclerView         mListView;
    protected TargetingListAdapter mAdapter;
    protected OnSelectListener     mOnSelectListener;

    protected boolean mLoaderIsInit = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setCancelable(false);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);

        mAdapter = new TargetingListAdapter(mContextThemeWrapper, null);
        mAdapter.setSingleSelectable(true);
        mAdapter.addOnSelectionChangedListener(this);

        handleIntent(getActivity().getIntent());
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_list, null);
        mListView = (RecyclerView) view.findViewById(R.id.list);
        mListView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mListView.setHasFixedSize(true);
        mListView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mListView.setAdapter(mAdapter);

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_action_bookmark_outline);
        } else {
            setIcon(R.drawable.ic_action_action_bookmark_outline);
        }

        setTitle(R.string.targeting_selection);
        setView(view, false);
        setPositiveText(R.string.ok);
        setNegativeText(R.string.cancel);

        setOnPositiveClickedListener(
                new OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        onSelectTargeting();
                    }
                });

        setOnNegativeClickedListener(
                new OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        onCancelTargeting();
                    }
                });


        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mButtonPositive.setEnabled(false);

        return rootView;
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    private void handleIntent(Intent intent)
    {
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            //use the query to search your data
//            // TODO: change query
//            String fullQuery = Constants.FIELD_CADASTRE_LV + " LIKE '%" + query + "%' OR " +
//                               Constants.FIELD_CADASTRE_ULV + " LIKE '%" + query + "%' OR " +
//                               Constants.FIELD_CADASTRE_PARCEL + " LIKE '%" + query + "%'";
//
//            Bundle bundle = new Bundle();
//            bundle.putString(KEY_QUERY, fullQuery);
//
//            if (mLoaderIsInit) {
//                getLoaderManager().restartLoader(0, bundle, this);
//            } else {
//                getLoaderManager().initLoader(0, bundle, this);
//                mLoaderIsInit = true;
//            }
//
//        } else {
            if (mLoaderIsInit) {
                getLoaderManager().restartLoader(0, null, this);
            } else {
                getLoaderManager().initLoader(0, null, this);
                mLoaderIsInit = true;
            }
//        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args)
    {
        Uri uri = Uri.parse(
                "content://" + SettingsConstants.AUTHORITY + "/" + Constants.KEY_LAYER_FV);

        String[] projection = {
                com.nextgis.maplib.util.Constants.FIELD_ID,
                Constants.FIELD_FV_DATE,
                Constants.FIELD_FV_FORESTRY,
                Constants.FIELD_FV_PRECINCT,
                Constants.FIELD_FV_REGION,
                Constants.FIELD_FV_TERRITORY,
                Constants.FIELD_FV_OBJECTID};

        String sortOrder = Constants.FIELD_FV_DATE + " DESC";

        if (args == null) {
            return new CursorLoader(getActivity(), uri, projection, null, null, sortOrder);
        } else {
            return new CursorLoader(
                    getActivity(), uri, projection, " " + args.getString(KEY_QUERY), null,
                    sortOrder);
        }
    }


    @Override
    public void onLoadFinished(
            Loader<Cursor> loader,
            Cursor data)
    {
        if (null != data) {
            mAdapter.swapCursor(data);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mAdapter.swapCursor(null);
    }


    protected void onSelectTargeting()
    {
        Integer id = mAdapter.getCurrentSingleSelectedItemId();


        Cursor cursor = mAdapter.getItem(id);

        String objectId =
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_FV_OBJECTID));

        if (null != mOnSelectListener) {
            mOnSelectListener.onSelect(objectId);
        }
    }


    protected void onCancelTargeting()
    {
        if (null != mOnSelectListener) {
            mOnSelectListener.onSelect(NOT_SELECTED);
        }
    }


    public void setOnSelectListener(OnSelectListener listener)
    {
        mOnSelectListener = listener;
    }


    @Override
    public void onSelectionChanged(
            int position,
            boolean selection)
    {
        if (!mButtonPositive.isEnabled()) {
            mButtonPositive.setEnabled(true);
        }
    }


    public interface OnSelectListener
    {
        void onSelect(String objectId);
    }
}
