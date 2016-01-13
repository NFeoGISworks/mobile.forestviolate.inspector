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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IDocumentFeatureSource;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;

import java.util.Map;


/**
 * Read only view of sheet
 */
public class SheetViewFragment
        extends TabFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_sheet_view, container, false);

        Activity activity = getActivity();

        if (activity instanceof IDocumentFeatureSource) {

            IDocumentFeatureSource documentSource = (IDocumentFeatureSource) activity;
            DocumentFeature feature = documentSource.getFeature();

            if (null != feature) {

                TextView author = (TextView) view.findViewById(R.id.author);
                author.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER));

                TextView createDateTime = (TextView) view.findViewById(R.id.creation_datetime);
                createDateTime.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE));

                TextView territory = (TextView) view.findViewById(R.id.territory);
                territory.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));

                // Signature
                for (Map.Entry<String, AttachItem> entry : feature.getAttachments().entrySet()) {
                    AttachItem attachItem = entry.getValue();

                    if (attachItem.getDisplayName().equals(Constants.SIGN_FILENAME)) {
                        String aid = attachItem.getAttachId();
                        Uri attachUri = Uri.parse(
                                "content://" + SettingsConstants.AUTHORITY + "/" +
                                Constants.KEY_LAYER_DOCUMENTS + "/" + feature.getId() + "/" +
                                        com.nextgis.maplib.util.Constants.URI_ATTACH + "/" + aid);

                        Cursor cursor = getActivity().getContentResolver().query(
                                attachUri, new String[] {VectorLayer.ATTACH_DATA}, null, null,
                                null);

                        if (null != cursor && cursor.moveToFirst()) {
                            String signPath = cursor.getString(0);

                            ImageView imageView = (ImageView) view.findViewById(R.id.sign);
                            Bitmap bm = BitmapFactory.decodeFile(signPath);
                            imageView.setImageBitmap(bm);

                            cursor.close();
                        }
                        break;
                    }
                }
            }
        }

        return view;
    }
}
