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

package com.nextgis.forestinspector.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;

/**
 * Read only view of Indictment
 */
public class IndictmentViewFragment extends TabFragment {

    protected DocumentFeature mFeature;

    public IndictmentViewFragment() {
    }

    @SuppressLint("ValidFragment")
    public IndictmentViewFragment(String name, DocumentFeature feature) {
        super(name);

        mFeature = feature;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_indictment, container, false);

        if(null != mFeature) {
            TextView author = (TextView) view.findViewById(R.id.author);
            author.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER));

            TextView createDateTime = (TextView) view.findViewById(R.id.create_datetime);
            createDateTime.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE));

            TextView place = (TextView) view.findViewById(R.id.place);
            place.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_PLACE));

            TextView violationType = (TextView) view.findViewById(R.id.violation_type);
            violationType.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE));

            TextView codNum = (TextView) view.findViewById(R.id.code_num);
            codNum.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_LAW));

            TextView territory = (TextView) view.findViewById(R.id.territory);
            territory.setText(mFeature.getTerritoryText(getString(R.string.forestry),
                    getString(R.string.district_forestry), getString(R.string.parcel),
                    getString(R.string.unit)));

            TextView forestCatType = (TextView) view.findViewById(R.id.forest_cat_type);
            forestCatType.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_VIOLATE));

            TextView who = (TextView) view.findViewById(R.id.who);
            who.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_USER_PICK));

            TextView when = (TextView) view.findViewById(R.id.when);
            when.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DATE_VIOLATE));

            TextView crime = (TextView) view.findViewById(R.id.crime);
            crime.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_CRIME));

            TextView peopleInfo = (TextView) view.findViewById(R.id.people_info);
            peopleInfo.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_DETECTOR));

            TextView crimeSay = (TextView) view.findViewById(R.id.crime_say);
            crimeSay.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_CRIME));

            TextView peopleSay = (TextView) view.findViewById(R.id.people_say);
            peopleSay.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESC_AUTHOR));

            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(mFeature.getFieldValueAsString(Constants.FIELD_DOCUMENTS_DESCRIPTION));

            // TODO: 28.07.15 Signature
        }

        return view;
    }
}
