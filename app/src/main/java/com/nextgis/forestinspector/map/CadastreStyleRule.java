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
import android.text.TextUtils;
import com.nextgis.forestinspector.R;
import com.nextgis.maplib.api.IStyleRule;
import com.nextgis.maplib.display.SimpleTextPolygonStyle;
import com.nextgis.maplib.display.Style;
import com.nextgis.maplib.map.NGWVectorLayer;


public abstract class CadastreStyleRule
        implements IStyleRule
{
    protected NGWVectorLayer mLayer;


    protected abstract String getCadastreName(long featureId);


    public CadastreStyleRule(NGWVectorLayer layer)
    {
        mLayer = layer;
    }


    public static Style getDefaultStyle(Context context)
    {
        SimpleTextPolygonStyle polygonStyle = new SimpleTextPolygonStyle();
        polygonStyle.setColor(context.getResources().getColor(R.color.primary_dark));
        polygonStyle.setWidth(3);
        polygonStyle.setFill(false);
        polygonStyle.setTextSize(25);
        return polygonStyle;
    }


    @Override
    public void setStyleParams(
            Style style,
            long featureId)
    {
        String text = getCadastreName(featureId);

        if (TextUtils.isEmpty(text)) {
            text = "";
        }

        SimpleTextPolygonStyle polygonStyle = (SimpleTextPolygonStyle) style;
        polygonStyle.setText(text);
    }
}
