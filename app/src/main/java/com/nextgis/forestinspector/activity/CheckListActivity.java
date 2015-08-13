/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.CheckListAdapter;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;

import java.util.ArrayList;

/**
 * Created by bishop on 07.08.15.
 */
public abstract class CheckListActivity extends FIActivity implements IDocumentFeatureSource, ActionMode.Callback {
    protected DocumentEditFeature mDocumentFeature;
    protected CheckListAdapter mAdapter;
    protected ArrayList<Integer> mIds;
    protected ActionMode mActionMode;
    protected static Handler mHandler;
    protected boolean mSelectState = false;
    protected ListView mList;

    protected static final String BUNDLE_SELECTED_ITEMS = "selected_items";
    protected static final String BUNDLE_IS_CHECKED_KEY = "is_checked";
    protected static final String BUNDLE_TAG_KEY = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewId());
        setToolbar(R.id.main_toolbar);

        final View add = findViewById(R.id.add);
        if (null != add) {
            add.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            add();
                        }
                    });
        }

        MainApplication app = (MainApplication) getApplication();
        mDocumentFeature = app.getTempFeature();

        mIds = new ArrayList<>();

        if (savedInstanceState != null) {
            mIds = savedInstanceState.getIntegerArrayList(BUNDLE_SELECTED_ITEMS);
        }

        mAdapter = getAdapter();
        mList = (ListView) findViewById(R.id.list);
        mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mList.setItemsCanFocus(false);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        CheckBox cb = (CheckBox) view.findViewById(R.id.check);
                        boolean isChecked = !cb.isChecked();
                        cb.setChecked(isChecked);
                        onUpdateSelectedItems(isChecked, position);
                    }
                });

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle resultData = msg.getData();
                updateSelectedItems(resultData.getBoolean(BUNDLE_IS_CHECKED_KEY), resultData.getInt(BUNDLE_TAG_KEY));
            }
        };
    }

    protected abstract int getContentViewId();
    protected abstract void add();
    protected abstract CheckListAdapter getAdapter();

    public void onUpdateSelectedItems(boolean isChecked, int tag){
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_IS_CHECKED_KEY, isChecked);
        bundle.putInt(BUNDLE_TAG_KEY, tag);

        Message msg = new Message();
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putIntegerArrayList(BUNDLE_SELECTED_ITEMS, mIds);
    }

    @Override
    public DocumentFeature getFeature() {
        return mDocumentFeature;
    }

    protected void updateSelectedItems(
            boolean isChecked,
            int id)
    {
        if (isChecked) {
            if (!mIds.contains(id)) {
                mIds.add(id);
            }
        } else {
            mIds.remove((Integer)id);
        }

        if (!mIds.isEmpty() && mActionMode == null) {
            mActionMode = startSupportActionMode(this);
        }

        if (mActionMode != null) {
            if (mIds.isEmpty()) {
                mActionMode.finish();
            } else {
                mActionMode.setTitle("" + mIds.size());
            }
        }


        mAdapter.notifyDataSetInvalidated();
    }

    @Override
    public boolean onCreateActionMode(
            ActionMode actionMode,
            Menu menu)
    {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.check_list, menu);


        ActionBar bar = getSupportActionBar();
        if (null != bar) {
            bar.hide();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode)
    {
        mSelectState = false;
        setSelection();
        mActionMode = null;
        ActionBar bar = getSupportActionBar();
        if (null != bar) {
            bar.show();
        }
    }


    @Override
    public boolean onPrepareActionMode(
            ActionMode actionMode,
            Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked(
            ActionMode actionMode,
            MenuItem menuItem)
    {
        int id = menuItem.getItemId();

        if (id == com.nextgis.maplibui.R.id.menu_delete) {
            if (mIds.size() > 0) {

                for(int featureId : mIds){
                    deleteFeature(featureId);
                }

                mIds.clear();
            } else {
                Toast.makeText(
                        getApplicationContext(), com.nextgis.maplibui.R.string.nothing_selected, Toast.LENGTH_SHORT)
                        .show();
            }

            actionMode.finish();
        } else if (id == com.nextgis.maplibui.R.id.menu_select_all) {
            mSelectState = !mSelectState;
            setSelection();
        }
        return true;
    }

    protected void setSelection()
    {
        for (int i = 0; i < mList.getCount(); i++) {
            View parent = mAdapter.getView(i, null, mList);
            CheckBox checkBox = (CheckBox) parent.findViewById(R.id.check);

            if (mSelectState != checkBox.isChecked()) {
                mList.performItemClick(parent, i, i);
            }
        }
        mList.invalidateViews();
    }

    public boolean isChecked(int position) {
        return mIds.contains(position);
    }

    protected void deleteFeature(int nFeaturePos){
        mAdapter.deleteFeature(nFeaturePos);
    }
}
