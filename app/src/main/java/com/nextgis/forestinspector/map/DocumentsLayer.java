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
import android.database.sqlite.SQLiteException;

import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.NGWVectorLayer;

import java.io.File;

/**
 * documents layer class for specific needs (sync, relationship tables, etc.)
 */
public class DocumentsLayer extends NGWVectorLayer {
    public DocumentsLayer(Context context, File path) {
        super(context, path);

        mLayerType = Constants.LAYERTYPE_DOCS;
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
        // 3. get lookup tables

        return false;
        //return super.getChangesFromServer(authority, syncResult);
    }
}
