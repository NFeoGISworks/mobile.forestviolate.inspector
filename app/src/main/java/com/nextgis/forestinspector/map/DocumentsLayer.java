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

package com.nextgis.forestinspector.map;

import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.INGWLayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.NGWVectorLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.nextgis.maplib.util.Constants.FIELD_ID;
import static com.nextgis.maplib.util.Constants.JSON_LAYERS_KEY;
import static com.nextgis.maplib.util.Constants.JSON_PATH_KEY;
import static com.nextgis.maplib.util.Constants.LAYER_PREFIX;

/**
 * documents layer class for specific needs (sync, relationship tables, etc.)
 */
public class DocumentsLayer extends NGWVectorLayer {
    protected List<ILayer> mLayers;
    protected LayerFactory mLayerFactory;

    public DocumentsLayer(Context context, File path,
                          LayerFactory layerFactory) {
        super(context, path);

        mLayerFactory = layerFactory;
        mLayers = new ArrayList<>();

        mLayerType = Constants.LAYERTYPE_DOCS;
    }

    public DocumentFeature getFeature(long featureId){
        Cursor cur = query(null, FIELD_ID + " = " + featureId, null, null, null);
        if(null == cur){
            return null;
        }
        cur.moveToFirst();

        DocumentFeature feature = new DocumentFeature(featureId, getFields());
        feature.fromCursor(cur);

        cur.close();

        //get documents connected with this one
        cur = query(null, Constants.FIELD_DOCUMENTS_PARENT_ID + " = " + featureId, null, null, null);
        if(null != cur && cur.moveToFirst()){
            do {
                Feature subFeature = new Feature(-1, getFields());
                subFeature.fromCursor(cur);
                feature.addSubFeature(getName(), subFeature);
            }while (cur.moveToNext());
            cur.close();
        }

        //get connected layers

        for(int i = 0; i < mLayers.size(); ++i){
            ILayer layer = mLayers.get(i);
            if(layer instanceof NGWVectorLayer){
                NGWVectorLayer subLayer = (NGWVectorLayer) layer;
                cur = subLayer.query(null, Constants.FIELD_DOCUMENTS_PARENT_ID + " = " + featureId,
                        null, null, null);
                if(null != cur && cur.moveToFirst()){
                    do {
                        Feature subFeature = new Feature(-1, subLayer.getFields());
                        subFeature.fromCursor(cur);
                        feature.addSubFeature(layer.getName(), subFeature);
                    } while (cur.moveToNext());
                    cur.close();
                }
            }
        }

        return feature;
    }

    @Override
    public void sync(
            String authority,
            SyncResult syncResult)
    {
        if (0 != (mSyncType & com.nextgis.maplib.util.Constants.SYNC_NONE) || !mIsInitialized) {
            return;
        }

        // sync lookup tables
        for (ILayer layer : mLayers) {
            if (layer instanceof NGWLookupTable) {
                NGWLookupTable ngwLayer = (NGWLookupTable) layer;
                ngwLayer.sync(authority, syncResult);
            }
        }

        // 1. get remote changes
        if (!getChangesFromServer(authority, syncResult)) {
            Log.d(Constants.FITAG, "Get remote changes failed");
            return;
        }

        // 2. send current changes
        if (!sendLocalChanges(syncResult)) {
            Log.d(Constants.FITAG, "Set local changes failed");
            //return;
        }
    }

    @Override
    protected boolean sendLocalChanges(SyncResult syncResult) throws SQLiteException {
        // TODO: 25.07.15
        // 1. send first doc
        // 2. update doc id
        // 3. send relation records from other layers
        // 4. update doc status (add change for next sync)
        // 5. repeat 1 - 4 for other docs

        return false;
        //return super.sendLocalChanges(syncResult);
    }

    @Override
    protected boolean getChangesFromServer(String authority, SyncResult syncResult) throws SQLiteException {
        // TODO: 25.07.15
        // 1. get docs
        // 2. get other layers

        return false;
        //return super.getChangesFromServer(authority, syncResult);
    }

    public ILayer getLayerByName(String name)
    {
        if (mName.equals(name)) {
            return this;
        }
        for (ILayer layer : mLayers) {
            if (layer.getName().equals(name)) {
                return layer;
            }
        }
        return null;
    }

    public void addLayer(ILayer layer)
    {
        if (layer != null) {
            mLayers.add(layer);
            layer.setParent(this);
        }
    }

    @Override
    public boolean delete()
    {
        for (ILayer layer : mLayers) {
            layer.setParent(null);
            layer.delete();
        }

        return super.delete();
    }

    @Override
    public JSONObject toJSON()
            throws JSONException
    {
        JSONObject rootConfig = super.toJSON();

        JSONArray jsonArray = new JSONArray();
        rootConfig.put(JSON_LAYERS_KEY, jsonArray);
        for (ILayer layer : mLayers) {
            JSONObject layerObject = new JSONObject();
            layerObject.put(JSON_PATH_KEY, layer.getPath().getName());
            jsonArray.put(layerObject);
        }

        return rootConfig;
    }

    public void clearLayers()
    {
        for (ILayer layer : mLayers) {
            if (layer instanceof LayerGroup) {
                ((LayerGroup) layer).clearLayers();
            }
        }

        mLayers.clear();
    }


    @Override
    public void fromJSON(JSONObject jsonObject)
            throws JSONException
    {
        super.fromJSON(jsonObject);

        clearLayers();

        final JSONArray jsonArray = jsonObject.getJSONArray(JSON_LAYERS_KEY);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonLayer = jsonArray.getJSONObject(i);
            String sPath = jsonLayer.getString(JSON_PATH_KEY);
            File inFile = new File(getPath(), sPath);
            if (inFile.exists()) {
                ILayer layer = mLayerFactory.createLayer(mContext, inFile);
                if (null != layer && layer.load()) {
                    addLayer(layer);
                }
            }
        }
    }

    public File createLayerStorage(String layerName)
    {
        if(TextUtils.isEmpty(layerName))
            return createLayerStorage();
        return new File(mPath, layerName);
    }

    public File createLayerStorage()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String layerDir = LAYER_PREFIX + sdf.format(new Date()) + getLayerCount();
        final Random r = new Random();
        layerDir += r.nextInt(99);
        return new File(mPath, layerDir);
    }

    public int getLayerCount()
    {
        return mLayers.size();
    }

    @Override
    public boolean save()
    {
        for (ILayer layer : mLayers) {
            layer.save();
        }
        return super.save();
    }
}
