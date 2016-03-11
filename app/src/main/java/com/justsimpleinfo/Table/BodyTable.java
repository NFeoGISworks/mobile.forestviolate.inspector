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
import android.view.View;
import android.view.ViewGroup;
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

            for (int x = 0; x < firstLvlHeaderCounts; ++x) {
                LinearLayout rowLayout = new LinearLayout(mContext);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout bodyLayout = this.bodyLinearLayoutTempMem.get(x);
                bodyLayout.addView(rowLayout);

                int secondLvlHeaderCount = secondLvlHeader.get(x).size();

                for (int y = 0; y < secondLvlHeaderCount; ++y) {
                    if (/*childIndex == 0 &&*/ scrollViewTag.equals(
                            Table.LEFT_BODY_SCROLLVIEW_TAG)) {

                        // child will be added in left
                        cellDiameterView(rowData.get(0).toString(), rowLayout);

                    } else {
                        // child will be added in right
                        int column = y + 1;
                        int cellId = row * columnCount + column;
                        cellNumberView(
                                rowData.get(column).toString(), cellId, row, column, rowLayout);
                    }
                }
            }
        }
    }


    OnClickListener mTextOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(final View view)
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
                            ViewGroup parent = (ViewGroup) view.getParent();
                            CellData cellData = (CellData) parent.getTag();
                            int row = cellData.mRow;
                            int column = cellData.mColumn;

                            Integer res = Integer.valueOf(dialog.getText());
                            TableRowData rowData = mTableData.get(row);
                            rowData.set(column, res);
                            text.setText(res.toString());
                        }
                    });
            dialog.show(
                    activity.getSupportFragmentManager(), Constants.FRAGMENT_TABLE_NUMBER_DIALOG);
        }
    };


    OnClickListener mButtonOnClickListener = new OnClickListener()
    {
        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View view)
        {
            Button button = (Button) view;
            ViewGroup parent = (ViewGroup) button.getParent();

            CellData cellData = (CellData) parent.getTag();
            int row = cellData.mRow;
            int column = cellData.mColumn;
            boolean plus = button.getText().toString().equals("+");

            TableRowData rowData = mTableData.get(row);
            Integer res = (Integer) rowData.get(column);

            if (!plus && res <= 0) {
                return;
            }

            res = plus ? res + 1 : res - 1;
            rowData.set(column, res);

            TextView text = (TextView) parent.findViewById(cellData.mCellId);
            text.setText(res.toString());
        }
    };


    private void cellDiameterView(
            String label,
            ViewGroup parent)
    {
        View view = View.inflate(mContext, R.layout.table_cell_diameter, parent);
        TextView text = (TextView) view.findViewById(R.id.diameter);

        text.setText(label);
    }


    private void cellNumberView(
            String label,
            int cellId,
            int row,
            int column,
            ViewGroup parent)
    {
        View view = View.inflate(mContext, R.layout.table_cell_number, parent);
        TextView text = (TextView) view.findViewById(R.id.number);
        Button buttonMinus = (Button) view.findViewById(R.id.button_minus);
        Button buttonPlus = (Button) view.findViewById(R.id.button_plus);

        ViewGroup p = (ViewGroup) text.getParent();
        p.setTag(new CellData(cellId, row, column));

        text.setId(cellId);
        buttonMinus.setId(cellId * 10000);
        buttonPlus.setId(cellId * 20000);

        text.setText(label);

        text.setOnClickListener(mTextOnClickListener);
        buttonMinus.setOnClickListener(mButtonOnClickListener);
        buttonPlus.setOnClickListener(mButtonOnClickListener);
    }


    protected class CellData
    {
        int mCellId;
        int mRow;
        int mColumn;


        public CellData(
                int cellId,
                int row,
                int column)
        {
            mCellId = cellId;
            mRow = row;
            mColumn = column;
        }
    }
}
