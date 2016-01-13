/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * ****************************************************************************
 * Copyright (c) 2015. NextGIS, info@nextgis.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.forestinspector.overlay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryCollection;
import com.nextgis.maplib.datasource.GeoLineString;
import com.nextgis.maplib.datasource.GeoLinearRing;
import com.nextgis.maplib.datasource.GeoMultiLineString;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoMultiPolygon;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.GeoPolygon;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplibui.api.EditEventListener;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.api.Overlay;
import com.nextgis.maplibui.fragment.BottomToolbar;
import com.nextgis.maplibui.mapui.MapViewOverlays;
import com.nextgis.maplibui.service.WalkEditService;
import com.nextgis.maplibui.util.ConstantsUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class EditTerritoryOverlay extends Overlay implements MapViewEventListener {


    /**
     * overlay mode constants
     */
    public final static int MODE_NONE         = 0;
    public final static int MODE_HIGHLIGHT    = 1;
    public final static int MODE_EDIT         = 2;
    public final static int MODE_CHANGE       = 3;
    public final static int MODE_EDIT_BY_WALK = 4;

    /**
     * edit feature style
     */
    protected final static int VERTEX_RADIUS = 20;
    protected final static int EDGE_RADIUS   = 12;
    protected final static int LINE_WIDTH    = 4;

    protected Paint mPaint;
    protected int             mFillColor;
    protected int             mOutlineColor;
    protected int             mSelectColor;
    protected final Bitmap mAnchor;
    protected final float           mAnchorRectOffsetX, mAnchorRectOffsetY;
    protected final float mAnchorCenterX, mAnchorCenterY;


    protected int mMode;

    protected DrawItems mDrawItems;

    protected static final int mType = 7;

    protected DocumentEditFeature mEditFeature;
    protected long                mEditFeatureId;

    /**
     * Store keys
     */
    protected static final String BUNDLE_KEY_MODE = "mode";

    protected final float mTolerancePX;
    protected final float mAnchorTolerancePX;

    protected PointF mTempPointOffset;
    protected boolean mHasEdits;

    protected BottomToolbar mCurrentToolbar;

    protected float mCanvasCenterX, mCanvasCenterY;
    protected List<EditEventListener> mListeners;

    protected WalkEditReceiver mReceiver;


    public EditTerritoryOverlay(
            Context context,
            MapViewOverlays mapViewOverlays,
            long editFeatureId)
    {
        super(context, mapViewOverlays);

        mEditFeatureId = editFeatureId;

        mMode = MODE_NONE;

        mTolerancePX =
                context.getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mFillColor =
                Color.MAGENTA;//ThemeUtils.getThemeAttrColor(mContext, com.nextgis.maplibui.R.attr.colorAccent);
        mOutlineColor = Color.BLACK;
        mSelectColor = Color.RED;

        mAnchor = BitmapFactory.decodeResource(
                mContext.getResources(), com.nextgis.maplibui.R.drawable.ic_action_anchor);
        mAnchorRectOffsetX = -mAnchor.getWidth() * 0.05f;
        mAnchorRectOffsetY = -mAnchor.getHeight() * 0.05f;
        mAnchorCenterX = mAnchor.getWidth() * 0.75f;
        mAnchorCenterY = mAnchor.getHeight() * 0.75f;
        mAnchorTolerancePX = mAnchor.getScaledWidth(context.getResources().getDisplayMetrics());

        mDrawItems = new DrawItems();
        mListeners = new LinkedList<>();

        mMapViewOverlays.addListener(this);
        mHasEdits = false;

    }


    public boolean setMode(int mode)
    {
        Activity parent = (Activity) mContext;
        MainApplication app = (MainApplication) parent.getApplication();
        mEditFeature = app.getEditFeature(mEditFeatureId);

        fillDrawItems(mEditFeature.getGeometry());

        if (mode == MODE_EDIT) {

            for (EditEventListener listener : mListeners) {
                listener.onStartEditSession();
            }
            mDrawItems.setSelectedPointIndex(0);
            mDrawItems.setSelectedRing(0);
            mMapViewOverlays.postInvalidate();
        } else if (mode == MODE_NONE) {
            mDrawItems.setSelectedPointIndex(Constants.NOT_FOUND);
            mDrawItems.setSelectedRing(Constants.NOT_FOUND);
            mEditFeature = null;
            mMapViewOverlays.postInvalidate();
        } else if (mode == MODE_HIGHLIGHT) {
            mDrawItems.setSelectedPointIndex(Constants.NOT_FOUND);
            mDrawItems.setSelectedRing(Constants.NOT_FOUND);
        } else if (mode == MODE_EDIT_BY_WALK) {
            for (EditEventListener listener : mListeners) {
                listener.onStartEditSession();
            }
            mDrawItems.setSelectedPointIndex(0);
            mDrawItems.setSelectedRing(0);
        }

        mMode = mode;

        return true;
    }


    public int getMode()
    {
        return mMode;
    }


    @Override
    public void draw(
            Canvas canvas,
            MapDrawable mapDrawable)
    {

        if (null == mEditFeature || mMode == MODE_CHANGE) {
            return;
        }

        GeoGeometry geom = mEditFeature.getGeometry();
        if (null == geom) {
            return;
        }

        fillDrawItems(geom);

        switch (geom.getType()) {
            case GeoConstants.GTPoint:
            case GeoConstants.GTMultiPoint:
                mDrawItems.drawPoints(canvas);
                break;
            case GeoConstants.GTLineString:
            case GeoConstants.GTMultiLineString:
            case GeoConstants.GTPolygon:
            case GeoConstants.GTMultiPolygon:
                mDrawItems.drawLines(canvas);
                break;
            default:
                break;
        }

        drawCross(canvas);
    }

    protected float[] mapToScreen(GeoPoint[] geoPoints){
        return mMapViewOverlays.getMap().mapToScreen(geoPoints);
    }

    protected void fillDrawItems(GeoGeometry geom)
    {
        mDrawItems.clear();

        if(null == geom){
            Log.w(Constants.TAG, "the geometry is null in fillDrawItems method");
            return;
        }

        GeoPoint[] geoPoints;
        float[] points;
        GeoLineString lineString;
        switch (geom.getType()) {
            case GeoConstants.GTPoint:
                geoPoints = new GeoPoint[1];
                geoPoints[0] = (GeoPoint) geom;
                points = mapToScreen(geoPoints);
                mDrawItems.addItems(0, points, DrawItems.TYPE_VERTEX);
                break;
            case GeoConstants.GTMultiPoint:
                GeoMultiPoint geoMultiPoint = (GeoMultiPoint) geom;
                geoPoints = new GeoPoint[geoMultiPoint.size()];
                for (int i = geoMultiPoint.size() - 1; i > -1 ; i--) {
                    geoPoints[i] = geoMultiPoint.get(i);
                }
                points = mapToScreen(geoPoints);
                mDrawItems.addItems(0, points, DrawItems.TYPE_VERTEX);
                break;
            case GeoConstants.GTLineString:
                lineString = (GeoLineString) geom;
                fillDrawLine(0, lineString);
                break;
            case GeoConstants.GTMultiLineString:
                GeoMultiLineString multiLineString = (GeoMultiLineString)geom;
                for(int i =  multiLineString.size() - 1; i > -1; i--){
                    fillDrawLine(i, multiLineString.get(i));
                }
                break;
            case GeoConstants.GTPolygon:
                GeoPolygon polygon = (GeoPolygon) geom;
                fillDrawPolygon(polygon);
                break;
            case GeoConstants.GTMultiPolygon:
                GeoMultiPolygon multiPolygon = (GeoMultiPolygon)geom;
                for(int i = multiPolygon.size() - 1; i > -1; i--){
                    fillDrawPolygon(multiPolygon.get(i));
                }
                break;
            case GeoConstants.GTGeometryCollection:
                GeoGeometryCollection collection = (GeoGeometryCollection)geom;
                for(int i = collection.size() - 1; i > -1; i--){
                    GeoGeometry geoGeometry = collection.get(i);
                    fillDrawItems(geoGeometry);
                }
                break;
            default:
                break;
        }
    }


    protected void fillDrawPolygon(GeoPolygon polygon)
    {
        fillDrawRing(0, polygon.getOuterRing());
        for (int i = 0; i < polygon.getInnerRingCount(); i++) {
            fillDrawRing(i + 1, polygon.getInnerRing(i));
        }
    }


    protected void fillDrawLine(int ring, GeoLineString lineString)
    {
        GeoPoint[] geoPoints =
                lineString.getPoints().toArray(new GeoPoint[lineString.getPointCount()]);
        float[] points = mapToScreen(geoPoints);

        if (points.length < 2) {
            return;
        }

        mDrawItems.addItems(ring, points, DrawItems.TYPE_VERTEX);
        float[] edgePoints = new float[points.length - 2];
        for (int i = 0; i < points.length - 2; i++) {
            edgePoints[i] = (points[i] + points[i + 2]) * .5f;
        }
        mDrawItems.addItems(ring, edgePoints, DrawItems.TYPE_EDGE);
    }


    protected void fillDrawRing(int ring, GeoLinearRing geoLinearRing)
    {
        GeoPoint[] geoPoints =
                geoLinearRing.getPoints().toArray(new GeoPoint[geoLinearRing.getPointCount()]);
        float[] points = mapToScreen(geoPoints);
        mDrawItems.addItems(ring, points, DrawItems.TYPE_VERTEX);
        float[] edgePoints = new float[points.length];

        if (points.length == 0 || edgePoints.length < 2) {
            return;
        }

        for (int i = 0; i < points.length - 2; i++) {
            edgePoints[i] = (points[i] + points[i + 2]) * .5f;
        }
        edgePoints[edgePoints.length - 2] = (points[0] + points[points.length - 2]) * .5f;
        edgePoints[edgePoints.length - 1] = (points[1] + points[points.length - 1]) * .5f;
        mDrawItems.addItems(ring, edgePoints, DrawItems.TYPE_EDGE);
    }


    @Override
    public void drawOnPanning(
            Canvas canvas,
            PointF currentMouseOffset)
    {

        if (null == mEditFeature || null == mEditFeature.getGeometry()) {
            return;
        }

        DrawItems drawItems = mDrawItems;
        if (mMode != MODE_CHANGE) {
            drawItems = mDrawItems.pan(currentMouseOffset);
        }

        switch (mEditFeature.getGeometry().getType()) {
            case GeoConstants.GTPoint:
            case GeoConstants.GTMultiPoint:
                drawItems.drawPoints(canvas);
                break;
            case GeoConstants.GTLineString:
            case GeoConstants.GTMultiLineString:
            case GeoConstants.GTPolygon:
            case GeoConstants.GTMultiPolygon:
                drawItems.drawLines(canvas);
                break;
            default:
                break;
        }

        drawCross(canvas);
    }


    @Override
    public void drawOnZooming(
            Canvas canvas,
            PointF currentFocusLocation,
            float scale)
    {
        if (null == mEditFeature || null == mEditFeature.getGeometry()) {
            return;
        }

        DrawItems drawItems = mDrawItems.zoom(currentFocusLocation, scale);

        switch (mEditFeature.getGeometry().getType()) {
            case GeoConstants.GTPoint:
            case GeoConstants.GTMultiPoint:
                drawItems.drawPoints(canvas);
                break;
            case GeoConstants.GTLineString:
            case GeoConstants.GTMultiLineString:
            case GeoConstants.GTPolygon:
            case GeoConstants.GTMultiPolygon:
                drawItems.drawLines(canvas);
                break;
            default:
                break;
        }

        drawCross(canvas);
    }


    protected void drawCross(Canvas canvas)
    {
        if (mMode != MODE_EDIT) {
            return;
        }
        mCanvasCenterX = canvas.getWidth() / 2;
        mCanvasCenterY = canvas.getHeight() / 2;

        mPaint.setColor(mSelectColor);
        mPaint.setStrokeWidth(LINE_WIDTH / 2);
        canvas.drawLine(
                mCanvasCenterX - mTolerancePX, mCanvasCenterY, mCanvasCenterX + mTolerancePX,
                mCanvasCenterY, mPaint);
        canvas.drawLine(
                mCanvasCenterX, mCanvasCenterY - mTolerancePX, mCanvasCenterX,
                mCanvasCenterY + mTolerancePX, mPaint);
    }


    public void addListener(EditEventListener listener)
    {
        if (mListeners != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }


    public void removeListener(EditEventListener listener)
    {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    protected float[] getNewGeometry(
            int geometryType,
            GeoPoint screenCenter)
    {
        float[] geoPoints;
        float add = mTolerancePX * 2;
        switch (geometryType) {
            case GeoConstants.GTPoint:
            case GeoConstants.GTMultiPoint:
                geoPoints = new float[2];
                geoPoints[0] = (float) screenCenter.getX();
                geoPoints[1] = (float) screenCenter.getY();
                return geoPoints;
            case GeoConstants.GTLineString:
            case GeoConstants.GTMultiLineString:
                geoPoints = new float[4];
                geoPoints[0] = (float) screenCenter.getX() - add;
                geoPoints[1] = (float) screenCenter.getY() - add;
                geoPoints[2] = (float) screenCenter.getX() + add;
                geoPoints[3] = (float) screenCenter.getY() + add;
                return geoPoints;
            case GeoConstants.GTPolygon:
            case GeoConstants.GTMultiPolygon:
                geoPoints = new float[6];
                geoPoints[0] = (float) screenCenter.getX() - add;
                geoPoints[1] = (float) screenCenter.getY() - add;
                geoPoints[2] = (float) screenCenter.getX() - add;
                geoPoints[3] = (float) screenCenter.getY() + add;
                geoPoints[4] = (float) screenCenter.getX() + add;
                geoPoints[5] = (float) screenCenter.getY() + add;
                return geoPoints;
            default:
                break;
        }
        return null;
    }

    protected void setHasEdits(boolean hasEdits){
        mHasEdits = hasEdits;
    }

    protected void moveSelectedPoint(float x, float y){
        setHasEdits(true);

        mDrawItems.setSelectedPoint(x, y);
        mDrawItems.fillGeometry(0, mEditFeature.getGeometry(), mMapViewOverlays.getMap());

        updateMap();
    }

    protected void fillGeometry(){
        MapDrawable mapDrawable = mMapViewOverlays.getMap();
        mDrawItems.fillGeometry(0, mEditFeature.getGeometry(), mapDrawable);
    }

    public void setToolbar(final BottomToolbar toolbar)
    {
        if (null == toolbar) {
            return;
        }

        mCurrentToolbar = toolbar;
        mCurrentToolbar.setNavigationIcon(com.nextgis.maplibui.R.drawable.ic_action_apply_dark);
        mCurrentToolbar.setNavigationContentDescription(com.nextgis.maplibui.R.string.apply);

        switch (mMode) {
            case MODE_EDIT:
                toolbar.setNavigationOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                for (EditEventListener listener : mListeners) {
                                    listener.onFinishEditSession();
                                }

                                setMode(MODE_HIGHLIGHT);
                            }
                        });

                if (toolbar.getMenu() != null) {
                    toolbar.getMenu().clear();
                }

                toolbar.inflateMenu(com.nextgis.maplibui.R.menu.edit_multipolygon);

                toolbar.setOnMenuItemClickListener(
                        new BottomToolbar.OnMenuItemClickListener()
                        {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem)
                            {
                                if (null == mEditFeature) {
                                    return false;
                                }

                                if (menuItem.getItemId() ==
                                        com.nextgis.maplibui.R.id.menu_edit_move_point_to_center) {
                                    if (!isItemValid()) {
                                        return false;
                                    }

                                    moveSelectedPoint(mCanvasCenterX, mCanvasCenterY);

                                } else if (menuItem.getItemId() ==
                                        com.nextgis.maplibui.R.id.menu_edit_move_point_to_current_location) {
                                    if (!isItemValid()) {
                                        return false;
                                    }

                                    Activity parent = (Activity) mContext;
                                    GpsEventSource gpsEventSource =
                                            ((IGISApplication) parent.getApplication()).getGpsEventSource();
                                    Location location = gpsEventSource.getLastKnownLocation();
                                    if (null != location) {
                                        //change to screen coordinates
                                        GeoPoint pt = new GeoPoint(
                                                location.getLongitude(), location.getLatitude());
                                        pt.setCRS(GeoConstants.CRS_WGS84);
                                        pt.project(GeoConstants.CRS_WEB_MERCATOR);
                                        MapDrawable mapDrawable = mMapViewOverlays.getMap();
                                        GeoPoint screenPt = mapDrawable.mapToScreen(pt);
                                        moveSelectedPoint((float) screenPt.getX(), (float) screenPt.getY());
                                    }
                                } else if (menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_point) {
                                    return addGeometry(GeoConstants.GTPoint);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_multipoint) {
                                    return addGeometry(GeoConstants.GTMultiPoint);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_line) {
                                    return addGeometry(GeoConstants.GTLineString);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_multiline) {
                                    return addGeometry(GeoConstants.GTMultiLineString);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_polygon) {
                                    return addGeometry(GeoConstants.GTPolygon);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_add_new_multipolygon) {
                                    return addGeometry(GeoConstants.GTMultiPolygon);
                                } else if (
                                        menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_delete_multipoint ||
                                                menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_delete_line ||
                                                menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_delete_multiline ||
                                                menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_delete_polygon) {
                                    return deleteGeometry();
                                } else if (menuItem.getItemId() == com.nextgis.maplibui.R.id.menu_edit_delete_point) {
                                    return deletePoint();
                                }
                                return true;
                            }
                        });
                break;
            case MODE_EDIT_BY_WALK:
                toolbar.setNavigationOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                stopGeometryByWalk();
                                setMode(MODE_EDIT);
                                setToolbar(toolbar);

                            }
                        });

                startGeometryByWalk();
                break;
        }
    }


    protected boolean isItemValid() {
        return null != mEditFeature && null != mEditFeature.getGeometry();
    }


    protected boolean deleteGeometry() {
        if (!isItemValid())
            return false;

        setHasEdits(true);

        mDrawItems.deleteSelectedRing();
        mEditFeature.setGeometry(
                mDrawItems.fillGeometry(
                        0, mEditFeature.getGeometry(), mMapViewOverlays.getMap()));

        if (mDrawItems.mDrawItemsVertex.size() <= 0)
            mDrawItems.clear();

        updateMap();
        return true;
    }


    protected boolean deletePoint() {
        if (!isItemValid()) {
            return false;
        }

        mDrawItems.deleteSelectedPoint();

        GeoGeometry geom = mDrawItems.fillGeometry(
                0, mEditFeature.getGeometry(), mMapViewOverlays.getMap());
        setHasEdits(geom != null);

        if (null == geom) {
            mEditFeature.setGeometry(null);
        }

        updateMap();
        return true;
    }


    protected boolean addGeometry(int geometryType) {
        boolean isGeometryTypesIdentical = GeoConstants.GTMultiPolygon == geometryType;
        if (!isItemValid())
            if (!isGeometryTypesIdentical)
                return false;

        setHasEdits(true);

        MapDrawable mapDrawable = mMapViewOverlays.getMap();
        if (null == mapDrawable)
            return false;

        GeoPoint center = mapDrawable.getFullScreenBounds().getCenter();
        if (isGeometryTypesIdentical)
            createNewGeometry(geometryType, center);
        else
            addGeometryToExistent(geometryType, center);

        //set new coordinates to GeoPoint from screen coordinates
        mDrawItems.fillGeometry(0, mEditFeature.getGeometry(), mapDrawable);
        updateMap();
        return true;
    }


    protected void addGeometryToExistent(int geometryType, GeoPoint center) {
        //insert geometry in appropriate position
        switch (geometryType) {
            case GeoConstants.GTPoint:
                mDrawItems.addNewPoint((float) center.getX(), (float) center.getY());
                int lastIndex = mDrawItems.getLastPointIndex();
                mDrawItems.setSelectedPointIndex(lastIndex);
                break;
            case GeoConstants.GTLineString:
            case GeoConstants.GTPolygon:
                int lastRing = mDrawItems.mDrawItemsVertex.size();
                float[] geoPoints = getNewGeometry(geometryType, center);
                mDrawItems.addItems(lastRing, geoPoints, DrawItems.TYPE_VERTEX);
                mDrawItems.setSelectedRing(lastRing);
                mDrawItems.setSelectedPointIndex(0);
                break;
        }
    }


    protected void createNewGeometry(int geometryType, GeoPoint center) {
        switch (geometryType) {
            case GeoConstants.GTPoint:
                mEditFeature.setGeometry(new GeoPoint());
                break;
            case GeoConstants.GTMultiPoint:
                mEditFeature.setGeometry(new GeoMultiPoint());
                break;
            case GeoConstants.GTLineString:
                mEditFeature.setGeometry(new GeoLineString());
                break;
            case GeoConstants.GTMultiLineString:
                mEditFeature.setGeometry(new GeoMultiLineString());
                break;
            case GeoConstants.GTPolygon:
                mEditFeature.setGeometry(new GeoPolygon());
                break;
            case GeoConstants.GTMultiPolygon:
                mEditFeature.setGeometry(new GeoMultiPolygon());
                break;
        }

        mDrawItems.clear();
        float[] geoPoints = getNewGeometry(geometryType, center);
        mDrawItems.addItems(0, geoPoints, DrawItems.TYPE_VERTEX);
        mDrawItems.setSelectedRing(0);
        mDrawItems.setSelectedPointIndex(0);
    }


    protected void updateMap()
    {
        mMapViewOverlays.buffer();
        mMapViewOverlays.postInvalidate();
    }


    @Override
    public Bundle onSaveState()
    {
        Bundle bundle = super.onSaveState();
        bundle.putInt(BUNDLE_KEY_TYPE, mType);
        bundle.putInt(BUNDLE_KEY_MODE, mMode);

        return bundle;
    }


    @Override
    public void onRestoreState(Bundle bundle)
    {
        if (null != bundle) {
            int type = bundle.getInt(BUNDLE_KEY_TYPE);
            if (mType == type) {
                mMode = bundle.getInt(BUNDLE_KEY_MODE);
            }
        }
        super.onRestoreState(bundle);
    }


    @Override
    public void onLongPress(MotionEvent event) {

    }

    /**
     * Select point in current geometry or new geometry from current layer
     *
     * @param event
     *         Motion event
     */
    @Override
    public void onSingleTapUp(MotionEvent event)
    {
        selectGeometryInScreenCoordinates(event.getX(), event.getY());
    }


    protected void selectGeometryInScreenCoordinates(
            float x,
            float y)
    {
        double dMinX = x - mTolerancePX;
        double dMaxX = x + mTolerancePX;
        double dMinY = y - mTolerancePX;
        double dMaxY = y + mTolerancePX;
        GeoEnvelope screenEnv = new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY);

        if(null != mEditFeature && null != mEditFeature.getGeometry()) {
            //1. search current geometry point
            if (mDrawItems.intersects(screenEnv, mEditFeature.getGeometry(), mMapViewOverlays.getMap())) {
                if (mMode == MODE_HIGHLIGHT) { // highlight same geometry
                    mDrawItems.setSelectedPointIndex(Constants.NOT_FOUND);
                    mDrawItems.setSelectedRing(Constants.NOT_FOUND);
                }
                else {
                    mMapViewOverlays.invalidate();
                }
                return;
            }

            if (mHasEdits) // prevent select another geometry before saving current edited one.
                return;
        }
    }


    @Override
    public void panStart(MotionEvent event)
    {
        if (mMode == MODE_EDIT) {

            if (null != mEditFeature && null != mEditFeature.getGeometry()) {

                //check if we are near selected point
                double dMinX = event.getX() - mTolerancePX * 2 - mAnchorTolerancePX;
                double dMaxX = event.getX() + mTolerancePX;
                double dMinY = event.getY() - mTolerancePX * 2 - mAnchorTolerancePX;
                double dMaxY = event.getY() + mTolerancePX;
                GeoEnvelope screenEnv = new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY);

                if (mDrawItems.isTapNearSelectedPoint(screenEnv)) {
                    PointF tempPoint = mDrawItems.getSelectedPoint();
                    mTempPointOffset =
                            new PointF(tempPoint.x - event.getX(), tempPoint.y - event.getY());
                    mMapViewOverlays.setLockMap(true);
                    mMode = MODE_CHANGE;
                }
            }
        }
    }


    @Override
    public void panMoveTo(MotionEvent e)
    {
        if (mMode == MODE_CHANGE) {
            mDrawItems.setSelectedPoint(
                    e.getX() + mTempPointOffset.x, e.getY() + mTempPointOffset.y);
        }
    }


    @Override
    public void panStop()
    {
        if (mMode == MODE_CHANGE) {
            mMapViewOverlays.setLockMap(false);

            setHasEdits(true);
            mMode = MODE_EDIT;
            mDrawItems.fillGeometry(0, mEditFeature.getGeometry(), mMapViewOverlays.getMap());
            updateMap(); // redraw the map
        }
    }

    protected void startGeometryByWalk()
    {
        GeoMultiPolygon multiPolygon = (GeoMultiPolygon) mEditFeature.getGeometry();
        if(null == multiPolygon) {
            multiPolygon = new GeoMultiPolygon();
            mEditFeature.setGeometry(multiPolygon);
        }

        // register broadcast events
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WalkEditService.WALKEDIT_CHANGE);
        mReceiver = new WalkEditReceiver();
        mContext.registerReceiver(mReceiver, intentFilter);

        // start service
        Intent trackerService = new Intent(mContext, WalkEditService.class);
        trackerService.setAction(WalkEditService.ACTION_START);
        trackerService.putExtra(ConstantsUI.KEY_GEOMETRY_TYPE, GeoConstants.GTPolygon);
        DocumentsLayer layer = mEditFeature.getDocumentsLayer();
        if(null != layer)
            trackerService.putExtra(ConstantsUI.KEY_LAYER_ID, layer.getId());
        trackerService.putExtra(ConstantsUI.TARGET_CLASS, mContext.getClass().getName());
        mContext.startService(trackerService);
    }


    public void stopGeometryByWalk()
    {
        // stop service
        Intent trackerService = new Intent(mContext, WalkEditService.class);
        trackerService.setAction(WalkEditService.ACTION_STOP);
        mContext.stopService(trackerService);

        // unregister events
        mContext.unregisterReceiver(mReceiver);
    }

    public class WalkEditReceiver
            extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            GeoGeometry geometry = (GeoGeometry) intent.getSerializableExtra(ConstantsUI.KEY_GEOMETRY);
            GeoMultiPolygon multiPolygon = (GeoMultiPolygon) mEditFeature.getGeometry();
            multiPolygon.set(0, geometry);
            mEditFeature.setGeometry(multiPolygon);
            mMapViewOverlays.postInvalidate();
        }
    }

    protected int getMinPointCount()
    {
        return 3;
    }


    protected boolean isCurrentGeometryValid()
    {
        if (null == mEditFeature || null == mEditFeature.getGeometry()) {
            return false;
        }

        GeoMultiPolygon multiPolygon = (GeoMultiPolygon) mEditFeature.getGeometry();
        for (int i = 0; i < multiPolygon.size(); i++) {
            GeoPolygon subPolygon = multiPolygon.get(i);
            if (subPolygon.getOuterRing().getPointCount() > 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLayerAdded(int id) {

    }

    @Override
    public void onLayerDeleted(int id) {

    }

    @Override
    public void onLayerChanged(int id) {

    }

    @Override
    public void onExtentChanged(float zoom, GeoPoint center) {

    }

    @Override
    public void onLayersReordered() {

    }

    @Override
    public void onLayerDrawFinished(int id, float percent) {

    }

    @Override
    public void onLayerDrawStarted() {

    }


    protected class DrawItems
    {
        List<float[]> mDrawItemsVertex;
        List<float[]> mDrawItemsEdge;

        public static final int TYPE_VERTEX = 1;
        public static final int TYPE_EDGE   = 2;

        protected int mSelectedRing, mSelectedPointIndex;


        public DrawItems()
        {
            mDrawItemsVertex = new ArrayList<>();
            mDrawItemsEdge = new ArrayList<>();

            mSelectedRing = Constants.NOT_FOUND;
            mSelectedPointIndex = Constants.NOT_FOUND;
        }


        public void setSelectedRing(int selectedRing)
        {
            if (selectedRing >= mDrawItemsVertex.size()) {
                return;
            }
            mSelectedRing = selectedRing;
        }


        public void setSelectedPointIndex(int selectedPoint)
        {
            mSelectedPointIndex = selectedPoint;
        }


        public int getSelectedPointIndex()
        {
            return mSelectedPointIndex;
        }


        public void addItems(
                int ring,
                float[] points,
                int type)
        {
            if (type == TYPE_VERTEX) {
                mDrawItemsVertex.add(ring, points);
            } else if (type == TYPE_EDGE) {
                mDrawItemsEdge.add(ring, points);
            }
        }


        public void clear()
        {
            mDrawItemsVertex.clear();
            mDrawItemsEdge.clear();
        }


        public DrawItems zoom(
                PointF location,
                float scale)
        {
            DrawItems drawItems = new DrawItems();
            drawItems.setSelectedRing(mSelectedRing);
            drawItems.setSelectedPointIndex(mSelectedPointIndex);

            int count = 0;
            for (float[] items : mDrawItemsVertex) {
                float[] newItems = new float[items.length];
                for (int i = 0; i < items.length - 1; i += 2) {
                    newItems[i] = items[i] - (1 - scale) * (items[i] + location.x);
                    newItems[i + 1] = items[i + 1] - (1 - scale) * (items[i + 1] + location.y);
                }
                drawItems.addItems(count++, newItems, TYPE_VERTEX);
            }

            count = 0;
            for (float[] items : mDrawItemsEdge) {
                float[] newItems = new float[items.length];
                for (int i = 0; i < items.length - 1; i += 2) {
                    newItems[i] = items[i] - (1 - scale) * (items[i] + location.x);
                    newItems[i + 1] = items[i + 1] - (1 - scale) * (items[i + 1] + location.y);
                }
                drawItems.addItems(count++, newItems, TYPE_EDGE);
            }

            return drawItems;
        }


        public DrawItems pan(PointF offset)
        {
            DrawItems drawItems = new DrawItems();
            drawItems.setSelectedRing(mSelectedRing);
            drawItems.setSelectedPointIndex(mSelectedPointIndex);

            int count = 0;
            for (float[] items : mDrawItemsVertex) {
                float[] newItems = new float[items.length];
                for (int i = 0; i < items.length - 1; i += 2) {
                    newItems[i] = items[i] - offset.x;
                    newItems[i + 1] = items[i + 1] - offset.y;
                }
                drawItems.addItems(count++, newItems, TYPE_VERTEX);
            }

            count = 0;
            for (float[] items : mDrawItemsEdge) {
                float[] newItems = new float[items.length];
                for (int i = 0; i < items.length - 1; i += 2) {
                    newItems[i] = items[i] - offset.x;
                    newItems[i + 1] = items[i + 1] - offset.y;
                }
                drawItems.addItems(count++, newItems, TYPE_EDGE);
            }

            return drawItems;
        }


        public void drawPoints(Canvas canvas)
        {
            for (float[] items : mDrawItemsVertex) {

                mPaint.setColor(mOutlineColor);
                mPaint.setStrokeWidth(VERTEX_RADIUS + 2);
                canvas.drawPoints(items, mPaint);

                mPaint.setColor(mFillColor);
                mPaint.setStrokeWidth(VERTEX_RADIUS);
                canvas.drawPoints(items, mPaint);
            }

            //draw selected point
            if (mSelectedPointIndex != Constants.NOT_FOUND) {

                float[] items = getSelectedRing();
                if (null != items) {
                    mPaint.setColor(mSelectColor);
                    mPaint.setStrokeWidth(VERTEX_RADIUS);

                    canvas.drawPoint(
                            items[mSelectedPointIndex], items[mSelectedPointIndex + 1], mPaint);

                    float anchorX = items[mSelectedPointIndex] + mAnchorRectOffsetX;
                    float anchorY = items[mSelectedPointIndex + 1] + mAnchorRectOffsetY;
                    canvas.drawBitmap(mAnchor, anchorX, anchorY, null);
                }
            }
        }


        public void drawLines(Canvas canvas)
        {
            float[] itemsVertex;
            for (int j = 0; j < mDrawItemsVertex.size(); j++) {
                itemsVertex = mDrawItemsVertex.get(j);

                if (mSelectedRing == j)
                    mPaint.setColor(mSelectColor);
                else
                    mPaint.setColor(mFillColor);

                mPaint.setStrokeWidth(LINE_WIDTH);

                for (int i = 0; i < itemsVertex.length - 3; i += 2) {
                    canvas.drawLine(itemsVertex[i], itemsVertex[i + 1], itemsVertex[i + 2], itemsVertex[i + 3], mPaint);
                }

                if (mEditFeature.getGeometry().getType() == GeoConstants.GTPolygon ||
                        mEditFeature.getGeometry().getType() == GeoConstants.GTMultiPolygon) {
                    if (itemsVertex.length >= 2) {
                        canvas.drawLine(
                                itemsVertex[0], itemsVertex[1], itemsVertex[itemsVertex.length - 2],
                                itemsVertex[itemsVertex.length - 1], mPaint);
                    }
                }

                if (mMode == MODE_EDIT || mMode == MODE_CHANGE) {
                    mPaint.setColor(mOutlineColor);
                    mPaint.setStrokeWidth(VERTEX_RADIUS + 2);
                    canvas.drawPoints(itemsVertex, mPaint);

                    mPaint.setColor(mFillColor);
                    mPaint.setStrokeWidth(VERTEX_RADIUS);
                    canvas.drawPoints(itemsVertex, mPaint);
                }
            }

            if (mMode == MODE_EDIT) {
                for (float[] items : mDrawItemsEdge) {

                    mPaint.setColor(mOutlineColor);
                    mPaint.setStrokeWidth(EDGE_RADIUS + 2);
                    canvas.drawPoints(items, mPaint);

                    mPaint.setColor(mFillColor);
                    mPaint.setStrokeWidth(EDGE_RADIUS);
                    canvas.drawPoints(items, mPaint);
                }
            }

            //draw selected point
            if (mSelectedPointIndex != Constants.NOT_FOUND) {
                float[] items = getSelectedRing();
                if (null != items && mSelectedPointIndex + 1 < items.length) {
                    mPaint.setColor(mSelectColor);
                    mPaint.setStrokeWidth(VERTEX_RADIUS);

                    canvas.drawPoint(
                            items[mSelectedPointIndex], items[mSelectedPointIndex + 1], mPaint);

                    float anchorX = items[mSelectedPointIndex] + mAnchorRectOffsetX;
                    float anchorY = items[mSelectedPointIndex + 1] + mAnchorRectOffsetY;
                    canvas.drawBitmap(mAnchor, anchorX, anchorY, null);
                }
            }
        }


        public boolean intersects(GeoEnvelope screenEnv,
                                  GeoGeometry geometry,
                                  MapDrawable mapDrawable)
        {
            int point;
            for (int ring = 0; ring < mDrawItemsVertex.size(); ring++) {
                point = 0;
                float[] items = mDrawItemsVertex.get(ring);
                for (int i = 0; i < items.length - 1; i += 2) {
                    if (screenEnv.contains(new GeoPoint(items[i], items[i + 1]))) {
                        mSelectedRing = ring;
                        mSelectedPointIndex = point;
                        return true;
                    }
                    point += 2;

                }
            }

            if (mMode == MODE_EDIT) {
                for (int ring = 0; ring < mDrawItemsEdge.size(); ring++) {
                    point = 0;
                    float[] items = mDrawItemsEdge.get(ring);
                    for (int i = 0; i < items.length - 1; i += 2) {
                        if (screenEnv.contains(new GeoPoint(items[i], items[i + 1]))) {
                            mSelectedPointIndex = i + 2;
                            mSelectedRing = ring;
                            insertNewPoint(mSelectedPointIndex, items[i], items[i + 1]);

                            //fill geometry
                            fillGeometry(0, geometry, mapDrawable);

                            return true;
                        }
                        point++;
                    }
                }
            }
            return false;
        }


        public PointF getSelectedPoint()
        {
            float[] points = getSelectedRing();
            if (null == points || mSelectedPointIndex < 0 || points.length <= mSelectedPointIndex) {
                return null;
            }
            return new PointF(points[mSelectedPointIndex], points[mSelectedPointIndex + 1]);
        }


        public void addNewPoint(
                float x,
                float y)
        {
            float[] points = getSelectedRing();
            if (null == points) {
                return;
            }
            float[] newPoints = new float[points.length + 2];
            System.arraycopy(points, 0, newPoints, 0, points.length);
            newPoints[points.length] = x;
            newPoints[points.length + 1] = y;

            mDrawItemsVertex.set(mSelectedRing, newPoints);
        }


        public void insertNewPoint(
                int insertPosition,
                float x,
                float y)
        {
            float[] points = getSelectedRing();
            if (null == points) {
                return;
            }
            float[] newPoints = new float[points.length + 2];
            int count = 0;
            for (int i = 0; i < newPoints.length - 1; i += 2) {
                if (i == insertPosition) {
                    newPoints[i] = x;
                    newPoints[i + 1] = y;
                } else {
                    newPoints[i] = points[count++];
                    newPoints[i + 1] = points[count++];
                }
            }

            mDrawItemsVertex.set(mSelectedRing, newPoints);
        }


        protected float[] getSelectedRing()
        {
            if (mDrawItemsVertex.isEmpty() || mSelectedRing < 0 ||
                    mSelectedRing >= mDrawItemsVertex.size()) {
                return null;
            }
            return mDrawItemsVertex.get(mSelectedRing);
        }


        public void deleteSelectedPoint()
        {
            float[] points = getSelectedRing();
            if (null == points || mSelectedPointIndex < 0) {
                return;
            }
            if (points.length <= getMinPointCount() * 2) {
                mDrawItemsVertex.remove(mSelectedRing);
                mSelectedRing--;
                mSelectedPointIndex = Constants.NOT_FOUND;
                return;
            }
            float[] newPoints = new float[points.length - 2];
            int counter = 0;
            for (int i = 0; i < points.length; i++) {
                if (i == mSelectedPointIndex || i == mSelectedPointIndex + 1) {
                    continue;
                }
                newPoints[counter++] = points[i];
            }

            if (mSelectedPointIndex >= newPoints.length) {
                mSelectedPointIndex = 0;
            }

            mDrawItemsVertex.set(mSelectedRing, newPoints);
        }


        public void deleteSelectedRing() {
            mDrawItemsVertex.remove(mSelectedRing);
            mSelectedRing = mDrawItemsVertex.size() > 0 ? 0 : Constants.NOT_FOUND;
            mSelectedPointIndex = Constants.NOT_FOUND;
        }


        public void setSelectedPoint(
                float x,
                float y)
        {
            float[] points = getSelectedRing();
            if (null != points && mSelectedPointIndex > Constants.NOT_FOUND) {
                points[mSelectedPointIndex] = x;
                points[mSelectedPointIndex + 1] = y;
            }
        }


        public GeoGeometry fillGeometry(
                int ring,
                GeoGeometry geometry,
                MapDrawable mapDrawable)
        {
            if (null == geometry || null == mapDrawable || ring < 0 ||
                    ring >= mDrawItemsVertex.size()) {
                return null;
            }

            if (mDrawItemsVertex.isEmpty()) {
                return null;
            }

            GeoPoint[] points;
            switch (geometry.getType()) {
                case GeoConstants.GTPoint:
                    points = mapDrawable.screenToMap(mDrawItemsVertex.get(ring));
                    GeoPoint point = (GeoPoint) geometry;
                    point.setCoordinates(points[0].getX(), points[0].getY());
                    break;
                case GeoConstants.GTMultiPoint:
                    points = mapDrawable.screenToMap(mDrawItemsVertex.get(ring));
                    GeoMultiPoint multiPoint = (GeoMultiPoint) geometry;
                    multiPoint.clear();
                    for (GeoPoint geoPoint : points) {
                        multiPoint.add(geoPoint);
                    }
                    break;
                case GeoConstants.GTLineString:
                    points = mapDrawable.screenToMap(mDrawItemsVertex.get(ring));
                    GeoLineString lineString = (GeoLineString) geometry;
                    lineString.clear();
                    for (GeoPoint geoPoint : points) {
                        if (null == geoPoint) {
                            continue;
                        }
                        lineString.add(geoPoint);
                    }
                    break;
                case GeoConstants.GTMultiLineString:
                    GeoMultiLineString multiLineString = (GeoMultiLineString) geometry;
                    GeoLineString line;
                    multiLineString.clear();
                    for (int i = 0; i < mDrawItemsVertex.size(); i++) {
                        points = mapDrawable.screenToMap(mDrawItemsVertex.get(i));
                        line = new GeoLineString();
                        for (GeoPoint geoPoint : points) {
                            if (null == geoPoint)
                                continue;

                            line.add(geoPoint);
                        }

                        multiLineString.add(line);
                    }
                    break;
                case GeoConstants.GTPolygon:
                    points = mapDrawable.screenToMap(mDrawItemsVertex.get(ring));
                    GeoPolygon polygon = (GeoPolygon) geometry;
                    polygon.clear();
                    for (GeoPoint geoPoint : points) {
                        if (null == geoPoint) {
                            continue;
                        }
                        polygon.add(geoPoint);
                    }

                    fillGeometry(ring, polygon.getOuterRing(), mapDrawable);

                    for (int currentRing = 0;
                         currentRing < polygon.getInnerRingCount();
                         currentRing++) {
                        fillGeometry(
                                ring + currentRing + 1, polygon.getInnerRing(currentRing),
                                mapDrawable);
                    }
                    break;
                case GeoConstants.GTMultiPolygon:
                    GeoMultiPolygon multiPolygon = (GeoMultiPolygon) geometry;
                    GeoPolygon geoPolygon;
                    multiPolygon.clear();
                    for (int i = 0; i < mDrawItemsVertex.size(); i++) {
                        points = mapDrawable.screenToMap(mDrawItemsVertex.get(i));
                        geoPolygon = new GeoPolygon();
                        for (GeoPoint geoPoint : points) {
                            if (null == geoPoint)
                                continue;

                            geoPolygon.add(geoPoint);
                        }

                        multiPolygon.add(geoPolygon);
                    }
                    break;
                default:
                    break;
            }

            return geometry;
        }


        public int getLastPointIndex()
        {
            float[] points = getSelectedRing();
            if (null == points) {
                return Constants.NOT_FOUND;
            }
            if (points.length < 2) {
                return Constants.NOT_FOUND;
            }
            return points.length - 2;
        }


        public boolean isTapNearSelectedPoint(GeoEnvelope screenEnv)
        {
            float[] points = getSelectedRing();
            if (null != points && mSelectedPointIndex > Constants.NOT_FOUND &&
                    points.length > mSelectedPointIndex + 1) {
                if (screenEnv.contains(
                        new GeoPoint(
                                points[mSelectedPointIndex], points[mSelectedPointIndex + 1]))) {
                    return true;
                }
            }
            return false;
        }
    }
}
