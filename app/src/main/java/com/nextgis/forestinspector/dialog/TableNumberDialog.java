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
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.InputFilterMinMaxInteger;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.util.SettingsConstantsUI;


public class TableNumberDialog
        extends StyledDialogFragment
{
    protected EditText mNumberView;
    protected String   mNumberText;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
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
        View view = inflateThemedLayout(R.layout.dialog_table_number);
        mNumberView = (EditText) view.findViewById(R.id.number);
        mNumberView.setFilters(new InputFilter[] {new InputFilterMinMaxInteger(0, 10000)});
        mNumberView.setText(mNumberText);

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_edit_light);
        } else {
            setIcon(R.drawable.ic_action_edit_light);
        }

        setTitle(R.string.input_amount);
        setView(view, true);
        setPositiveText(R.string.ok);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    public void setNumberText(String numberText)
    {
        mNumberText = numberText;
    }


    public String getText()
    {
        return mNumberView.getText().toString();
    }
}
