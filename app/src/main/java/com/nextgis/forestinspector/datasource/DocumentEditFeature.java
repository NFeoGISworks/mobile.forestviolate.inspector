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
import android.os.DropBoxManager;
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

    @Override
    public String getTerritoryText(String area, String district, String parcel, String unit) {
        fillTerritoryLayer();
        return super.getTerritoryText(area, district, parcel, unit);
    }

    protected void fillTerritoryLayer(){
        if(mParcelIds.size() > 0){
            List<Feature> territoryArray = mSubFeatures.get(Constants.KEY_LAYER_TERRITORY);
            if(null != territoryArray)
                territoryArray.clear();
            else
                territoryArray = new ArrayList<>();

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
                return;

            VectorLayer territoryLayer = (VectorLayer) docsLayer.getLayerByName(Constants.KEY_LAYER_TERRITORY);
            VectorLayer parcelsLayer = (VectorLayer) map.getLayerByPathName(Constants.KEY_LAYER_CADASTRE);

            Cursor cursor = parcelsLayer.query(null, " " + getWhereClauseForParcelIds(), null, null, null);
            if(cursor.moveToFirst()){
                do{
                    try {
                        Feature feature = new Feature(com.nextgis.maplib.util.Constants.NOT_FOUND, territoryLayer.getFields());
                        GeoGeometry geom = GeoGeometryFactory.fromBlob(cursor.getBlob(cursor.getColumnIndex(FIELD_GEOM)));
                        feature.setGeometry(geom);

                        String lv = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV));
                        String ulv = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV));
                        String parcel = cursor.getString(cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_PARCEL));

                        feature.setFieldValue(Constants.FIELD_TERRITORY_AREA, ulv);
                        feature.setFieldValue(Constants.FIELD_TERRITORY_DISTRICT, lv);
                        feature.setFieldValue(Constants.FIELD_TERRITORY_PARCEL, parcel);

                        territoryArray.add(feature);
                    }
                    catch (IOException | ClassNotFoundException | IllegalArgumentException e){
                        e.printStackTrace();
                    }
                }while (cursor.moveToNext());
            }

            cursor.close();
            mSubFeatures.put(Constants.KEY_LAYER_TERRITORY, territoryArray);
        }
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
