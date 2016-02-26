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

package com.justsimpleinfo.Table;


import android.content.Context;
import android.widget.ScrollView;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
public class CustomScrollView
        extends ScrollView
{
    Table table;


    public CustomScrollView(
            Context context,
            Table table)
    {
        super(context);
        this.table = table;
    }


    @Override
    protected void onScrollChanged(
            int l,
            int t,
            int oldl,
            int oldt)
    {
        final String tag = this.getTag() + "";

        if (tag.equalsIgnoreCase(Table.RIGHT_BODY_SCROLLVIEW_TAG)) {
            table.leftTable.bodyScrollView.scrollTo(0, t);
        } else {
            table.rightTable.bodyScrollView.scrollTo(0, t);
        }
    }
}
