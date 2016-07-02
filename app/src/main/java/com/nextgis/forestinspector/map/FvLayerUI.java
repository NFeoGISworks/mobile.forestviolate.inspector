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
import android.content.SyncResult;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapContentProviderHelper;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class FvLayerUI
        extends NGWVectorLayerUI
{
    public FvLayerUI(
            Context context,
            File path)
    {
        super(context, path);

        mLayerType = Constants.LAYERTYPE_FV;
    }


    @Override
    public void sync(
            String authority,
            SyncResult syncResult)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MONTH, Constants.MONTH_TO_LOAD_FV_DATA);

        // delete old messages
        try {
            MapContentProviderHelper map = (MapContentProviderHelper) MapBase.getInstance();
            if (null == map) {
                throw new IllegalArgumentException(
                        "The map should extends MapContentProviderHelper or inherited");
            }
            SQLiteDatabase db = map.getDatabase(false);
            String table = getPath().getName();

            db.delete(table, Constants.FIELD_FV_DATE + " < " + calendar.getTimeInMillis(), null);
            db.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        // load only last messages
        String datePrefix = "={\"gt\":\"";
        String dateSuffix = "T00:00:00Z\"}";
        int dateStart = mServerWhere.lastIndexOf(datePrefix);
        int dateEnd = mServerWhere.lastIndexOf(dateSuffix);

        if (-1 != dateStart && -1 != dateEnd) {
            dateStart += datePrefix.length();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currDate = sdf.format(calendar.getTime());

            mServerWhere = mServerWhere.substring(0, dateStart) + currDate + mServerWhere.substring(
                    dateEnd, mServerWhere.length());
        }

        super.sync(authority, syncResult);
    }
}
