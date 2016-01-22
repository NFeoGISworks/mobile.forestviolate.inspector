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

package com.nextgis.forestinspector.dialog;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.nextgis.forestinspector.activity.DocumentViewActivity;
import com.nextgis.forestinspector.activity.NoteCreatorActivity;
import com.nextgis.forestinspector.adapter.ClickedItemsListAdapter;
import com.nextgis.forestinspector.adapter.DocumentsListItem;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplibui.activity.ModifyAttributesActivity;

import java.util.List;

import static com.nextgis.maplib.util.Constants.FIELD_ID;
import static com.nextgis.maplibui.util.ConstantsUI.*;


public class ClickedItemsInfoGetter
        implements ClickedItemsListAdapter.OnListItemClickListener
{
    protected AppCompatActivity mActivity;
    protected GeoEnvelope       mEnvelope;

    protected MapBase mMap;


    public ClickedItemsInfoGetter(
            AppCompatActivity activity,
            GeoEnvelope envelope)
    {
        mActivity = activity;
        mEnvelope = envelope;
        mMap = MapBase.getInstance();
    }


    public void showInfo()
    {
        int itemCount = 0;
        int singleItemType = -1;

        List<Long> docsIds = getIntersectedIds(Constants.KEY_LAYER_DOCUMENTS, mEnvelope);
        if (null != docsIds && docsIds.size() > 0) {
            itemCount += docsIds.size();
            singleItemType = Constants.DOC_TYPE_DOCUMENT_ANY;
        }

        List<Long> notesIds = getIntersectedIds(Constants.KEY_LAYER_NOTES, mEnvelope);
        if (null != notesIds && notesIds.size() > 0) {
            itemCount += notesIds.size();
            singleItemType = Constants.DOC_TYPE_NOTE;
        }

        List<Long> targetsIds = getIntersectedIds(Constants.KEY_LAYER_FV, mEnvelope);
        if (null != targetsIds && targetsIds.size() > 0) {
            itemCount += targetsIds.size();
            singleItemType = Constants.DOC_TYPE_TARGET;
        }

        if (itemCount == 1) {
            switch (singleItemType) {
                case Constants.DOC_TYPE_DOCUMENT_ANY:
                    showItemInfo(docsIds.get(0), singleItemType);
                    break;
                case Constants.DOC_TYPE_NOTE:
                    showItemInfo(notesIds.get(0), singleItemType);
                    break;
                case Constants.DOC_TYPE_TARGET:
                    showItemInfo(targetsIds.get(0), singleItemType);
                    break;
            }
        } else {
            showList(docsIds, notesIds, targetsIds);
        }
    }


    protected List<Long> getIntersectedIds(
            String layerPathName,
            GeoEnvelope envelope)
    {
        ILayer layer = mMap.getLayerByPathName(layerPathName);
        if (null == layer) {
            return null;
        }

        if (!layer.isValid()) {
            return null;
        }

        ILayerView layerView = (ILayerView) layer;
        if (!layerView.isVisible()) {
            return null;
        }

        VectorLayer vectorLayer = (VectorLayer) layer;
        return vectorLayer.query(envelope);
    }


    protected void showItemInfo(
            long featureId,
            int itemType)
    {
        Intent intent = null;

        switch (itemType) {
            case Constants.DOC_TYPE_DOCUMENT_ANY:
            case Constants.DOC_TYPE_INDICTMENT:
            case Constants.DOC_TYPE_SHEET:
            case Constants.DOC_TYPE_FIELD_WORKS:
                intent = new Intent(mActivity, DocumentViewActivity.class);
                intent.putExtra(FIELD_ID, featureId);
                intent.putExtra(Constants.DOCUMENT_VIEWER, true);
                break;
            case Constants.DOC_TYPE_NOTE:
                intent = new Intent(mActivity, NoteCreatorActivity.class);
                intent.putExtra(FIELD_ID, featureId);
                break;
            case Constants.DOC_TYPE_TARGET:
                VectorLayer targetsLayer =
                        (VectorLayer) mMap.getLayerByPathName(Constants.KEY_LAYER_FV);

                GeoGeometry geometry = targetsLayer.getGeometryForId(featureId);

                intent = new Intent(mActivity, ModifyAttributesActivity.class);
                intent.putExtra(KEY_LAYER_ID, targetsLayer.getId());
                intent.putExtra(KEY_FEATURE_ID, featureId);
                intent.putExtra(KEY_VIEW_ONLY, true);
                intent.putExtra(KEY_GEOMETRY_CHANGED, false);
                if (null != geometry) {
                    intent.putExtra(KEY_GEOMETRY, geometry);
                }
                break;
        }

        if (null != intent) {
            mActivity.startActivity(intent);
        }
    }


    protected void showList(
            List<Long> docsIds,
            List<Long> notesIds,
            List<Long> targetsIds)
    {
        FragmentManager fm = mActivity.getSupportFragmentManager();

        ClickedItemsListDialog dialog = (ClickedItemsListDialog) fm.findFragmentByTag(
                Constants.FRAGMENT_CLICKED_LIST_DIALOG);

        if (dialog == null) {
            dialog = new ClickedItemsListDialog();
            dialog.setDocsIds(docsIds);
            dialog.setNotesIds(notesIds);
            dialog.setTargetsIds(targetsIds);
            dialog.setAdapderOnListItemClickListener(this);
            dialog.show(fm, Constants.FRAGMENT_CLICKED_LIST_DIALOG);
        }
    }


    @Override
    public void onListItemClick(DocumentsListItem item)
    {
        showItemInfo(item.mId, item.mType);
    }
}
