/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 * ****************************************************************************
 * Copyright (c) 2015. NextGIS, info@nextgis.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.dialog;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.SelectTerritoryActivity;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class InputParcelTextDialog
        extends StyledDialogFragment
{
    protected static final String KEY_TEXT           = "text";
    protected static final String KEY_CLOSE_ACTIVITY = "key_close_activity";

    protected EditText    mParcelTextEditor;
    protected RadioButton mRbFromParcels;
    protected RadioButton mRbInputText;

    protected EventListener mEventListener;

    protected boolean mCloseActivity = false;


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(KEY_TEXT, mParcelTextEditor.getText().toString());
        outState.putBoolean(KEY_CLOSE_ACTIVITY, mCloseActivity);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (mCloseActivity) {
            FragmentActivity activity = getActivity();

            if (activity instanceof EventListener) {
                setEventListener((EventListener) activity);
            } else {
                throw new RuntimeException("Activity must implement EventListener.");
            }
        }
    }


    @Override
    public void onDetach()
    {
        setEventListener(null);

        super.onDetach();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        setCancelable(false);
        setThemeDark(isAppThemeDark());

        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
        String parcelText;
        if (null != savedInstanceState) {
            parcelText = savedInstanceState.getString(KEY_TEXT);
            mCloseActivity = savedInstanceState.getBoolean(KEY_CLOSE_ACTIVITY);
        } else {
            parcelText = activity.getTerritoryText();
            mCloseActivity = false;
        }


        View view = inflateThemedLayout(R.layout.dialog_choose_parcels_text);
        mParcelTextEditor = (EditText) view.findViewById(R.id.parcel_text_editor);
        mRbFromParcels = (RadioButton) view.findViewById(R.id.ck_from_parcels);
        mRbInputText = (RadioButton) view.findViewById(R.id.ck_input_text);


        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_edit_light);
        } else {
            setIcon(R.drawable.ic_action_edit_light);
        }

        setTitle(R.string.input_territory_text);
        setView(view, true);
        setPositiveText(R.string.save);
        setNegativeText(R.string.cancel);


        if (!TextUtils.isEmpty(parcelText)) {
            mParcelTextEditor.setText(parcelText);
        }

        View.OnClickListener radioListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RadioButton rb = (RadioButton) v;

                switch (rb.getId()) {
                    case R.id.ck_from_parcels:
                    default:
                        mParcelTextEditor.setEnabled(false);
                        break;

                    case R.id.ck_input_text:
                        mParcelTextEditor.setEnabled(true);
                        break;
                }
            }
        };

        mRbFromParcels.setOnClickListener(radioListener);
        mRbInputText.setOnClickListener(radioListener);


        setOnPositiveClickedListener(new OnPositiveClickedListener()
        {
            @Override
            public void onPositiveClicked()
            {
                SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();

                if (mRbFromParcels.isChecked()) {
                    activity.setTerritoryTextByGeom();
                    if (null != mEventListener) {
                        mEventListener.onParcelTextSelected();
                    }
                } else {
                    activity.setTerritoryText(mParcelTextEditor.getText().toString());
                    if (null != mEventListener) {
                        mEventListener.onEditorTextSelected();
                    }
                }
            }
        });

        setOnNegativeClickedListener(new OnNegativeClickedListener()
        {
            @Override
            public void onNegativeClicked()
            {
                SelectTerritoryActivity activity = (SelectTerritoryActivity) getActivity();
                activity.clearTerritoryGeometry();
                if (null != mEventListener) {
                    mEventListener.onCancelDialog();
                }
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


    public void setCloseActivity(boolean closeActivity)
    {
        mCloseActivity = closeActivity;
    }


    public void setEventListener(EventListener listener)
    {
        mEventListener = listener;
    }


    public interface EventListener
    {
        void onParcelTextSelected();

        void onEditorTextSelected();

        void onCancelDialog();
    }
}