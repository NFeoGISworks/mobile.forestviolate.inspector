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
import android.support.v7.internal.widget.ThemeUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.DaveKoelle.AlphanumComparator;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
public class Table
        extends LinearLayout
{
    public static final String  PREVIOUS_ARROW            = "\u2190";
    public static final String  NEXT_ARROW                = "\u2192";
    public static       String  LEFT_BODY_SCROLLVIEW_TAG  = "LEFT_BODY_SCROLLVIEW_TAG";
    public static       String  RIGHT_BODY_SCROLLVIEW_TAG = "RIGHT_BODY_SCROLLVIEW_TAG";
    /**
     * @IS_TWO_COLUMN_HEADER = set this to true if you want two column header with span.
     */
    public static final boolean IS_TWO_COLUMN_HEADER      = false;

    public int BODY_BACKROUND_COLOR;
    public int HEADER_BACKROUND_COLOR;
    public int CELL_BACKROUND_COLOR;
    public static final int COLUMN_WIDTH = 120;

    int pagination = 20;
    int totalPage  = 0;
    int pageNumber = 1;

    Map<String, List<String>> leftHeaders  = new LinkedHashMap<>();
    Map<String, List<String>> rightHeaders = new LinkedHashMap<>();

    BodyTable leftTable;
    BodyTable rightTable;
    /**
     * @leftHeaderChildrenWidht = value will be set on adjust header width to match in screen width
     */
    Integer[] leftHeaderChildrenWidth;
    /**
     * rightHeaderChildrenWidht = value will be set on adjust header width to match in screen width
     */
    Integer[] rightHeaderChildrenWidht;

    Context        mContext;
    DocumentsLayer mDocsLayer;
    TableData      mTableData;


    public void init(Context context)
    {
        mContext = context;

        BODY_BACKROUND_COLOR =
                ThemeUtils.getThemeAttrColor(mContext, R.attr.tableBodyBackgroundColor);
        HEADER_BACKROUND_COLOR =
                ThemeUtils.getThemeAttrColor(mContext, R.attr.tableHeaderBackgroundColor);
        CELL_BACKROUND_COLOR =
                ThemeUtils.getThemeAttrColor(mContext, R.attr.tableCellBackgroundColor);

        MapBase map = MapBase.getInstance();
        mDocsLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                mDocsLayer = (DocumentsLayer) layer;
                break;
            }
        }


        this.createHeaders();
        this.properties();
        this.init();

        this.resizeFirstLvlHeaderHeight();
        this.resizeSecondLvlHeaderHeight();
        this.resizeHeaderSecondLvlWidhtToMatchInScreen();

        this.leftTable.setHeaderChildrenWidth(this.leftHeaderChildrenWidth);
        this.rightTable.setHeaderChildrenWidth(this.rightHeaderChildrenWidht);

        this.loadData();
    }


    public Table(Context context)
    {
        super(context);
        init(context);
    }


    public Table(
            Context context,
            AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }


    public List<String> getDictData(
            DocumentsLayer docsLayer,
            String layerKey,
            boolean numberSort)
    {
        NGWLookupTable table = (NGWLookupTable) docsLayer.getLayerByName(layerKey);

        if (null != table) {
            Map<String, String> data = table.getData();
            List<String> dataArray = new ArrayList<>();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                dataArray.add(entry.getKey());
            }

            if (numberSort) {
                Collections.sort(dataArray, new AlphanumComparator());
            } else {
                Collections.sort(dataArray);
            }

            return dataArray;
        }

        return null;
    }


    public void createHeaders()
    {
        // TODO: strings
        List<String> leftHeadersData = new ArrayList<>();
        leftHeadersData.add("Диаметр пня");
        leftHeaders.put("1", leftHeadersData);

        if (null != mDocsLayer) {
            List<String> species =
                    getDictData(mDocsLayer, Constants.KEY_LAYER_SPECIES_TYPES, false);
            rightHeaders.put("2", species);
        }
    }


    public TableData getTableData()
    {
        return mTableData;
    }


    public void loadData()
    {
        mTableData = loadTableData();
        leftTable.loadData(mTableData);
        rightTable.loadData(mTableData);

        resizeBodyChildrenHeight();
    }


    TableData loadTableData()
    {
        List<String> thickness = getDictData(mDocsLayer, Constants.KEY_LAYER_THICKNESS_TYPES, true);
        int columnCount = rightHeaders.get("2").size() + 1;
        int rowCount = thickness.size();
        TableData tableData = new TableData(rowCount);

        for (int i = 0; i < rowCount; ++i) {
            TableRowData rowData = new TableRowData(columnCount);
            rowData.add(thickness.get(i));

            for (int j = 1; j < columnCount; ++j) {
                rowData.add(0);
            }

            tableData.add(rowData);
        }

        return tableData;
    }


    private void properties()
    {
        setBackgroundColor(BODY_BACKROUND_COLOR);
    }


    private void init()
    {
        this.leftTable = new BodyTable(mContext, this, leftHeaders, LEFT_BODY_SCROLLVIEW_TAG);
        this.rightTable = new BodyTable(mContext, this, rightHeaders, RIGHT_BODY_SCROLLVIEW_TAG);

        this.addView(this.leftTable);
        this.addView(this.rightTable);
    }


    private void resizeFirstLvlHeaderHeight()
    {
        int rightHeaderLinearLayoutChildCount =
                rightTable.headerHorizontalLinearLayout.getChildCount();

        int rightHeaderFirstLvlHeighestHeight = 0;
        int rightHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; ++x) {
            HeaderRow row = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int height = ViewSizeUtils.getViewHeight(row.firstLvlLinearLayout);

            if (rightHeaderFirstLvlHeighestHeight <= height) {
                rightHeaderFirstLvlHeighestHeight = height;
                rightHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        int leftHeaderLinearLayoutChildCount =
                leftTable.headerHorizontalLinearLayout.getChildCount();

        int leftHeaderFirstLvlHighestHeight = 0;
        int leftHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            HeaderRow row = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int height = ViewSizeUtils.getViewHeight(row.firstLvlLinearLayout);

            if (leftHeaderFirstLvlHighestHeight <= height) {
                leftHeaderFirstLvlHighestHeight = height;
                leftHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        // (if isHighestHighInLeft == false) apply right header height in left and right except for the index in highest height
        boolean isHighestHighInLeft =
                leftHeaderFirstLvlHighestHeight >= rightHeaderFirstLvlHeighestHeight;

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; ++x) {
            LinearLayout firstLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).firstLvlLinearLayout;

            if (isHighestHighInLeft) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHighestHeight);
                params.weight = 1;
                firstLvlLinearLayout.setLayoutParams(params);

            } else {
                if (rightHeaderFirstLvlHeighestHeight != rightHeaderFirstLvlHighestHeightIndex) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, rightHeaderFirstLvlHeighestHeight);
                    params.weight = 1;
                    firstLvlLinearLayout.setLayoutParams(params);
                }
            }
        }

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout firstLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).firstLvlLinearLayout;

            if (isHighestHighInLeft) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHighestHeight);
                params.weight = 1;
                firstLvlLinearLayout.setLayoutParams(params);

            } else {
                if (leftHeaderFirstLvlHighestHeight != leftHeaderFirstLvlHighestHeightIndex) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, rightHeaderFirstLvlHeighestHeight);
                    params.weight = 1;
                    firstLvlLinearLayout.setLayoutParams(params);
                }
            }
        }
    }


    private void resizeSecondLvlHeaderHeight()
    {
        int rightHeaderLinearLayoutChildCount =
                rightTable.headerHorizontalLinearLayout.getChildCount();

        int rightHeaderFirstLvlHeighestHeight = 0;
        int rightHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; ++x) {
            HeaderRow row = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int height = ViewSizeUtils.getViewHeight(row.secondLvlLinearLayout);

            if (rightHeaderFirstLvlHeighestHeight <= height) {
                rightHeaderFirstLvlHeighestHeight = height;
                rightHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        int leftHeaderLinearLayoutChildCount =
                leftTable.headerHorizontalLinearLayout.getChildCount();

        int leftHeaderFirstLvlHeighestHeight = 0;
        int leftHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            HeaderRow row = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int height = ViewSizeUtils.getViewHeight(row.secondLvlLinearLayout);

            if (leftHeaderFirstLvlHeighestHeight <= height) {
                leftHeaderFirstLvlHeighestHeight = height;
                leftHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        // (if isHighestHighInLeft == false) apply right header height in left and right except for the index in highest height
        boolean isHighestHighInLeft =
                leftHeaderFirstLvlHeighestHeight >= rightHeaderFirstLvlHeighestHeight;

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;

            if (isHighestHighInLeft) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHeighestHeight);
                params.weight = 1;
                secondLvlLinearLayout.setLayoutParams(params);

            } else {
                if (rightHeaderFirstLvlHeighestHeight != rightHeaderFirstLvlHighestHeightIndex) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, rightHeaderFirstLvlHeighestHeight);
                    params.weight = 1;
                    secondLvlLinearLayout.setLayoutParams(params);
                }
            }
        }

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;

            if (isHighestHighInLeft) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHeighestHeight);
                params.weight = 1;
                secondLvlLinearLayout.setLayoutParams(params);

            } else {
                if (leftHeaderFirstLvlHeighestHeight != leftHeaderFirstLvlHighestHeightIndex) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, rightHeaderFirstLvlHeighestHeight);
                    params.weight = 1;
                    secondLvlLinearLayout.setLayoutParams(params);
                }
            }
        }
    }


    private void resizeHeaderSecondLvlWidhtToMatchInScreen()
    {
        int screenWidth = ScreenUtils.getScreenWidth(mContext);
        int leftHeaderChildrenTotalWidth = this.leftSecondLvlHeaderChildrenTotalWidth();
        int rightHeaderChildrenTotalWidth = this.rightHeaderChildrenTotalWidth();
        int leftHeaderSecondLvlChildrenCount = this.leftSecondLvlHeaderChildrenCount();
        int rightHeaderSecondLvlChildrenCount = this.rightSecondLvlHeaderChildrenCount();
        float availableWidth =
                screenWidth - (leftHeaderChildrenTotalWidth + rightHeaderChildrenTotalWidth);

        if (availableWidth <= 0) {
            // set the header width
            this.leftHeaderChildrenWidth = this.getLeftHeaderChildrenWidth();
            this.rightHeaderChildrenWidht = this.getRightHeaderChildrenWidth();

            return;
        }

        int widthForEachHeaderChild = (int) Math.ceil(
                availableWidth / (leftHeaderSecondLvlChildrenCount
                        + rightHeaderSecondLvlChildrenCount));

        this.addWidthForEachHeaderLeftAndRightChild(widthForEachHeaderChild);
        // set the header width
        this.leftHeaderChildrenWidth = this.getLeftHeaderChildrenWidth();
        this.rightHeaderChildrenWidht = this.getRightHeaderChildrenWidth();
    }


    /**
     * get children count in left header
     *
     * @return
     */
    private int leftSecondLvlHeaderChildrenCount()
    {
        int totalChildren = 0;
        int leftHeaderLinearLayoutChildCount =
                leftTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            totalChildren += secondLvlLinearLayout.getChildCount();
        }

        return totalChildren;
    }


    /**
     * get children count in right header
     *
     * @return
     */
    private int rightSecondLvlHeaderChildrenCount()
    {
        int totalChildren = 0;
        int leftHeaderLinearLayoutChildCount =
                rightTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            totalChildren += secondLvlLinearLayout.getChildCount();
        }

        return totalChildren;
    }


    /**
     * Compute total header width in left header
     *
     * @return
     */
    private int leftSecondLvlHeaderChildrenTotalWidth()
    {
        int totalWidth = 0;
        int leftHeaderLinearLayoutChildCount =
                leftTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            int leftColumnChildrenCount = secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < leftColumnChildrenCount; ++y) {
                View view = secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                int width = params.width <= 0 ? ViewSizeUtils.getViewWidth(view) : params.width;
                totalWidth += width;
            }
        }

        return totalWidth;
    }


    /**
     * Compute total right header children width
     *
     * @return
     */
    private int rightHeaderChildrenTotalWidth()
    {
        int totalWidth = 0;
        int leftHeaderLinearLayoutChildCount =
                rightTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; ++x) {
            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            int leftColumnChildrenCount = secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < leftColumnChildrenCount; ++y) {
                View view = secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                int width = params.width <= 0 ? ViewSizeUtils.getViewWidth(view) : params.width;
                totalWidth += width;
            }
        }

        return totalWidth;
    }


    /**
     * Add width in left and right children width if needed to match screen width.
     *
     * @param widthToBeAdded
     */
    private void addWidthForEachHeaderLeftAndRightChild(int widthToBeAdded)
    {
        int leftHeaderColumnCount = leftTable.headerHorizontalLinearLayout.getChildCount();
        int rightHeaderColumnCount = rightTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderColumnCount; ++x) {
            HeaderRow tableRow = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; ++y) {
                View view = tableRow.secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                params.width = params.width <= 0
                               ? ViewSizeUtils.getViewWidth(view) + widthToBeAdded
                               : params.width + widthToBeAdded;
            }
        }

        for (int x = 0; x < rightHeaderColumnCount; ++x) {
            HeaderRow tableRow = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; ++y) {
                View view = tableRow.secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                params.width = params.width <= 0
                               ? ViewSizeUtils.getViewWidth(view) + widthToBeAdded
                               : params.width + widthToBeAdded;
            }
        }
    }


    /**
     * Get each width of left header child
     *
     * @return
     */
    private Integer[] getLeftHeaderChildrenWidth()
    {
        List<Integer> headerChildrenWidth = new ArrayList<>();
        int leftHeaderColumnCount = leftTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < leftHeaderColumnCount; ++x) {
            HeaderRow tableRow = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; ++y) {
                View view = tableRow.secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
                int width = params.width <= 0 ? ViewSizeUtils.getViewWidth(view) : params.width;
                headerChildrenWidth.add(width);
            }
        }

        return headerChildrenWidth.toArray(new Integer[headerChildrenWidth.size()]);
    }


    /**
     * Get each width of right header child
     *
     * @return
     */
    private Integer[] getRightHeaderChildrenWidth()
    {
        List<Integer> headerChildrenWidth = new ArrayList<>();
        int rightHeaderColumnCount = rightTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < rightHeaderColumnCount; ++x) {
            HeaderRow tableRow = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; ++y) {
                View view = tableRow.secondLvlLinearLayout.getChildAt(y);
                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
                int width = params.width <= 0 ? ViewSizeUtils.getViewWidth(view) : params.width;
                headerChildrenWidth.add(width);
            }
        }

        return headerChildrenWidth.toArray(new Integer[headerChildrenWidth.size()]);
    }


    /**
     * Resize each body column to match each other
     */
    private void resizeBodyChildrenHeight()
    {
        int leftHeaderFirstLvlHighestHeight = 0;

        for (LinearLayout lin : leftTable.bodyLinearLayoutTempMem) {
            int childCount = lin.getChildCount();

            for (int x = 0; x < childCount; ++x) {
                int width = ViewSizeUtils.getViewHeight(lin.getChildAt(x));

                if (leftHeaderFirstLvlHighestHeight < width) {
                    leftHeaderFirstLvlHighestHeight = width;
                }
            }
        }

        int rightHeaderFirstLvlHighestHeight = 0;
        //int rightHeaderFirstLvlHighestHeightIndex = 0;
        for (LinearLayout lin : rightTable.bodyLinearLayoutTempMem) {
            int childCount = lin.getChildCount();

            for (int x = 0; x < childCount; ++x) {
                int width = ViewSizeUtils.getViewHeight(lin.getChildAt(x));
                if (rightHeaderFirstLvlHighestHeight < width) {
                    rightHeaderFirstLvlHighestHeight = width;
                    //rightHeaderFirstLvlHighestHeightIndex = x;
                }
            }
        }

        boolean isHighestHighInLeft =
                leftHeaderFirstLvlHighestHeight > rightHeaderFirstLvlHighestHeight;

        for (LinearLayout lin : leftTable.bodyLinearLayoutTempMem) {
            int childCount = lin.getChildCount();

            for (int x = 0; x < childCount; ++x) {
                LinearLayout.LayoutParams params =
                        (LayoutParams) lin.getChildAt(x).getLayoutParams();
                params.height = isHighestHighInLeft
                                ? leftHeaderFirstLvlHighestHeight
                                : rightHeaderFirstLvlHighestHeight;
            }
        }

        for (LinearLayout lin : rightTable.bodyLinearLayoutTempMem) {
            int childCount = lin.getChildCount();

            for (int x = 0; x < childCount; ++x) {
                LinearLayout.LayoutParams params =
                        (LayoutParams) lin.getChildAt(x).getLayoutParams();
                params.height = isHighestHighInLeft
                                ? leftHeaderFirstLvlHighestHeight
                                : rightHeaderFirstLvlHighestHeight;
            }
        }
    }
}
