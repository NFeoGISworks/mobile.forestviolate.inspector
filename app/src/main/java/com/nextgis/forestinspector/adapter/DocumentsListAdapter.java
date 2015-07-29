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
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.DocumentViewActivity;
import com.nextgis.forestinspector.activity.NotificationActivity;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The main document list
 */
public class DocumentsListAdapter extends BaseAdapter
        implements MapEventListener, AdapterView.OnItemClickListener {

    protected int mDocsId, mNotesId;
    protected List<Document> mDocuments;
    protected MapBase mMap;
    protected Context mActivity;

    public DocumentsListAdapter(FragmentActivity activity) {
        mActivity = activity;
        mDocuments = new ArrayList<>();
        mMap = MapBase.getInstance();
        mDocsId = mNotesId = -10;
        for(int i = 0; i < mMap.getLayerCount(); i++){
            ILayer layer = mMap.getLayer(i);
            if(layer instanceof DocumentsLayer){
                mDocsId = layer.getId();
            }
            else if(layer.getName().equals(mActivity.getString(R.string.notes))){
                mNotesId = layer.getId();
            }

            if(mDocsId > -10 && mNotesId > -10)
                break;
        }
        loadData();
    }

    protected void loadData(){

        mDocuments.clear();

        ILayer docs = mMap.getLayerById(mDocsId);
        if(docs != null){
            VectorLayer vlayer = (VectorLayer)docs;
            //order by datetime(datetimeColumn) ASC LIMIT 100
            Cursor cursor = vlayer.query(new String[] { com.nextgis.maplib.util.Constants.FIELD_ID,
                            Constants.FIELD_DOCUMENTS_TYPE, Constants.FIELD_DOCUMENTS_PARENT_ID,
                            Constants.FIELD_DOCUMENTS_DATE, Constants.FIELD_DOCUMENTS_NUMBER,
                            Constants.FIELD_DOCUMENTS_STATUS, Constants.FIELD_DOCUMENTS_VIOLATE },
                    null, null, Constants.FIELD_DOCUMENTS_DATE + " ASC", " " + Constants.MAX_DOCUMENTS);
            if (null != cursor) {
                int nIdPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int nTypePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_TYPE);
                int nDocIdPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_PARENT_ID);
                int nDatePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_DATE);
                int nNumberPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_NUMBER);
                int nStatusPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_STATUS);
                int nViolatePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_VIOLATE);
                if(cursor.moveToFirst()) {
                    do {
                        int nParentDocId = cursor.getInt(nDocIdPos);
                        if(nParentDocId > 0) //don't show connected documents
                            continue;

                        Document doc = new Document();
                        doc.mType = cursor.getInt(nTypePos);
                        switch (doc.mType) {
                            case Constants.TYPE_DOCUMENT:
                                doc.mName = mActivity.getString(R.string.indictment);
                                break;
                            case Constants.TYPE_SHEET:
                                doc.mName = mActivity.getString(R.string.sheet);
                                break;
                            default:
                                continue;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(nDatePos));
                        doc.mDate = calendar.getTime();

                        doc.mName += " " + cursor.getString(nNumberPos);
                        doc.mStatus = cursor.getInt(nStatusPos);
                        doc.mDesc = cursor.getString(nViolatePos);

                        doc.mId = cursor.getLong(nIdPos);

                        mDocuments.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        ILayer notes = mMap.getLayerById(mNotesId);
        if(notes != null){
            VectorLayer vlayer = (VectorLayer)notes;
            Cursor cursor = vlayer.query(new String[] { Constants.FIELD_NOTES_DATE_BEG,
                            Constants.FIELD_NOTES_DATE_END,
                            Constants.FIELD_NOTES_DESCRIPTION},
                    null, null, Constants.FIELD_NOTES_DATE_BEG + " ASC", " 100");
            if (null != cursor) {
                if(cursor.moveToFirst()) {
                    do {
                        Document doc = new Document();
                        doc.mType = Constants.TYPE_NOTE;

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(0));
                        doc.mDate = calendar.getTime();

                        doc.mName = mActivity.getString(R.string.note);

                        doc.mStatus = -1; //note status
                        doc.mDesc = cursor.getString(2);

                        mDocuments.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
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
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            v = inflater.inflate(R.layout.row_document, null);
        }

        Document item = (Document) getItem(position);

        ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
        switch (item.mType){
            case Constants.TYPE_DOCUMENT:
                ivIcon.setImageDrawable( mActivity.getResources().getDrawable(R.mipmap.ic_indicment));
                break;
            case Constants.TYPE_NOTE:
                ivIcon.setImageDrawable( mActivity.getResources().getDrawable(R.mipmap.ic_bookmark));
                break;
            case Constants.TYPE_SHEET:
                ivIcon.setImageDrawable( mActivity.getResources().getDrawable(R.mipmap.ic_sheet));
                break;
        }

        //TODO: state icon
        //ImageView ivStateIcon = (ImageView) v.findViewById(R.id.ivStateIcon);

        TextView tvStep = (TextView) v.findViewById(R.id.tvName);
        tvStep.setText(item.mName);

        TextView tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        tvDesc.setText(item.mDesc);

        TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy"); //(SimpleDateFormat) DateFormat.getDateInstance();//
        tvDate.setText(sdf.format(item.mDate));

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Document item = (Document) getItem(position);
        Intent intent;
        if(item.mType == Constants.TYPE_NOTE){
            //show notify activity
            intent = new Intent(mActivity, NotificationActivity.class);
            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, item.mId);
            mActivity.startActivity(intent);
        }
        else{
            //show documents activity
            intent = new Intent(mActivity, DocumentViewActivity.class);
            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, item.mId);
            mActivity.startActivity(intent);
        }
    }

    protected class Document implements Comparable{
        public String mName;
        public String mDesc;
        public Date mDate;
        public int mType;
        public int mStatus;
        public long mId;

        @Override
        public int compareTo(@NonNull Object another) {
            return mDate.compareTo(((Document)(another)).mDate);
        }
    }
}
