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

import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplibui.formcontrol.Sign;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.styled_dialog.StyledDialogFragment;

import java.io.File;
import java.io.IOException;


public class SignDialog
        extends StyledDialogFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        setKeepInstance(true);
        super.onCreate(savedInstanceState);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_sign, null);
        final Sign sign = (Sign) view.findViewById(R.id.sign);

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_image_edit);
        } else {
            setIcon(R.drawable.ic_action_image_edit);
        }

        setThemeDark(isAppThemeDark());

        if (isThemeDark()) {
            setIcon(R.drawable.ic_action_image_edit);
        } else {
            setIcon(R.drawable.ic_action_image_edit);
        }

        setTitle(R.string.sign_and_save);
        setView(view);
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
                        File sigFile = new File(temPath, Constants.SIGN_FILENAME);
                        try {
                            sign.save(194, 102, false, sigFile);
                            onCreateDocument();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return super.onCreateDialog(savedInstanceState);
    }


    // TODO: this is hack, make it via GISApplication
    public boolean isAppThemeDark()
    {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsConstantsUI.KEY_PREF_THEME, "light")
                .equals("dark");
    }


    private void onCreateDocument()
    {
        //save picture to file and add it as attachment

        MainApplication app = (MainApplication) getActivity().getApplication();
        MapBase mapBase = app.getMap();
        DocumentsLayer documentsLayer = null;
        //get documents layer
        for (int i = 0; i < mapBase.getLayerCount(); i++) {
            ILayer layer = mapBase.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                documentsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if (null == documentsLayer) {
            Toast.makeText(
                    getActivity(), getString(R.string.error_documents_layer_not_found),
                    Toast.LENGTH_LONG).show();
            return;
        }

        DocumentEditFeature feature = app.getTempFeature();
        AttachItem signAttach = new AttachItem(
                "-1", Constants.SIGN_FILENAME, "image/png", Constants.SIGN_DESCRIPTION);
        feature.addAttachment(signAttach);

        if (documentsLayer.insert(feature)) {
            //remove temp feature
            app.setTempFeature(null);
            getActivity().finish();
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_db_insert), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
