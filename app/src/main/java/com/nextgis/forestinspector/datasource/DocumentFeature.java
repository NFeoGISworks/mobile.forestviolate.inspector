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

package com.nextgis.forestinspector.datasource;

import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The document feature
 */
public class DocumentFeature
        extends Feature
{

    protected Map<String, List<Feature>> mSubFeatures;


    public DocumentFeature(
            long id,
            List<Field> fields)
    {
        super(id, fields);
        mSubFeatures = new HashMap<>();
    }


    public DocumentFeature(Feature other)
    {
        super(other);
        mSubFeatures = new HashMap<>();
    }


    public DocumentFeature(DocumentFeature other)
    {
        super(other);
        mSubFeatures = other.getSubFeatures();
    }


    @Override
    public void setId(long id)
    {
        super.setId(id);
        for (Map.Entry<String, List<Feature>> entry : mSubFeatures.entrySet()) {
            for (Feature feature : entry.getValue()) {
                feature.setFieldValue(Constants.FIELD_DOC_ID, id);
            }
        }
    }


    public void addSubFeature(
            String layerName,
            Feature feature)
    {
        List<Feature> features;

        if (mSubFeatures.containsKey(layerName)) {
            features = mSubFeatures.get(layerName);
        } else {
            features = new ArrayList<>();
        }

        feature.setFieldValue(Constants.FIELD_DOC_ID, getId());
        features.add(feature);

        mSubFeatures.put(layerName, features);
    }


    public int getSubFeaturesCount(String layerName)
    {
        if (mSubFeatures.containsKey(layerName)) {
            return mSubFeatures.get(layerName).size();
        }
        return 0;
    }


    public List<Feature> getSubFeatures(String layerName)
    {
        return mSubFeatures.get(layerName);
    }


    public Map<String, List<Feature>> getSubFeatures()
    {
        return mSubFeatures;
    }
}
