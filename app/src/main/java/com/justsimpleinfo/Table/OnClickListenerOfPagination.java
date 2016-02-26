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

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
class OnClickListenerOfPagination
        implements OnClickListener
{
    Table table;


    OnClickListenerOfPagination(Table table)
    {
        this.table = table;
    }


    @Override
    public void onClick(View view)
    {
        // show loading dialog
        table.loadingDialog.show();

        int tag = (Integer) view.getTag();

        switch (tag) {
            case HeaderRow.NEXT_PAGINATION_TAG:
                table.pageNumber++;
                table.loadData();

                table.leftTable.headerRow.previousTextView.setTextColor(Color.BLACK);
                table.leftTable.headerRow.previousTextView.setEnabled(true);

                if (table.pageNumber == table.totalPage) {
                    table.leftTable.headerRow.nextTextView.setEnabled(false);
                    table.leftTable.headerRow.nextTextView.setTextColor(
                            Table.HEADER_BACKROUND_COLOR);
                }

                break;

            case HeaderRow.PREVIUOS_PAGINATION_TAG:
                table.pageNumber--;
                table.loadData();

                // set text color black
                table.leftTable.headerRow.nextTextView.setTextColor(Color.BLACK);
                table.leftTable.headerRow.nextTextView.setEnabled(true);

                if (table.pageNumber == 1) {
                    table.leftTable.headerRow.previousTextView.setEnabled(false);
                    table.leftTable.headerRow.previousTextView.setTextColor(
                            Table.HEADER_BACKROUND_COLOR);

                }

                break;

            default:
                break;
        }

        table.loadingDialog.dismiss();
    }
}
