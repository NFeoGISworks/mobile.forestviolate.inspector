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

import android.content.Context;
import android.util.Log;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplibui.mapui.LayerFactoryUI;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.nextgis.maplib.util.Constants.*;

/**
 * Layer factory for forest inspector specific data
 */
public class FILayerFactory extends LayerFactoryUI{
    @Override
    public ILayer createLayer(Context context, File path) {
        ILayer layer = super.createLayer(context, path);
        if(null != layer)
            return layer;
        File config_file = new File(path, CONFIG);
        try {
            String sData = FileUtil.readFromFile(config_file);
            JSONObject rootObject = new JSONObject(sData);
            int nType = rootObject.getInt(JSON_TYPE_KEY);

            switch (nType) {
                case Constants.LAYERTYPE_DOCS:
                    layer = new DocumentsLayer(context, path, this);
                    break;
                case Constants.LAYERTYPE_LV:
                    layer = new LvLayer(context, path);
                    break;
                case Constants.LAYERTYPE_ULV:
                    layer = new UlvLayer(context, path);
                    break;
                case Constants.LAYERTYPE_KV:
                    layer = new KvLayer(context, path);
                    break;
                case Constants.LAYERTYPE_NOTES:
                    layer = new NotesLayerUI(context, path);
                    break;
            }
        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        return layer;
    }

    @Override
    public String getLayerTypeString(Context context, int type) {
        switch (type) {
            case Constants.LAYERTYPE_DOCS:
                return context.getString(R.string.documents_layer);
            case Constants.LAYERTYPE_LV:
                return context.getString(R.string.lv_layer);
            case Constants.LAYERTYPE_ULV:
                return context.getString(R.string.ulv_layer);
            case Constants.LAYERTYPE_KV:
                return context.getString(R.string.kv_layer);
            case Constants.LAYERTYPE_NOTES:
                return context.getString(R.string.notes_layer);
            default:
                return super.getLayerTypeString(context, type);
        }
    }
}
