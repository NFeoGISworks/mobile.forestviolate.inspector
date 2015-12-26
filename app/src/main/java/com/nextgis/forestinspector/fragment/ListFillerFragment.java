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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.ListFillerAdapter;
import com.nextgis.forestinspector.dialog.ListFillerDialog;
import com.nextgis.forestinspector.util.Constants;

import java.io.IOException;

import static com.nextgis.maplib.util.Constants.TAG;


public abstract class ListFillerFragment
        extends ListViewerFragment
        implements ListFillerAdapter.OnSelectionChangedListener, ActionMode.Callback,
                   ListFillerDialog.OnAddListener
{
    protected View mAddButton;

    protected ActionMode mActionMode;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mIsListViewer = false;
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mAddButton = view.findViewById(R.id.add_button);
        if (null != mAddButton) {
            if (mIsListViewer) {
                mAddButton.setVisibility(View.GONE);
            } else {
                mAddButton.setVisibility(View.VISIBLE);
                mAddButton.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (mAddButton.isEnabled()) {
                                    addItem();
                                }
                            }
                        });
            }
        }

        if (null != mAdapter) {
            mAdapter.addOnSelectionChangedListener(this);
        }

        return view;
    }


    @Override
    public void onDestroyView()
    {
        if (null != mAdapter) {
            mAdapter.removeOnSelectionChangedListener(this);
        }

        super.onDestroyView();
    }


    @Override
    public void onSelectionChanged(
            int position,
            boolean selection)
    {
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
            mActionMode.setTitle("" + mAdapter.getSelectedItemCount());

        } else if (!mAdapter.hasSelectedItems()) {
            mActionMode.setTitle("");
            mActionMode.finish();

        } else {
            mActionMode.setTitle("" + mAdapter.getSelectedItemCount());
        }
    }


    @Override
    public boolean onCreateActionMode(
            ActionMode mode,
            Menu menu)
    {
        mAddButton.setVisibility(View.GONE);
        mode.getMenuInflater().inflate(R.menu.list_actionmode, menu);
        return true;
    }


    @Override
    public boolean onPrepareActionMode(
            ActionMode mode,
            Menu menu)
    {
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        mAdapter.clearSelectionForAll();
        mActionMode = null;

        if (!mIsListViewer) {
            mAddButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onActionItemClicked(
            final ActionMode mode,
            MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {

            case R.id.menu_delete:

                Snackbar undoBar = Snackbar.make(
                        getView(), R.string.sel_items_will_be_deleted, Snackbar.LENGTH_LONG)
                        .setAction(
                                R.string.cancel, new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        // cancel
                                    }
                                })
                        .setCallback(
                                new Snackbar.Callback()
                                {
                                    @Override
                                    public void onDismissed(
                                            Snackbar snackbar,
                                            int event)
                                    {
                                        switch (event) {
                                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                                break;

                                            case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                                            case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                                            case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                            default:
                                                try {
                                                    mAdapter.deleteAllSelected();
                                                    mode.finish();
                                                } catch (IOException e) {
                                                    String error = e.getLocalizedMessage();
                                                    Log.d(TAG, error);
                                                    e.printStackTrace();
                                                    Toast.makeText(
                                                            getActivity(), error, Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                                break;

                                        }

                                        super.onDismissed(snackbar, event);
                                    }
                                })
                        .setActionTextColor(
                                getResources().getColor(R.color.color_undobar_action_text));

                View undoBarView = undoBar.getView();
                undoBarView.setBackgroundColor(
                        getResources().getColor(R.color.color_undobar_background));
                TextView tv = (TextView) undoBarView.findViewById(
                        android.support.design.R.id.snackbar_text);
                tv.setTextColor(getResources().getColor(R.color.color_undobar_text));

                undoBar.show();
                return true;

            case R.id.menu_select_all:
                mAdapter.toggleSelectionForAll();
                return true;

            default:
                return false;
        }
    }


    @Override
    public void onItemClick(int position)
    {
        ListFillerDialog dialog = getFillerDialog();
        dialog.setOnAddListener(this);
        dialog.setFeature(mAdapter.getFeature(position));
        dialog.show(
                getActivity().getSupportFragmentManager(), Constants.FRAGMENT_LIST_FILLER_DIALOG);
    }


    protected void addItem()
    {
        ListFillerDialog dialog = getFillerDialog();
        dialog.setOnAddListener(this);
        dialog.show(
                getActivity().getSupportFragmentManager(), Constants.FRAGMENT_LIST_FILLER_DIALOG);
    }


    protected abstract ListFillerDialog getFillerDialog();


    public void onAdd()
    {
        mAdapter.notifyDataSetChanged();
    }
}
