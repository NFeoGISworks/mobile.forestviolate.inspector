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
import android.graphics.Color;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
public class BodyTable
        extends HorizontalScrollView
{
    LinearLayout generalVerticalLinearLayout;
    LinearLayout headerHorizontalLinearLayout;

    ScrollView bodyScrollView;
    LinearLayout bodyHorizontalLinearLayout;

    Map<String, List<String>> headers;
    List<LinearLayout> bodyLinearLayoutTempMem = new ArrayList<>();
    Integer[] headerChildrenWidth;
    String    scrollViewTag;
    Table     table;
    HeaderRow headerRow;


    public BodyTable(
            Context context,
            Table table,
            Map<String, List<String>> headers,
            String scrollViewTag)
    {
        super(context);

        this.headers = headers;
        this.scrollViewTag = scrollViewTag;
        this.table = table;

        this.init();
        this.initHeaders();
        this.addBodyVerticalLinearLayout();
    }


    public void setHeaderChildrenWidth(Integer[] headerChildrenWidth)
    {
        this.headerChildrenWidth = headerChildrenWidth;
    }


    /**
     * initialization of layouts
     */
    private void init()
    {
        this.generalVerticalLinearLayout = new LinearLayout(this.getContext());
        this.generalVerticalLinearLayout.setOrientation(LinearLayout.VERTICAL);


        this.headerHorizontalLinearLayout = new LinearLayout(this.getContext());
        this.headerHorizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        this.bodyScrollView = new CustomScrollView(this.getContext(), table);
        this.bodyScrollView.setTag(scrollViewTag);

        this.bodyHorizontalLinearLayout = new LinearLayout(this.getContext());
        this.bodyHorizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // add child to each parent

        this.generalVerticalLinearLayout.addView(headerHorizontalLinearLayout);
        this.generalVerticalLinearLayout.addView(this.bodyScrollView);

        this.bodyScrollView.addView(this.bodyHorizontalLinearLayout);

        this.addView(this.generalVerticalLinearLayout);
    }


    private void initHeaders()
    {
        for (Entry<String, List<String>> header : headers.entrySet()) {
            String key = (String) header.getKey();
            List<String> values = header.getValue();

            headerRow = new HeaderRow(table, key, values, scrollViewTag);
            this.headerHorizontalLinearLayout.addView(headerRow);
        }
    }


    /**
     *
     */
    private void addBodyVerticalLinearLayout()
    {
        int firstLvlHeaderCount = headers.size();

        for (int x = 0; x < firstLvlHeaderCount; ++x) {
            LinearLayout bodyLinear = new LinearLayout(this.getContext());
            bodyLinear.setOrientation(LinearLayout.VERTICAL);

            bodyLinearLayoutTempMem.add(bodyLinear);

            this.bodyHorizontalLinearLayout.addView(bodyLinear);
        }
    }


    /**
     * remove all view in table body
     */
    private void removeView()
    {
        for (LinearLayout lin : bodyLinearLayoutTempMem) {
            lin.removeAllViews();
        }
    }


    final int bgColor = Color.GRAY;
    final int PADDING = 5;


    public void loadData(TableData tableData)
    {
        this.removeView();

        int firstLvlHeaderCounts = headers.size();
        List<List<String>> secondLvlHeader = new ArrayList<>();

        for (Entry<String, List<String>> header : headers.entrySet()) {
            secondLvlHeader.add(header.getValue());
        }

        for (int z = 0; z < tableData.size(); ++z) {
            TableRowData rowData = tableData.get(z);
            int childIndex = 0;

            for (int x = 0; x < firstLvlHeaderCounts; ++x) {
                LinearLayout cellLinear = new LinearLayout(this.getContext());
                cellLinear.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout bodyLinear = this.bodyLinearLayoutTempMem.get(x);
                bodyLinear.addView(cellLinear);

                int secondLvlHeaderCount = secondLvlHeader.get(x).size();

                for (int y = 0; y < secondLvlHeaderCount; ++y) {
                    int width = headerChildrenWidth[childIndex];

                    if (/*childIndex == 0 &&*/ scrollViewTag.equals(
                            Table.LEFT_BODY_SCROLLVIEW_TAG)) {
                        // child will be added in left

                        if (y == 0) {
                            cellLinear.addView(textView(rowData.get(0).toString()));
                        }

                    } else {
                        // child will be added in right
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
                        params.setMargins(1, 1, 1, 1);
                        params.weight = 1;

                        LinearLayout layout = new LinearLayout(this.getContext());
                        layout.setOrientation(LinearLayout.HORIZONTAL);
                        layout.setLayoutParams(params);

                        layout.addView(textView(rowData.get(y + 1).toString()));
                        layout.addView(buttonView(R.mipmap.ic_minus));
                        layout.addView(buttonView(R.mipmap.ic_plus));

                        cellLinear.addView(layout);
                    }

                    ++childIndex;
                }
            }
        }
    }


    /**
     * @param label
     *
     * @return
     */
    private TextView textView(String label)
    {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.setMargins(1, 1, 1, 1);
        params.weight = 1;

        TextView textView = new TextView(this.getContext());
        textView.setText(label);
        textView.setPadding(PADDING, PADDING, PADDING, PADDING);
        textView.setBackgroundColor(Table.BODY_BACKROUND_COLOR);
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(params);

        return textView;
    }


    private ImageButton buttonView(int resId)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50); // TODO: 50dp
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;

        Context context = getContext();

        ImageButton button = new ImageButton(context);
        button.setImageDrawable(getContext().getResources().getDrawable(resId));
        button.setLayoutParams(params);

        return button;
    }
}
