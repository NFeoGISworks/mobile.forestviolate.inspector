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

import android.annotation.SuppressLint;
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
 * Read only view of Indictment
 */
public class IndictmentViewFragment extends TabFragment {

    public IndictmentViewFragment() {
    }

    @SuppressLint("ValidFragment")
    public IndictmentViewFragment(String name) {
        super(name);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_indictment_view, container, false);

        Activity activity = getActivity();
        if(activity instanceof IDocumentFeatureSource) {
            IDocumentFeatureSource documentFeatureSource = (IDocumentFeatureSource) activity;
            DocumentFeature feature = documentFeatureSource.getFeature();

            if (null != feature) {
                TextView author = (TextView) view.findViewById(R.id.author);
                author.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER));

                TextView createDateTime = (TextView) view.findViewById(R.id.create_datetime);
                createDateTime.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE));

                TextView place = (TextView) view.findViewById(R.id.place);
                place.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_PLACE));

                TextView violationType = (TextView) view.findViewById(R.id.violation_type);
                violationType.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE));

                TextView codNum = (TextView) view.findViewById(R.id.code_num);
                codNum.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_LAW));

                TextView territory = (TextView) view.findViewById(R.id.territory);
                territory.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));

                TextView forestCatType = (TextView) view.findViewById(R.id.forest_cat_type);
                forestCatType.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE));

                String datePick = feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE_PICK);
                String userPick = feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_PICK);
                String whoText = datePick + ", " + userPick;

                TextView who = (TextView) view.findViewById(R.id.who);
                who.setText(whoText);

                TextView when = (TextView) view.findViewById(R.id.when);
                when.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE_VIOLATE));

                TextView crime = (TextView) view.findViewById(R.id.crime);
                crime.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_CRIME));

                TextView peopleInfo = (TextView) view.findViewById(R.id.detector_say);
                peopleInfo.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));

                TextView crimeSay = (TextView) view.findViewById(R.id.crime_say);
                crimeSay.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_CRIME));

                TextView peopleSay = (TextView) view.findViewById(R.id.author_say);
                peopleSay.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));

                TextView description = (TextView) view.findViewById(R.id.description);
                description.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESCRIPTION));

                TextView userTrans = (TextView) view.findViewById(R.id.user_trans);
                String userTransText = userTrans.getText().toString();
                userTransText += ": " + feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_TRANS);
                userTrans.setText(userTransText);

                // Signature
                for(Map.Entry<String, AttachItem> entry : feature.getAttachments().entrySet()){
                    AttachItem attachItem = entry.getValue();
                    if(attachItem.getDisplayName().equals(Constants.SIGN_FILENAME)){
                        String aid = attachItem.getAttachId();
                        Uri attachUri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/"
                                + Constants.KEY_LAYER_DOCUMENTS + "/"
                                + feature.getId() + "/"
                                + "attach" + "/" + aid);

                        Cursor cursor = getActivity().getContentResolver().query(attachUri,
                                new String[] {VectorLayer.ATTACH_DATA}, null, null, null);
                        if(null != cursor && cursor.moveToFirst()) {
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
