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

package com.nextgis.forestinspector.map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.IStyleRule;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.display.RuleFeatureRenderer;
import com.nextgis.maplib.display.Style;
import com.nextgis.maplib.map.LayerFactory;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;
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

import static com.nextgis.maplib.util.Constants.*;

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


    @Override
    public void sync(
            String authority,
            SyncResult syncResult)
    {
        if (0 != (mSyncType & com.nextgis.maplib.util.Constants.SYNC_NONE) || mFields.isEmpty()) {
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
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                Cursor cur = subLayer.query(
                        new String[] {com.nextgis.maplib.util.Constants.FIELD_ID},
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
                            subLayer.getPath().getName() + "/" + id);
                    subLayer.update(uri, values, null, null);
                }
            }
        }
    }

    @Override
    public boolean sendLocalChanges(SyncResult syncResult) throws SQLiteException {
        // send docs
        if (!super.sendLocalChanges(syncResult)) { return false; }

        // send relation records from other layers
        boolean hasChanges = false;
        for (ILayer layer : mLayers) {
            if (layer instanceof NGWVectorLayer) {
                NGWVectorLayer subLayer = (NGWVectorLayer) layer;
                if (subLayer.sendLocalChanges(syncResult)) {
                    if (subLayer.isChanges()) {
                        hasChanges = true;
                    }
                } else {
                    Log.d(
                            Constants.FITAG,
                            "send changes to server for " + layer.getName() + " failed");
                }
            }
        }

        // update doc status (add change for next sync)
        if (!hasChanges) {
            Cursor cur = query(
                    new String[] {com.nextgis.maplib.util.Constants.FIELD_ID},
                    Constants.FIELD_DOCUMENTS_STATUS + " = " + Constants.DOCUMENT_STATUS_FOR_SEND,
                    null, null, null);

            List<Long> ids = new ArrayList<>();
            if (null != cur) {
                if (cur.moveToFirst()) {
                    do {
                        ids.add(cur.getLong(0));
                    } while (cur.moveToNext());
                }
                cur.close();
            }

            for (Long id : ids) {
                if (setDocumentStatus(id, Constants.DOCUMENT_STATUS_OK)) {
                    addChange(id, CHANGE_OPERATION_CHANGED);
                }
            }
        }

        return true;
    }


    public boolean setDocumentStatus(long featureId, int status)
    {
        ContentValues values = new ContentValues();
        values.put(Constants.FIELD_DOCUMENTS_STATUS, status);
        return update(featureId, values, FIELD_ID + " = " + featureId, null) == 1;
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


    @Override
    public DocumentFeature getFeature(long featureId)
    {
        Feature feature = super.getFeature(featureId);
        if (null == feature) {
            return null;
        }

        DocumentFeature documentFeature = new DocumentFeature(feature);

        //get documents connected with this one
        Cursor cur = query(null, Constants.FIELD_DOC_ID + " = " + featureId, null, null, null);
        if (null != cur) {
            if (cur.moveToFirst()) {
                int idPos = cur.getColumnIndex(FIELD_ID);
                List<Long> ids = new ArrayList<>();
                do {
                    ids.add(cur.getLong(idPos));
                } while (cur.moveToNext());

                for (Long id : ids) {
                    DocumentFeature subDocFeature = getFeature(id);
                    if (null != subDocFeature) {
                        documentFeature.addSubFeature(Constants.KEY_LAYER_DOCUMENTS, subDocFeature);
                    }
                }
            }
            cur.close();
        }

        //get connected layers
        for (int i = 0; i < mLayers.size(); ++i) {
            ILayer layer = mLayers.get(i);
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                cur = subLayer.query(
                        null, Constants.FIELD_DOC_ID + " = " + featureId, null, null, null);
                if (null != cur) {
                    if (cur.moveToFirst()) {
                        do {
                            Feature subFeature = new Feature(NOT_FOUND, subLayer.getFields());
                            subFeature.fromCursor(cur);
                            documentFeature.addSubFeature(subLayer.getName(), subFeature);
                        } while (cur.moveToNext());
                    }
                    cur.close();
                }
            }
        }

        return documentFeature;
    }


    @Override
    public DocumentFeature getFeatureWithAttaches(long featureId)
    {
        Feature feature = super.getFeatureWithAttaches(featureId);

        if (null == feature) {
            return null;
        }

        DocumentFeature documentFeature = (DocumentFeature) feature;

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = subLayer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        subFeature.addAttachments(subLayer.getAttachMap("" + subFeature.getId()));
                    }
                }
            }
        }

        return documentFeature;
    }


    @Override
    public DocumentFeature getNewTempFeature()
    {
        Feature feature = super.getNewTempFeature();

        if (null == feature) {
            return null;
        }

        return new DocumentFeature(feature);
    }


    public Feature getNewTempSubFeature(
            DocumentFeature documentFeature,
            VectorLayer subDocumentLayer)
    {
        Feature subFeature = subDocumentLayer.getNewTempFeature();

        if (null == subFeature) {
            return null;
        }

        String subDocLayerName = subDocumentLayer.getName();

        documentFeature.addSubFeature(subDocLayerName, subFeature);

        return subFeature;
    }



    @Override
    public int updateFeatureWithFlags(Feature feature)
    {
        int res = super.updateFeatureWithFlags(feature);

        if (!(feature instanceof DocumentFeature)) {
            return res;
        }

        DocumentFeature documentFeature = (DocumentFeature) feature;

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = layer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        res += subLayer.updateFeatureWithFlags(subFeature);
                    }
                }
            }
        }

        return res;
    }


    @Override
    public int updateFeatureWithAttachesWithFlags(Feature feature)
    {
        int res = super.updateFeatureWithAttachesWithFlags(feature);

        if (!(feature instanceof DocumentFeature)) {
            return res;
        }

        DocumentFeature documentFeature = (DocumentFeature) feature;

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = subLayer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        long subFeatureIdL = subFeature.getId();

                        Map<String, AttachItem> subAttaches = subLayer.getAttachMap(
                                "" + subFeatureIdL);
                        if (null != subAttaches) {
                            for (AttachItem subAttachItem : subAttaches.values()) {
                                res += subLayer.updateAttachWithFlags(subFeature, subAttachItem);
                            }
                        }
                    }
                }
            }
        }

        return res;
    }


    @Override
    public void deleteAllTempFeatures()
    {
        super.deleteAllTempFeatures();

        // connected layers
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                subLayer.deleteAllTempFeatures();
            }
        }
    }


    @Override
    public void deleteAllTempAttaches()
    {
        super.deleteAllTempAttaches();

        // connected layers
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                subLayer.deleteAllTempAttaches();
            }
        }
    }


    @Override
    public long setFeatureWithAttachesTempFlag(
            Feature feature,
            boolean flag)
    {
        long res = super.setFeatureWithAttachesTempFlag(feature, flag);

        if (!(feature instanceof DocumentFeature)) {
            return res;
        }

        DocumentFeature documentFeature = (DocumentFeature) feature;

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = subLayer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        res += subLayer.setFeatureWithAttachesTempFlag(subFeature, flag);
                    }
                }
            }
        }

        return res;
    }


    @Override
    public long setFeatureWithAttachesNotSyncFlag(
            Feature feature,
            boolean flag)
    {
        long res = super.setFeatureWithAttachesNotSyncFlag(feature, flag);

        if (!(feature instanceof DocumentFeature)) {
            return res;
        }

        DocumentFeature documentFeature = (DocumentFeature) feature;

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = subLayer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        res += subLayer.setFeatureWithAttachesNotSyncFlag(subFeature, flag);
                    }
                }
            }
        }

        return res;
    }


    public boolean addChangeNew(DocumentEditFeature documentFeature)
    {
        long docId = documentFeature.getId();
        if (docId == com.nextgis.maplib.util.Constants.NOT_FOUND) {
            return false;
        }

        //update connected features doc_id
        documentFeature.setId(docId);

        addChange(docId, CHANGE_OPERATION_NEW);
        addAttachesChangeNew(this, documentFeature);

        // connected features
        for (ILayer layer : mLayers) {
            if (layer instanceof VectorLayer) {
                VectorLayer subLayer = (VectorLayer) layer;
                String pathName = subLayer.getPath().getName();
                List<Feature> featureList = documentFeature.getSubFeatures(pathName);

                if (null != featureList) {
                    for (Feature subFeature : featureList) {
                        subLayer.addChange(subFeature.getId(), CHANGE_OPERATION_NEW);
                        addAttachesChangeNew(subLayer, subFeature);
                    }
                }
            }
        }

        return true;
    }


    protected void addAttachesChangeNew(
            VectorLayer vectorLayer,
            Feature feature)
    {
        long featureIdL = feature.getId();

        Map<String, AttachItem> attaches = vectorLayer.getAttachMap("" + featureIdL);
        if (null != attaches) {
            for (AttachItem attachItem : attaches.values()) {
                long attachIdL = Long.parseLong(attachItem.getAttachId());
                vectorLayer.addChange(featureIdL, attachIdL, CHANGE_OPERATION_NEW);
            }
        }
    }


    @Override
    public Style getDefaultStyle()
            throws Exception
    {
        return DocumentStyleRule.getDefaultStyle();
    }


    protected IStyleRule getStyleRule()
    {
        return new DocumentStyleRule(mContext, this);
    }


    @Override
    protected void setDefaultRenderer()
    {
        try {
            Style style = getDefaultStyle();
            IStyleRule rule = getStyleRule();
            mRenderer = new RuleFeatureRenderer(this, rule, style);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            mRenderer = null;
        }
    }
}
