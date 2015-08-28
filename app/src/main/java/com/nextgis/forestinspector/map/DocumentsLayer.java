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

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.FeatureChanges;
import com.nextgis.maplib.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.nextgis.maplib.util.Constants.CHANGE_OPERATION_CHANGED;
import static com.nextgis.maplib.util.Constants.CHANGE_OPERATION_NEW;
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
        cur = query(null, Constants.FIELD_DOC_ID + " = " + featureId, null, null, null);
        if(null != cur && cur.moveToFirst()){
            int idPos = cur.getColumnIndex(FIELD_ID);
            List<Long> ids = new ArrayList<>();
            do {
                ids.add(cur.getLong(idPos));
            }while (cur.moveToNext());
            cur.close();

            for(Long id : ids) {
                DocumentFeature subDocFeature = getFeature(id);
                if (null != subDocFeature)
                    feature.addSubFeature(Constants.KEY_LAYER_DOCUMENTS, subDocFeature);
            }
        }

        //get connected layers

        for(int i = 0; i < mLayers.size(); ++i){
            ILayer layer = mLayers.get(i);
            if(layer instanceof NGWVectorLayer){
                NGWVectorLayer subLayer = (NGWVectorLayer) layer;
                cur = subLayer.query(null, Constants.FIELD_DOC_ID + " = " + featureId,
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
    protected void changeFeatureId(long oldFeatureId, long newFeatureId) {
        super.changeFeatureId(oldFeatureId, newFeatureId);
        //update doc id in other layers
        for(ILayer layer : mLayers) {
            if (layer instanceof NGWVectorLayer) {
                NGWVectorLayer ngwVectorLayer = (NGWVectorLayer) layer;
                Cursor cur = ngwVectorLayer.query(new String[] { com.nextgis.maplib.util.Constants.FIELD_ID },
                        Constants.FIELD_DOC_ID + " = " + oldFeatureId, null, null, null);
                List<Long> ids = new ArrayList<>();
                if(null != cur && cur.moveToFirst()) {
                    do {
                        ids.add(cur.getLong(0));
                    } while (cur.moveToNext());
                    cur.close();
                }

                for(Long id : ids){
                    ContentValues values = new ContentValues();
                    values.put(Constants.FIELD_DOC_ID, newFeatureId);
                    Uri uri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/" +
                            ngwVectorLayer.getPath().getName() + "/" + id);
                    ngwVectorLayer.update(uri, values, null, null);
                }
            }
        }
    }

    @Override
    public boolean sendLocalChanges(SyncResult syncResult) throws SQLiteException {
        // send docs
        if(!super.sendLocalChanges(syncResult))
            return false;

        // send relation records from other layers
        boolean hasChanges = false;
        for(ILayer layer : mLayers){
            if(layer instanceof NGWVectorLayer){
                NGWVectorLayer ngwVectorLayer = (NGWVectorLayer) layer;
                if(!ngwVectorLayer.sendLocalChanges(syncResult)){
                    Log.d(Constants.FITAG, "send changes to server for " + layer.getName() +
                            " failed");
                }
                else{
                    if(FeatureChanges.getChangeCount(layer.getPath().getName() +
                            com.nextgis.maplib.util.Constants.CHANGES_NAME_POSTFIX) > 0){
                        hasChanges = true;
                    }
                }
            }
        }
        // update doc status (add change for next sync)
        if(!hasChanges){
            Cursor cur = query(new String[] { com.nextgis.maplib.util.Constants.FIELD_ID },
                    Constants.FIELD_DOCUMENTS_STATUS + " = " + Constants.DOCUMENT_STATUS_SEND,
                    null, null, null);
            List<Long> ids = new ArrayList<>();
            if(null != cur && cur.moveToFirst()) {
                do {
                    ids.add(cur.getLong(0));
                } while (cur.moveToNext());
                cur.close();
            }

            for(Long id : ids){
                ContentValues values = new ContentValues();
                values.put(Constants.FIELD_DOCUMENTS_STATUS, Constants.DOCUMENT_STATUS_OK);
                if(update(id, values, FIELD_ID + " = " + id, null) == 1)
                    addChange(id, CHANGE_OPERATION_CHANGED);
            }
        }

        return true;
    }

    @Override
    public boolean getChangesFromServer(String authority, SyncResult syncResult) throws SQLiteException {
        // 1. get docs
        if(super.getChangesFromServer(authority, syncResult)) {
            // 2. get other layers
            for(ILayer layer : mLayers){
                if(layer instanceof NGWVectorLayer){
                    NGWVectorLayer ngwVectorLayer = (NGWVectorLayer) layer;
                    if(!ngwVectorLayer.getChangesFromServer(authority, syncResult)){
                        Log.d(Constants.FITAG, "get changes from server for " + layer.getName() +
                                " failed");
                    }
                }
            }
        }
        return true;
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

    public boolean insert(DocumentEditFeature feature) {
        //create document
        ContentValues values = feature.getContentValues(false);
        long docId = insert(values);
        if(docId == com.nextgis.maplib.util.Constants.NOT_FOUND){
            return false;
        }

        addChange(docId, CHANGE_OPERATION_NEW);
        //update connected features doc_id
        feature.setId(docId);

        //create connected features
        for (ILayer layer : mLayers){
            if(layer instanceof VectorLayer) {
                VectorLayer vectorLayer = (VectorLayer) layer;
                String pathName = layer.getPath().getName();
                List<Feature> featureList = feature.getSubFeatures(pathName);
                if (null != featureList && featureList.size() > 0) {
                    Uri uri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/" + pathName);
                    for (Feature subFeature : featureList) {
                        if (vectorLayer.insert(uri, subFeature.getContentValues(false)) == null) {
                            Log.d(Constants.FITAG, "insert feature into " + pathName + " failed");
                        }
                    }
                }
            }
        }

        //add attachments
        MainApplication app = (MainApplication) mContext.getApplicationContext();
        File attachFolder = app.getDocFeatureFolder();
        Uri uri = Uri.parse("content://" + SettingsConstants.AUTHORITY + "/" + getPath().getName() +
                "/" + docId + "/" +  "attach");
        for(Map.Entry<String, AttachItem> entry : feature.getAttachments().entrySet()){
            AttachItem item = entry.getValue();

            values = new ContentValues();
            values.put(VectorLayer.ATTACH_DISPLAY_NAME, item.getDisplayName());
            values.put(VectorLayer.ATTACH_MIME_TYPE, item.getMimetype());
            values.put(VectorLayer.ATTACH_DESCRIPTION, item.getDescription());

            Uri result = insert(uri, values);
            if (result == null) {
                Log.d(Constants.FITAG, "insert attach failed");
            } else {
                List<String> pathSegments = result.getPathSegments();
                String featureId = pathSegments.get(pathSegments.size() - 3);
                String attachId = pathSegments.get(pathSegments.size() - 1);
                File to = new File(mPath, featureId + File.separator + attachId);
                File from = new File(attachFolder, item.getDisplayName());
                if(!FileUtil.copyRecursive(from, to)){
                    Log.d(Constants.FITAG, "create attach file failed");
                }
            }
        }

        return true;
    }
}
