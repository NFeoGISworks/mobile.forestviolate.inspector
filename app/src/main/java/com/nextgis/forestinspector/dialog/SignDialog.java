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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplibui.dialog.StyledDialogFragment;
import com.nextgis.maplibui.formcontrol.Sign;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import java.io.File;
import java.io.IOException;


public class SignDialog
        extends StyledDialogFragment
{
    protected OnSignListener mOnSignListener;


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
        View view = inflateThemedLayout(R.layout.dialog_sign);
        final Sign sign = (Sign) view.findViewById(R.id.sign);

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_edit_light);
        } else {
            setIcon(R.drawable.ic_action_edit_light);
        }

        setTitle(R.string.sign_and_send);
        setView(view, false);
        setNegativeText(R.string.fix);
        setPositiveText(R.string.save);

        setOnNegativeClickedListener(
                new ListFillerDialog.OnNegativeClickedListener()
                {
                    @Override
                    public void onNegativeClicked()
                    {
                        // cancel
                    }
                });

        setOnPositiveClickedListener(
                new ListFillerDialog.OnPositiveClickedListener()
                {
                    @Override
                    public void onPositiveClicked()
                    {
                        MainApplication app = (MainApplication) getActivity().getApplication();
                        File temPath = app.getDocFeatureFolder();
                        FileUtil.createDir(temPath);
                        File signFile = new File(temPath, Constants.SIGN_FILENAME);
                        try {
                            sign.save(194, 102, false, signFile);

                            if (null != mOnSignListener) {
                                mOnSignListener.onSign(signFile);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
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


    public void setOnSignListener(OnSignListener onSignListener)
    {
        mOnSignListener = onSignListener;
    }


    public interface OnSignListener
    {
        void onSign(File signatureFile);
    }
}
