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

package com.nextgis.forestinspector.datasource;

import android.database.Cursor;
import android.text.TextUtils;

import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.Field;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.datasource.GeoMultiPolygon;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.GeoPolygon;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;

/**
 * Created by bishop on 03.08.15.
 */
public class DocumentEditFeature extends DocumentFeature {
    protected List<Long> mParcelIds;

    public DocumentEditFeature(long id, List<Field> fields) {
        super(id, fields);

        mParcelIds = new ArrayList<>();
    }

    public List<Long> getParcelIds() {
        return mParcelIds;
    }

    public String getTerritoryText(String area, String district, String parcel, String unit) {

        String where = getWhereClauseForParcelIds();
        if(null == where || TextUtils.isEmpty(where))
            return "";

        MapBase map = MapBase.getInstance();
        DocumentsLayer docsLayer = null;
        for(int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if(docsLayer == null)
            return "";

        VectorLayer parcelsLayer = (VectorLayer) map.getLayerByPathName(Constants.KEY_LAYER_CADASTRE);

        Cursor cursor = parcelsLayer.query(null, " " + where, null, null, null);
        GeoEnvelope env = new GeoEnvelope();
        Map<String, Map<String, String>> data = new HashMap<>();

        if(cursor.moveToFirst()){
            do{
                try {
                    GeoGeometry geom = GeoGeometryFactory.fromBlob(cursor.getBlob(cursor.getColumnIndex(FIELD_GEOM)));
                    env.merge(geom.getEnvelope());

                    String sLv = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV));
                    String sUlv = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV));
                    String sParcel = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_PARCEL));


                    String key = sLv + " " + area + " " + sUlv + " " + district;
                    String value_u = "";
                    String key_u = parcel + " " + sParcel;
                    if(data.containsKey(key)) {
                        Map<String, String> data_u = data.get(key);
                        if(data_u.containsKey(key_u)){
                            data_u.put(key_u, data_u.get(key_u) + ", " + value_u);
                        }
                        else{
                            data_u.put(key_u, value_u);
                        }
                    }
                    else {
                        Map<String, String> data_u = new HashMap<>();
                        data_u.put(key_u, value_u);
                        data.put(key, data_u);
                    }
                }
                catch (IOException | ClassNotFoundException | IllegalArgumentException e){
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }

        cursor.close();

        if(env.isInit())
            setGeometryFromEnvelope(env);

        String result = "";
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            result += entry.getKey() + " ";
            Map<String, String> data_u = entry.getValue();
            boolean firstIteration = true;
            for(Map.Entry<String, String> entry_u : data_u.entrySet()) {
                if(!firstIteration)
                    result += ", ";
                else
                    firstIteration = false;
                result += entry_u.getKey();  //TODO when videl be set + " " + unit + " " + entry_u.getValue() + ", ";
            }
        }

        return result;
    }

    public void setUnionGeometryFromLayer(String layerName){
        List<Feature> featureList = mSubFeatures.get(layerName);
        if(null == featureList)
            return;

        // TODO: 04.08.15 Convex hull

        GeoEnvelope env = new GeoEnvelope();
        for(Feature feature : featureList){
            env.merge(feature.getGeometry().getEnvelope());
        }

        setGeometryFromEnvelope(env);
    }

    protected void setGeometryFromEnvelope(GeoEnvelope env){
        GeoPolygon polygon = new GeoPolygon();
        polygon.add(new GeoPoint(env.getMinX(), env.getMinY()));
        polygon.add(new GeoPoint(env.getMinX(), env.getMaxY()));
        polygon.add(new GeoPoint(env.getMaxX(), env.getMaxY()));
        polygon.add(new GeoPoint(env.getMaxX(), env.getMinY()));
        polygon.add(new GeoPoint(env.getMinX(), env.getMinY()));
        GeoMultiPolygon multiPolygon = new GeoMultiPolygon();
        multiPolygon.add(polygon);
        mGeometry = multiPolygon;
    }

    public String getWhereClauseForParcelIds(){
        String fullQuery = "";
        for(Long fid : getParcelIds()){
            if(!TextUtils.isEmpty(fullQuery))
                fullQuery += " OR ";
            fullQuery += com.nextgis.maplib.util.Constants.FIELD_ID + " = " + fid;
        }
        return fullQuery;
    }

    @Override
    public void setId(long id) {
        super.setId(id);
        for(Map.Entry<String, List<Feature>> entry : mSubFeatures.entrySet()){
            for(Feature feature : entry.getValue()){
                feature.setFieldValue(Constants.FIELD_DOC_ID, id);
            }
        }
    }
}
