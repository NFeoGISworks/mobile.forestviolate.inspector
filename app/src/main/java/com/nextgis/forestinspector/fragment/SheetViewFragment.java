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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.IDocumentFeatureSource;
import com.nextgis.forestinspector.adapter.SheetViewListAdapter;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;

import java.util.List;


public class SheetViewFragment
        extends TabFragment
{

    public SheetViewFragment()
    {
    }


    @SuppressLint("ValidFragment")
    public SheetViewFragment(String name)
    {
        super(name);
    }


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_sheetview, container, false);

        Activity activity = getActivity();

        if (activity instanceof IDocumentFeatureSource) {
            IDocumentFeatureSource documentFeatureSource = (IDocumentFeatureSource) activity;
            DocumentFeature feature = documentFeatureSource.getFeature();

            if (null != feature) {

                List<Feature> features = feature.getSubFeatures(Constants.KEY_LAYER_SHEET);
                SheetViewListAdapter adapter = new SheetViewListAdapter(getActivity(), features);
                ListView list = (ListView) view.findViewById(R.id.treeList);
                list.setAdapter(adapter);
            }
        }

        return view;
    }
}
