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
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoMultiPolygon;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.GeoPolygon;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.GeoUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;


public class DocumentEditFeature extends DocumentFeature {
    protected List<Long> mParcelIds;
    protected long       mNewAttachId;


    public DocumentEditFeature(
            long id,
            List<Field> fields)
    {
        super(id, fields);

        mParcelIds = new ArrayList<>();
        mNewAttachId = 0;
    }


    public DocumentEditFeature(DocumentFeature other)
    {
        super(other);

        mParcelIds = new ArrayList<>();
        mNewAttachId = getMaxAttachId() + 1;
    }


    public List<Long> getParcelIds()
    {
        return mParcelIds;
    }

    public static DocumentsLayer getDocumentsLayer(){
        MapBase map = MapBase.getInstance();
        return (DocumentsLayer) map.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
    }

    public String getTerritoryTextByGeom(
            String area,
            String district,
            String parcel,
            String unit)
    {
        if(null == mGeometry)
            return "";

        DocumentsLayer docsLayer = getDocumentsLayer();
        if (docsLayer == null)
            return "";

        MapBase map = MapBase.getInstance();
        VectorLayer parcelsLayer =
                (VectorLayer) map.getLayerByPathName(Constants.KEY_LAYER_KV);

        GeoEnvelope env = mGeometry.getEnvelope();

        List<Long> res = parcelsLayer.query(env);
        String where = getWhereClauseForParcelIds(res);
        if (null == where || TextUtils.isEmpty(where))
            return "";

        String columns[] = {FIELD_GEOM, Constants.FIELD_CADASTRE_LV, Constants.FIELD_CADASTRE_ULV, Constants.FIELD_CADASTRE_KV};
        Cursor cursor = parcelsLayer.query(columns, " " + where, null, null, null);
        Map<String, Map<String, String>> data = new HashMap<>();

        if (cursor.moveToFirst()) {
            do {
                try {
                    String sLv = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV));
                    String sUlv = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV));
                    String sParcel = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_KV));


                    String key = sLv + " " + area + " " + sUlv + " " + district;
                    String value_u = "";
                    String key_u = parcel + " " + sParcel;
                    if (data.containsKey(key)) {
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
                catch ( IllegalArgumentException e){
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }

        cursor.close();

        return formParcelText(data);
    }

    public String getTerritoryText(
            String area,
            String district,
            String parcel,
            String unit)
    {

        String where = getWhereClauseForParcelIds();
        if (null == where || TextUtils.isEmpty(where))
            return "";

        MapBase map = MapBase.getInstance();
        DocumentsLayer docsLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docsLayer = (DocumentsLayer) layer;
                break;
            }
        }

        if (docsLayer == null)
            return "";

        VectorLayer parcelsLayer =
                (VectorLayer) map.getLayerByPathName(Constants.KEY_LAYER_KV);

        String columns[] = {FIELD_GEOM, Constants.FIELD_CADASTRE_LV, Constants.FIELD_CADASTRE_ULV, Constants.FIELD_CADASTRE_KV};
        Cursor cursor = parcelsLayer.query(null, " " + where, null, null, null);
        //GeoEnvelope env = new GeoEnvelope();
        GeoMultiPolygon multiPolygon = new GeoMultiPolygon();
        Map<String, Map<String, String>> data = new HashMap<>();

        if (cursor.moveToFirst()) {
            do {
                try {
                    GeoGeometry geom = GeoGeometryFactory.fromBlob(
                            cursor.getBlob(cursor.getColumnIndex(FIELD_GEOM)));
                    if(geom.getType() == GeoConstants.GTPolygon){
                        multiPolygon.add(geom);
                    }
                    else if(geom.getType() == GeoConstants.GTMultiPolygon){
                      GeoMultiPolygon otherMultiPolygon = (GeoMultiPolygon) geom;
                        multiPolygon.add(otherMultiPolygon.getGeometry(0));
                    }
                    //env.merge(geom.getEnvelope());

                    String sLv = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_LV));
                    String sUlv = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_ULV));
                    String sParcel = cursor.getString(
                            cursor.getColumnIndexOrThrow(Constants.FIELD_CADASTRE_KV));


                    String key = sLv + " " + area + " " + sUlv + " " + district;
                    String value_u = "";
                    String key_u = parcel + " " + sParcel;
                    if (data.containsKey(key)) {
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

        //if(env.isInit())
        //    setGeometryFromEnvelope(env);
        mGeometry = multiPolygon;

        return formParcelText(data);
    }

    protected String formParcelText(Map<String, Map<String, String>> data) {
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
            result += " ";
        }

        return result;
    }

    public void setUnionGeometryFromLayer(String layerName){
        List<Feature> featureList = mSubFeatures.get(layerName);
        if(null == featureList)
            return;

        // Convex hull
        List<GeoPoint> points = new ArrayList<>();

        for(Feature feature : featureList){
            GeoMultiPoint pt = (GeoMultiPoint) feature.getGeometry();
            for(int i = 0; i < pt.size(); ++i)
                points.add(pt.get(i));
        }

        GeoPolygon polygon = GeoUtil.convexHull(points);
        GeoMultiPolygon multiPolygon = new GeoMultiPolygon();
        multiPolygon.add(polygon);
        mGeometry = multiPolygon;
        //setGeometryFromEnvelope(env);
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
        return getWhereClauseForParcelIds(getParcelIds());
    }


    public String getWhereClauseForParcelIds(List<Long> ids)
    {
        StringBuilder sb = new StringBuilder(1024);

        for (Long fid : ids) {
            if (sb.length() == 0) {
                sb.append(com.nextgis.maplib.util.Constants.FIELD_ID);
                sb.append(" IN (");
            } else {
                sb.append(",");
            }
            sb.append(fid);
        }

        if (sb.length() > 0) {
            sb.append(")");
        }

        return sb.toString();
    }


    @Override
    public void addAttachment(AttachItem item)
    {
        String attachId = item.getAttachId();
        if (attachId.equals("-1")) {
            attachId = "" + mNewAttachId;
            ++mNewAttachId;
        }

        mAttachments.put(attachId, item);
    }
}
