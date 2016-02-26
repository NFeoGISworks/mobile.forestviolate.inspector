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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author http://justsimpleinfo.blogspot.ru/2015/04/android-scrolling-table-with-fixed.html
 */
public class Table
        extends LinearLayout
{
    public static final String  PREVIOUS_ARROW            = "\u2190";
    public static final String  NEXT_ARROW                = "\u2192";
    public static final int     HEADER_BACKROUND_COLOR    = Color.parseColor("#339999");
    public static final int     BODY_BACKROUND_COLOR      = Color.parseColor("#99cccc");
    public static       String  LEFT_BODY_SCROLLVIEW_TAG  = "LEFT_BODY_SCROLLVIEW_TAG";
    public static       String  RIGHT_BODY_SCROLLVIEW_TAG = "RIGHT_BODY_SCROLLVIEW_TAG";
    /**
     * @IS_TWO_COLUMN_HEADER = set this to true if you want two column header with span.
     */
    public static final boolean IS_TWO_COLUMN_HEADER      = true;

    LinkedHashMap<Object, Object[]> leftHeaders  = new LinkedHashMap<Object, Object[]>();
    LinkedHashMap<Object, Object[]> rightHeaders = new LinkedHashMap<Object, Object[]>();

    BodyTable rightTable;
    BodyTable leftTable;
    /**
     * @leftHeaderChildrenWidht = value will be set on adjust header width to match in screen width
     */
    Integer[] leftHeaderChildrenWidth;
    /**
     * rightHeaderChildrenWidht = value will be set on adjust header width to match in screen width
     */
    Integer[] rightHeaderChildrenWidht;


    LoadingDialog loadingDialog;


    public Table(Context context)
    {
        super(context);

        this.headers();
        this.properties();
        this.init();

        this.resizeFirstLvlHeaderHeight();
        this.resizeSecondLvlHeaderHeight();
        this.resizeHeaderSecondLvlWidhtToMatchInScreen();

        this.leftTable.setHeaderChildrenWidth(this.leftHeaderChildrenWidth);
        this.rightTable.setHeaderChildrenWidth(this.rightHeaderChildrenWidht);


        this.createTestData();
        this.loadData();


    }


    public final static String NAME                = "Name";
    public final static String GENDER              = "Gender";
    public final static String TICKET_SET_SEQUENCE = "Set Sequence";
    public final static String TICKET_NUMBER       = "Ticket Number";
    public final static String TICKET_VALID_UNTIL  = "    Valid Until    ";
    public final static String COUNTRY_FROM        = "  Country From  ";
    public final static String COUNTRY_TO          = "  Country To  ";


    public void headers()
    {
        leftHeaders.put("Passenger Info", new String[] {NAME, GENDER});
        rightHeaders.put(
                "Ticket Info",
                new String[] {TICKET_VALID_UNTIL, TICKET_NUMBER, TICKET_SET_SEQUENCE});
        rightHeaders.put("Country Info", new String[] {COUNTRY_FROM, COUNTRY_TO});

    }


    List<Passenger> testData     = new ArrayList<Table.Passenger>();
    List<Passenger> dataToBeLoad = new ArrayList<Table.Passenger>();
    int             pagination   = 20;
    int             totalPage    = 0;
    int             pageNumber   = 1;


    public void loadData()
    {


        // TODO Auto-generated method stub
        this.dataToBeLoad = this.getDataToBeLoad();

        leftTable.loadData(dataToBeLoad);
        rightTable.loadData(dataToBeLoad);


        this.resizeBodyChildrenHeight();

    }


    private void createTestData()
    {
        for (int x = 0; x < 102; x++) {
            Passenger passenger = new Passenger();
            passenger.name = "Passenger " + x;
            passenger.gender = x % 2 == 0 ? 'F' : 'M';
            passenger.ticketNum = x;
            passenger.setSequence = "Set " + x;
            passenger.validUntil = "May 01, 2015";
            passenger.countryFrom = "Country " + x;
            passenger.countryTo = x % 2 == 0 ? "Philippines" : "Country " + x;

            testData.add(passenger);
        }

        this.totalPage = this.totalPage(testData, pagination);
        /*this.dataToBeLoad = this.getDataToBeLoad();*/
    }


    /**
     * @return
     */
    private List<Passenger> getDataToBeLoad()
    {
        List<Passenger> passengers = new ArrayList<Table.Passenger>();
        int startingIndex = (pageNumber - 1) * pagination;

        int totalPassenger = testData.size();
        //dataToBeLoad.clear();

        for (int x = 0; x < pagination; x++) {

            int index = startingIndex + x;

            if (index < totalPassenger) {

                passengers.add(testData.get(index));

            } else {
                Log.e("no data", "no data");
            }

        }


        return passengers;
    }


    private int totalPage(
            List<Passenger> testData,
            int pagination)
    {

        int totalPage = testData.size() / pagination;
        totalPage = totalPage + (testData.size() % 20 == 0 ? 0 : 1);

        return totalPage;

    }


    private void properties()
    {
        this.setBackgroundColor(Color.WHITE);
        this.setOrientation(LinearLayout.HORIZONTAL);
    }


    private void init()
    {

        this.loadingDialog = new LoadingDialog(this.getContext());
        this.rightTable =
                new BodyTable(this.getContext(), this, rightHeaders, RIGHT_BODY_SCROLLVIEW_TAG);
        this.leftTable =
                new BodyTable(this.getContext(), this, leftHeaders, LEFT_BODY_SCROLLVIEW_TAG);


        this.addView(this.leftTable);
        this.addView(this.rightTable);
    }


    private void resizeFirstLvlHeaderHeight()
    {
        int rightHeaderLinearLayoutChildCount =
                rightTable.headerHorizontalLinearLayout.getChildCount();

        int rightHeaderFirstLvlHeighestHeight = 0;
        int rightHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; x++) {

            HeaderRow row = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);

            int height = ViewSizeUtils.getViewHeight(row.firtLvlLinearLayout);

            if (rightHeaderFirstLvlHeighestHeight <= height) {

                rightHeaderFirstLvlHeighestHeight = height;
                rightHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        int leftHeaderLinearLayoutChildCount =
                leftTable.headerHorizontalLinearLayout.getChildCount();

        int leftHeaderFirstLvlHeighestHeight = 0;
        int leftHeaderFirstLvlHighestHeightIndex = 0;

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

            HeaderRow row = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);

            int height = ViewSizeUtils.getViewHeight(row.firtLvlLinearLayout);

            if (leftHeaderFirstLvlHeighestHeight <= height) {

                leftHeaderFirstLvlHeighestHeight = height;
                leftHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        boolean isHighestHighInLeft = false;


        if (leftHeaderFirstLvlHeighestHeight < rightHeaderFirstLvlHeighestHeight) {
            // apply right header height in left and right except for the index in highest height

            isHighestHighInLeft = false;


        } else {

            isHighestHighInLeft = true;

        }

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; x++) {

            LinearLayout firstLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).firtLvlLinearLayout;


            if (isHighestHighInLeft) {

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHeighestHeight);
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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

            LinearLayout firstLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).firtLvlLinearLayout;


            if (isHighestHighInLeft) {

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, leftHeaderFirstLvlHeighestHeight);
                params.weight = 1;

                firstLvlLinearLayout.setLayoutParams(params);

            } else {

                if (leftHeaderFirstLvlHeighestHeight != leftHeaderFirstLvlHighestHeightIndex) {
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

        for (int x = 0; x < rightHeaderLinearLayoutChildCount; x++) {

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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

            HeaderRow row = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);

            int height = ViewSizeUtils.getViewHeight(row.secondLvlLinearLayout);

            if (leftHeaderFirstLvlHeighestHeight <= height) {

                leftHeaderFirstLvlHeighestHeight = height;
                leftHeaderFirstLvlHighestHeightIndex = x;
            }
        }

        boolean isHighestHighInLeft = false;


        if (leftHeaderFirstLvlHeighestHeight < rightHeaderFirstLvlHeighestHeight) {
            // apply right header height in left and right except for the index in highest height

            isHighestHighInLeft = false;


        } else {

            isHighestHighInLeft = true;

        }


        for (int x = 0; x < rightHeaderLinearLayoutChildCount; x++) {

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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

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
        int screenWidth = ScreenUtils.getScreenWidth(this.getContext());
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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            int leftColumnChildrenCount = secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < leftColumnChildrenCount; y++) {
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

        for (int x = 0; x < leftHeaderLinearLayoutChildCount; x++) {

            LinearLayout secondLvlLinearLayout =
                    ((HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(
                            x)).secondLvlLinearLayout;
            int leftColumnChildrenCount = secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < leftColumnChildrenCount; y++) {
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

        for (int x = 0; x < leftHeaderColumnCount; x++) {

            HeaderRow tableRow = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; y++) {

                View view = tableRow.secondLvlLinearLayout.getChildAt(y);

                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                int width = params.width <= 0
                            ? ViewSizeUtils.getViewWidth(view) + widthToBeAdded
                            : params.width + widthToBeAdded;
                params.width = width;
            }


        }

        for (int x = 0; x < rightHeaderColumnCount; x++) {

            HeaderRow tableRow = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; y++) {

                View view = tableRow.secondLvlLinearLayout.getChildAt(y);

                LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();

                int width = params.width <= 0
                            ? ViewSizeUtils.getViewWidth(view) + widthToBeAdded
                            : params.width + widthToBeAdded;
                params.width = width;
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

        List<Integer> headerChildrenWidth = new ArrayList<Integer>();

        int leftHeaderColumnCount = leftTable.headerHorizontalLinearLayout.getChildCount();


        for (int x = 0; x < leftHeaderColumnCount; x++) {

            HeaderRow tableRow = (HeaderRow) leftTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; y++) {

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

        List<Integer> headerChildrenWidth = new ArrayList<Integer>();

        int rightHeaderColumnCount = rightTable.headerHorizontalLinearLayout.getChildCount();

        for (int x = 0; x < rightHeaderColumnCount; x++) {

            HeaderRow tableRow = (HeaderRow) rightTable.headerHorizontalLinearLayout.getChildAt(x);
            int headerRowChildCount = tableRow.secondLvlLinearLayout.getChildCount();

            for (int y = 0; y < headerRowChildCount; y++) {

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

            for (int x = 0; x < childCount; x++) {
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

            for (int x = 0; x < childCount; x++) {
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

            for (int x = 0; x < childCount; x++) {
                LinearLayout.LayoutParams params =
                        (LayoutParams) lin.getChildAt(x).getLayoutParams();
                params.height = isHighestHighInLeft
                                ? leftHeaderFirstLvlHighestHeight
                                : rightHeaderFirstLvlHighestHeight;

            }


        }

        for (LinearLayout lin : rightTable.bodyLinearLayoutTempMem) {

            int childCount = lin.getChildCount();

            for (int x = 0; x < childCount; x++) {
                LinearLayout.LayoutParams params =
                        (LayoutParams) lin.getChildAt(x).getLayoutParams();
                params.height = isHighestHighInLeft
                                ? leftHeaderFirstLvlHighestHeight
                                : rightHeaderFirstLvlHighestHeight;

            }


        }

    }


    /**
     * @author lauro
     */
    class LoadingDialog
            extends Dialog
    {

        LoadingDialog(Context context)
        {
            super(context);
            this.setCancelable(false);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.init(context);
        }


        private void init(Context context)
        {
            TextView textView = new TextView(context);
            textView.setText("Please wait loading data..");

            this.setContentView(textView);

        }
    }


    class Passenger
    {
        String name;
        char   gender;
        int    ticketNum;
        String validUntil;
        String setSequence;
        String countryFrom;
        String countryTo;
    }
}
