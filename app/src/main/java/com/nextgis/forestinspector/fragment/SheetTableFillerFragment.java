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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.DaveKoelle.AlphanumComparator;
import com.justsimpleinfo.Table.Table;
import com.justsimpleinfo.Table.TableData;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.SheetTableFillerActivity;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static com.nextgis.maplib.util.Constants.TAG;


public class SheetTableFillerFragment
        extends Fragment
        implements SheetTableFillerActivity.OnSaveTableDataListener
{
    protected final static int CREATE_TABLE_DONE   = 0;
    protected final static int CREATE_TABLE_OK     = 1;
    protected final static int CREATE_TABLE_FAILED = 2;

    protected Table mTable;

    protected TextView         mTableWarning;
    protected LinearLayout     mTableLayout;
    protected AppCompatSpinner mHeightView;
    protected AppCompatSpinner mCategoryView;

    protected ArrayAdapter<String> mHeightAdapter;
    protected ArrayAdapter<String> mCategoryAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        MapBase map = MapBase.getInstance();
        DocumentsLayer docs = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docs = (DocumentsLayer) layer;
                break;
            }
        }

        if (null != docs) {
            mHeightAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_HEIGHT_TYPES, true);
            mCategoryAdapter = getArrayAdapter(docs, Constants.KEY_LAYER_TREES_TYPES, false);
        }
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sheet_table_filler, null);
        mTableLayout = (LinearLayout) view.findViewById(R.id.table_layout);
        mTableWarning = (TextView) view.findViewById(R.id.table_warning);

        mHeightView = (AppCompatSpinner) view.findViewById(R.id.height);
        mHeightView.setAdapter(mHeightAdapter);

        mCategoryView = (AppCompatSpinner) view.findViewById(R.id.category);
        mCategoryView.setAdapter(mCategoryAdapter);

        if (null != mTable) {
            mTableWarning.setVisibility(View.GONE);
            mTableLayout.addView(mTable);
            return view;
        }

        final Handler handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case CREATE_TABLE_DONE:
                        break;

                    case CREATE_TABLE_OK:
                        mTable = (Table) msg.obj;
                        mTableWarning.setVisibility(View.GONE);
                        mTableLayout.addView(mTable);
                        break;

                    case CREATE_TABLE_FAILED:
                        Toast.makeText(
                                getActivity(),
                                "SheetTableFillerFragment create table ERROR: " + msg.obj,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        RunnableFuture<Table> future = new FutureTask<Table>(
                new Callable<Table>()
                {
                    @Override
                    public Table call()
                            throws Exception
                    {
                        return new Table(getActivity());
                    }
                })
        {
            @Override
            protected void done()
            {
                super.done();
                handler.sendEmptyMessage(CREATE_TABLE_DONE);
            }


            @Override
            protected void set(Table result)
            {
                super.set(result);
                Message msg = handler.obtainMessage(CREATE_TABLE_OK, result);
                msg.sendToTarget();
            }


            @Override
            protected void setException(Throwable t)
            {
                super.setException(t);

                String error = t.getLocalizedMessage();
                Log.d(TAG, error);
                t.printStackTrace();

                Message msg = handler.obtainMessage(CREATE_TABLE_FAILED, error);
                msg.sendToTarget();
            }
        };

        new Thread(future).start();

        return view;
    }


    @Override
    public void onDestroyView()
    {
        mTableLayout.removeView(mTable);
        super.onDestroyView();
    }


    protected ArrayAdapter<String> getArrayAdapter(
            DocumentsLayer docsLayer,
            String layerKey,
            boolean numberSort)
    {
        NGWLookupTable table = (NGWLookupTable) docsLayer.getLayerByName(layerKey);

        if (null != table) {
            Map<String, String> data = table.getData();
            List<String> dataArray = new ArrayList<>();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                dataArray.add(entry.getKey());
            }

            if (numberSort) {
                Collections.sort(dataArray, new AlphanumComparator());
            } else {
                Collections.sort(dataArray);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.simple_spinner_item, dataArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            return adapter;
        }

        return null;
    }



    @Override
    public void onSaveTableDataListener()
    {
        TableData tableData = mTable.getTableData();
        // TODO:
    }
}
