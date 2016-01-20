/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2016. NextGIS, info@nextgis.com
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.IStyleRule;
import com.nextgis.maplib.display.SimpleTiledPolygonStyle;
import com.nextgis.maplib.display.Style;

import static com.nextgis.maplib.util.Constants.FIELD_ID;


public class DocumentStyleRule
        implements IStyleRule
{
    protected DocumentsLayer mDocumentsLayer;
    protected long           mUserId;


    public DocumentStyleRule(
            Context context,
            DocumentsLayer documentsLayer)
    {
        mDocumentsLayer = documentsLayer;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUserId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
    }


    public static Style getDefaultStyle()
    {
        SimpleTiledPolygonStyle polygonStyle = new SimpleTiledPolygonStyle();
        polygonStyle.setColor(Color.GRAY);
        polygonStyle.setWidth(3);
        polygonStyle.setFill(true);
        return polygonStyle;
    }


    @Override
    public void setStyleParams(
            Style style,
            long featureId)
    {
        String selection = FIELD_ID + " = " + featureId;
        String[] columns = new String[] {Constants.FIELD_DOCUMENTS_USER_ID};

        Cursor cursor = mDocumentsLayer.query(columns, selection, null, null, null);

        long userId = -2;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0);
            }
            cursor.close();
        }

        SimpleTiledPolygonStyle polygonStyle = (SimpleTiledPolygonStyle) style;

        if (mUserId == userId) {
            polygonStyle.setColor(Color.CYAN);
            polygonStyle.setWidth(6);
        }
    }
}
