/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * *****************************************************************************
 * Copyright (c) 2015-2015. NextGIS, info@nextgis.com
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

package com.nextgis.forestinspector.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The main document list
 */
public class DocumentsListAdapter extends BaseAdapter
        implements MapEventListener {

    protected int mDocsId, mNotesId;
    protected List<Document> mDocuments;
    protected MapBase mMap;
    protected Context mContext;

    public DocumentsListAdapter(Context context) {
        mContext = context;
        mDocuments = new ArrayList<>();
        mMap = MapBase.getInstance();
        for(int i = 0; i < mMap.getLayerCount(); i++){
            ILayer layer = mMap.getLayer(i);
            if(layer.getName().equals(Constants.KEY_LAYER_DOCUMENTS)){
                mDocsId = layer.getId();
            }
            else if(layer.getName().equals(Constants.KEY_LAYER_NOTES)){
                mNotesId = layer.getId();
            }
        }
        loadData();
    }

    protected void loadData(){

        mDocuments.clear();

        ILayer docs = mMap.getLayerById(mDocsId);
        if(docs != null){
            VectorLayer vlayer = (VectorLayer)docs;
            Cursor cur = vlayer.query(...);
        }

        ILayer notes = mMap.getLayerById(mNotesId);
        if(notes != null){
            VectorLayer vlayer = (VectorLayer)notes;
            Cursor cur = vlayer.query(...);
        }

        Collections.sort(mDocuments, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Document) (o1)).compareTo((Document) (o2));
            }
        });
    }

    @Override
    public int getCount() {
        return mDocuments.size();
    }

    @Override
    public Object getItem(int position) {
        return mDocuments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.row_document, null);
        }

        Document item = (Document) getItem(position);

        ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
        switch (item.mType){
            case Constants.TYPE_DOCUMENT:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.mipmap.ic_indicment));
                break;
            case Constants.TYPE_NOTE:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.mipmap.ic_bookmark));
                break;
        }

        ImageView ivStateIcon = (ImageView) v.findViewById(R.id.ivStateIcon);

        TextView tvStep = (TextView) v.findViewById(R.id.tvName);
        tvStep.setText(item.mName);

        TextView tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        tvDesc.setText(item.mDesc);

        TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
        tvDesc.setText(item.mStepDescription); //TODO: format date

        return v;
    }

    @Override
    public void onLayerAdded(int id) {

    }

    @Override
    public void onLayerDeleted(int id) {

    }

    @Override
    public void onLayerChanged(int id) {
        //work only on docs and notes layers
        if(id == mDocsId || id == mNotesId){
            loadData();
            //reload
            notifyDataSetChanged();
        }
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

    protected class Document implements Comparable{
        String mName;
        String mDesc;
        Date mDate;
        int mType;
        int mStatus;

        @Override
        public int compareTo(@NonNull Object another) {
            return mDate > ((Document)(another)).mDate;
        }
    }
}
