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
 * Read only view of field works
 */
public class FieldWorksViewFragment
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
        final View view = inflater.inflate(R.layout.fragment_field_works_view, container, false);

        Activity activity = getActivity();

        if (activity instanceof IDocumentFeatureSource) {

            IDocumentFeatureSource documentSource = (IDocumentFeatureSource) activity;
            DocumentFeature feature = documentSource.getFeature();

            if (null != feature) {

                TextView author = (TextView) view.findViewById(R.id.author);
                author.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER));

                TextView territory = (TextView) view.findViewById(R.id.territory);
                territory.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_TERRITORY));

                TextView createDateTime = (TextView) view.findViewById(R.id.creation_datetime);
                createDateTime.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE));

                TextView createPlace = (TextView) view.findViewById(R.id.creation_place);
                createPlace.setText(feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_PLACE));

                TextView representative = (TextView) view.findViewById(R.id.representative);
                representative.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_TRANS));

                TextView fieldWorkType = (TextView) view.findViewById(R.id.field_work_type);
                fieldWorkType.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE));

                TextView contract = (TextView) view.findViewById(R.id.contract);
                contract.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_PICK));

                TextView contractTypeSpinner = (TextView) view.findViewById(R.id.contract_type);
                contractTypeSpinner.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE_VIOLATE));

                TextView contractDate = (TextView) view.findViewById(R.id.contract_date);
                contractDate.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_CONTRACT_DATE));

                TextView contractNumber = (TextView) view.findViewById(R.id.contract_number);
                contractNumber.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_LAW));

                TextView cuttingAreaCleanQuality = (TextView) view.findViewById(R.id.cutting_area_clean_quality);
                cuttingAreaCleanQuality.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));

                TextView cuttingAreaCultivationQuality = (TextView) view.findViewById(R.id.cutting_area_cultivation_quality);
                cuttingAreaCultivationQuality.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));

                TextView violation = (TextView) view.findViewById(R.id.violation);
                violation.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE));

                TextView infringerOrganisationName = (TextView) view.findViewById(R.id.infringer_organisation_name);
                infringerOrganisationName.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_CRIME));

                TextView infringerFullName = (TextView) view.findViewById(R.id.infringer_full_name);
                infringerFullName.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_CRIME));

                TextView infringerLivingPlace = (TextView) view.findViewById(R.id.infringer_living_place);
                infringerLivingPlace.setText(
                        feature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESCRIPTION));


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
