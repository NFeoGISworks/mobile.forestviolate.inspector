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
import android.util.Log;
import com.nextgis.maplib.api.IStyleRule;
import com.nextgis.maplib.display.RuleFeatureRenderer;
import com.nextgis.maplib.display.Style;
import com.nextgis.maplib.map.NGWVectorLayer;

import java.io.File;

import static com.nextgis.maplib.util.Constants.TAG;


public abstract class CadastreLayer
        extends NGWVectorLayer
{
    protected abstract IStyleRule getStyleRule();


    public CadastreLayer(
            Context context,
            File path)
    {
        super(context, path);
    }


    @Override
    protected Style getDefaultStyle()
            throws Exception
    {
        return CadastreStyleRule.getDefaultStyle(mContext);
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
