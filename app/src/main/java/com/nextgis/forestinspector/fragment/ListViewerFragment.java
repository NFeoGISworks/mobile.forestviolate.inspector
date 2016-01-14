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

package com.nextgis.forestinspector.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.ListFillerAdapter;
import com.nextgis.forestinspector.adapter.SimpleDividerItemDecoration;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;


public abstract class ListViewerFragment
        extends TabFragment
        implements ListFillerAdapter.ViewHolder.OnItemClickListener
{
    protected RecyclerView      mList;
    protected ListFillerAdapter mAdapter;

    protected boolean mIsListViewer = true;


    protected abstract ListFillerAdapter getFillerAdapter(DocumentFeature feature);


    protected RecyclerView.LayoutManager getListLayoutManager()
    {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        Activity activity = getActivity();
        Bundle extras = activity.getIntent().getExtras();
        DocumentFeature documentFeature = null;

        if (null != extras) {
            if (extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
                long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
                boolean isDocViewer = extras.getBoolean(Constants.DOCUMENT_VIEWER);

                MainApplication app = (MainApplication) activity.getApplication();

                if (isDocViewer) {
                    DocumentsLayer docs = app.getDocsLayer();
                    documentFeature = docs.getFeatureWithAttaches(featureId);
                } else {
                    documentFeature = app.getEditFeature(featureId);
                }

            }
        }

        if (null != documentFeature) {
            mAdapter = getFillerAdapter(documentFeature);
            mAdapter.setOnItemClickListener(this);
        }
    }


    protected int getFragmentViewResId()
    {
        return R.layout.fragment_list;
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(getFragmentViewResId(), null);
        mList = (RecyclerView) view.findViewById(R.id.list);
        mList.setLayoutManager(getListLayoutManager());
        mList.setHasFixedSize(true);

        if (null != mAdapter) {
            mList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
            mList.setAdapter(mAdapter);
        }

        return view;
    }


    @Override
    public void onItemClick(int position)
    {
        // do nothing
    }
}
