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

import android.database.Cursor;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.map.NGWVectorLayer;

import static com.nextgis.maplib.util.Constants.FIELD_ID;


public class LvStyleRule
        extends CadastreStyleRule
{
    public LvStyleRule(NGWVectorLayer layer)
    {
        super(layer);
    }


    protected String getCadastreName(long featureId)
    {
        String selection = FIELD_ID + " = " + featureId;
        String[] columns = new String[] {Constants.FIELD_CADASTRE_LV};

        Cursor cursor = mLayer.query(columns, selection, null, null, null);

        String text = null;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                text = cursor.getString(0);
            }
            cursor.close();
        }

        return text;
    }
}
