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

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
public class HeaderRow
        extends LinearLayout
{
    final int PADDING = 5;
    LinearLayout firstLvlLinearLayout;
    LinearLayout secondLvlLinearLayout;

    Object       firstLvlLabel;
    List<String> secondLvlLabel;
    String       scrollViewTag;

    Table table;


    public HeaderRow(
            Table table,
            Object firstLvlLabel,
            List<String> secondLvlLabel,
            String scrollViewTag)
    {
        super(table.getContext());
        this.table = table;
        this.firstLvlLabel = firstLvlLabel;
        this.secondLvlLabel = secondLvlLabel;
        this.scrollViewTag = scrollViewTag;

        this.properties();
        this.init();
        this.initFirstLvlHeaders();
        this.initSecondLvlHeader();
        this.analizeFirstAndSecondHeaderWidth();
    }


    private void properties()
    {
        this.setOrientation(LinearLayout.VERTICAL);
    }


    /**
     * initialization
     */
    private void init()
    {
        this.firstLvlLinearLayout = new LinearLayout(this.getContext());
        this.firstLvlLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        this.secondLvlLinearLayout = new LinearLayout(this.getContext());
        this.secondLvlLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.secondLvlLinearLayout.setBackgroundColor(table.BODY_BACKROUND_COLOR);

        if (Table.IS_TWO_COLUMN_HEADER) {
            this.addView(firstLvlLinearLayout);
        }
        this.addView(secondLvlLinearLayout);
    }


    /**
     * top most header column
     */
    private void initFirstLvlHeaders()
    {
        LinearLayout.LayoutParams firstLvlTextViewParams =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        firstLvlTextViewParams.setMargins(1, 1, 1, 1);
        firstLvlTextViewParams.weight = 1;

        TextView firstLvlTextView = new TextView(this.getContext());
        firstLvlTextView.setText(firstLvlLabel.toString());
        firstLvlTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        firstLvlTextView.setBackgroundColor(table.HEADER_BACKROUND_COLOR);
        firstLvlTextView.setGravity(Gravity.CENTER);
        firstLvlTextView.setLayoutParams(firstLvlTextViewParams);

        this.firstLvlLinearLayout.addView(firstLvlTextView);
    }


    /**
     * second header column
     */
    private void initSecondLvlHeader()
    {
        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, table.COLUMN_WIDTH,
                getResources().getDisplayMetrics());

        for (Object aSecondLvlLabel : this.secondLvlLabel) {
            LayoutParams firstLvlTextViewParams =
                    new LayoutParams(width, LayoutParams.MATCH_PARENT);
            firstLvlTextViewParams.setMargins(1, 1, 1, 1);

            String labelString = aSecondLvlLabel.toString();

            // TODO: strings
            if (labelString.equalsIgnoreCase("Диаметр пня")) {
                TextView secondLvlTextView = new TextView(this.getContext());
                secondLvlTextView.setText(labelString);
                secondLvlTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
                secondLvlTextView.setBackgroundColor(table.HEADER_BACKROUND_COLOR);
                secondLvlTextView.setGravity(Gravity.CENTER);
                secondLvlTextView.setLayoutParams(firstLvlTextViewParams);
                this.secondLvlLinearLayout.addView(secondLvlTextView);

            } else {
                TextView secondLvlTextView = new TextView(this.getContext());
                secondLvlTextView.setText(labelString);
                secondLvlTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
                secondLvlTextView.setBackgroundColor(table.HEADER_BACKROUND_COLOR);
                secondLvlTextView.setGravity(Gravity.CENTER);
                secondLvlTextView.setLayoutParams(firstLvlTextViewParams);
                this.secondLvlLinearLayout.addView(secondLvlTextView);
            }
        }
    }


    /**
     * adjust the width of headers to match in screen size
     */
    private void analizeFirstAndSecondHeaderWidth()
    {
        int headerSecondLvlChildrenCount = secondLvlLinearLayout.getChildCount();
        /**
         * LEFT = 1;
         * RIGHT = 1;
         * get first lvl header width + (number of second lvl * (PADDING * (LEFT + RIGHT)) )
         */
        int headerFirstLvlWidth = ViewSizeUtils.getViewWidth(firstLvlLinearLayout.getChildAt(0)) + (
                headerSecondLvlChildrenCount * (PADDING * 2));

        int headerSecondLvlChildrenTotalWidth = 0;

        for (int x = 0; x < headerSecondLvlChildrenCount; ++x) {
            headerSecondLvlChildrenTotalWidth +=
                    ViewSizeUtils.getViewWidth(secondLvlLinearLayout.getChildAt(x));
        }

        int availableWidht = headerFirstLvlWidth - headerSecondLvlChildrenTotalWidth;
        int widhtForEachChild = (int) Math.ceil(availableWidht / headerSecondLvlChildrenCount);

        if (availableWidht <= 0) {
            // if no available width, do nothing
            return;
        }

        for (int x = 0; x < headerSecondLvlChildrenCount; ++x) {
            View view = secondLvlLinearLayout.getChildAt(x);
            LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.width = params.width <= 0
                           ? ViewSizeUtils.getViewWidth(view) + widhtForEachChild
                           : params.width + widhtForEachChild;
        }
    }
}
