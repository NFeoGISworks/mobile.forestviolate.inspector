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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.dialog.TableNumberDialog;
import com.nextgis.forestinspector.util.Constants;

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
    protected final int CELL_TEXT_PADDING = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());

    LinearLayout generalVerticalLinearLayout;
    LinearLayout headerHorizontalLinearLayout;

    ScrollView   bodyScrollView;
    LinearLayout bodyHorizontalLinearLayout;

    Map<String, List<String>> headers;
    List<LinearLayout> bodyLinearLayoutTempMem = new ArrayList<>();
    Integer[] headerChildrenWidth;
    String    scrollViewTag;
    Table     table;
    HeaderRow headerRow;

    TableData mTableData;
    Context   mContext;


    public BodyTable(
            Context context,
            Table table,
            Map<String, List<String>> headers,
            String scrollViewTag)
    {
        super(context);

        mContext = context;

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
        this.generalVerticalLinearLayout = new LinearLayout(mContext);
        this.generalVerticalLinearLayout.setOrientation(LinearLayout.VERTICAL);


        this.headerHorizontalLinearLayout = new LinearLayout(mContext);
        this.headerHorizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        this.bodyScrollView = new CustomScrollView(mContext, table);
        this.bodyScrollView.setTag(scrollViewTag);

        this.bodyHorizontalLinearLayout = new LinearLayout(mContext);
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
            LinearLayout bodyLinear = new LinearLayout(mContext);
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


    public void loadData(TableData tableData)
    {
        this.removeView();
        mTableData = tableData;

        int firstLvlHeaderCounts = headers.size();
        List<List<String>> secondLvlHeader = new ArrayList<>();

        for (Entry<String, List<String>> header : headers.entrySet()) {
            secondLvlHeader.add(header.getValue());
        }

        int rowCount = tableData.size();

        for (int row = 0; row < rowCount; ++row) {
            TableRowData rowData = tableData.get(row);
            int columnCount = rowData.size();
            int childIndex = 0;

            for (int x = 0; x < firstLvlHeaderCounts; ++x) {
                LinearLayout rowLayout = new LinearLayout(mContext);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout bodyLayout = this.bodyLinearLayoutTempMem.get(x);
                bodyLayout.addView(rowLayout);

                int secondLvlHeaderCount = secondLvlHeader.get(x).size();

                for (int y = 0; y < secondLvlHeaderCount; ++y) {
                    int width = headerChildrenWidth[childIndex];

                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
                    params.setMargins(1, 1, 1, 1);
                    params.weight = 1;

                    LinearLayout cellLayout = new LinearLayout(mContext);
                    cellLayout.setOrientation(LinearLayout.HORIZONTAL);
                    cellLayout.setLayoutParams(params);
                    cellLayout.setBackgroundColor(table.CELL_BACKROUND_COLOR);

                    rowLayout.addView(cellLayout);

                    if (/*childIndex == 0 &&*/ scrollViewTag.equals(
                            Table.LEFT_BODY_SCROLLVIEW_TAG)) {

                        // child will be added in left

                        TextView textView = textView(rowData.get(0).toString(), row, 0);
                        cellLayout.addView(textView);

                    } else {
                        // child will be added in right

                        int column = y + 1;
                        int tag = row * columnCount + column;


                        TextView textView = textView(rowData.get(column).toString(), row, column);
                        textView.setId(tag);

                        View buttonMinus = buttonView(row, column, false, tag);
                        View buttonPlus = buttonView(row, column, true, tag);

                        cellLayout.addView(textView);
                        cellLayout.addView(buttonMinus);
                        cellLayout.addView(buttonPlus);
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
    private TextView textView(
            final String label,
            final int row,
            final int column)
    {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.setMargins(1, 1, 1, 1);
        params.weight = 1;

        TextView textView = new TextView(mContext);
        textView.setText(label);
        textView.setPadding(
                CELL_TEXT_PADDING, CELL_TEXT_PADDING, CELL_TEXT_PADDING, CELL_TEXT_PADDING);
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(params);

        textView.setOnClickListener(
                new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        AppCompatActivity activity = (AppCompatActivity) mContext;
                        final TextView text = (TextView) view;

                        final TableNumberDialog dialog = new TableNumberDialog();
                        dialog.setNumberText(text.getText().toString());
                        dialog.setOnPositiveClickedListener(
                                new TableNumberDialog.OnPositiveClickedListener()
                                {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onPositiveClicked()
                                    {
                                        Integer res = Integer.valueOf(dialog.getText());
                                        TableRowData rowData = mTableData.get(row);
                                        rowData.set(column, res);
                                        text.setText(res.toString());
                                    }
                                });
                        dialog.show(
                                activity.getSupportFragmentManager(),
                                Constants.FRAGMENT_TABLE_NUMBER_DIALOG);
                    }
                });

        return textView;
    }


    private View buttonView(
            final int row,
            final int column,
            final boolean plus,
            final int tag)
    {
        Context context = new ContextThemeWrapper(mContext, R.style.table_button);

        View view = View.inflate(context, R.layout.table_button, null);
        Button button = (Button) view.findViewById(R.id.table_button);
        button.setTag(tag);
        button.setText(plus ? "+" : "-");

        button.setOnClickListener(
                new OnClickListener()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View view)
                    {
                        TableRowData rowData = mTableData.get(row);
                        Integer res = (Integer) rowData.get(column);

                        if (!plus && res <= 0) {
                            return;
                        }

                        res = plus ? res + 1 : res - 1;
                        rowData.set(column, res);

                        Button button = (Button) view;
                        int tag = (Integer) button.getTag();
                        TextView textView = (TextView) findViewById(tag);
                        textView.setText(res.toString());
                    }
                });

        return view;
    }
}
