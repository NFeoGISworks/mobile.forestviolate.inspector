/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.DocumentViewActivity;
import com.nextgis.forestinspector.activity.NoteCreatorActivity;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapEventSource;
import com.nextgis.maplib.map.VectorLayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * The main document list
 */
public class DocumentsListAdapter
        extends ListSelectorAdapter
        implements MapEventListener
{
    protected int mDocsId, mNotesId;

    protected List<Document> mDocuments;
    protected MapEventSource mMap;


    @Override
    protected int getItemViewResId()
    {
        return R.layout.item_document;
    }


    @Override
    protected ListSelectorAdapter.ViewHolder getViewHolder(View itemView)
    {
        setOnItemClickListener(
                new ListSelectorAdapter.ViewHolder.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(int position)
                    {
                        Document item = mDocuments.get(position);
                        Intent intent;
                        if (item.mType == Constants.DOC_TYPE_NOTE) {
                            //show note activity
                            intent = new Intent(mContext, NoteCreatorActivity.class);
                            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, item.mId);
                            mContext.startActivity(intent);
                        } else {
                            //show documents activity
                            intent = new Intent(mContext, DocumentViewActivity.class);
                            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, item.mId);
                            mContext.startActivity(intent);
                        }
                    }
                });

        return new DocumentsListAdapter.ViewHolder(itemView, mOnItemClickListener);
    }


    @Override
    public void onBindViewHolder(
            ListSelectorAdapter.ViewHolder holder,
            int position)
    {
        super.onBindViewHolder(holder, position);

        DocumentsListAdapter.ViewHolder viewHolder = (DocumentsListAdapter.ViewHolder) holder;
        Document item = mDocuments.get(position);

        switch (item.mType) {
            case Constants.DOC_TYPE_INDICTMENT:
                viewHolder.mCheckBox.setEnabled(false);
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_indicment));
                break;
            case Constants.DOC_TYPE_NOTE:
                viewHolder.mCheckBox.setEnabled(true);
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_bookmark));
                break;
            case Constants.DOC_TYPE_SHEET:
                viewHolder.mCheckBox.setEnabled(false);
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_sheet));
                break;
        }

        viewHolder.mTypeName.setText(item.mName);
        viewHolder.mDocDesc.setText(item.mDesc);

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yy"); //(SimpleDateFormat) DateFormat.getDateInstance();
        viewHolder.mDocDate.setText(sdf.format(item.mDate));

        // TODO: state icon
        // viewHolder.mStateIcon.setImageDrawable();
    }


    public static class ViewHolder
            extends ListSelectorAdapter.ViewHolder
            implements View.OnClickListener
    {
        ImageView mTypeIcon;
        TextView  mTypeName;
        TextView  mDocDesc;
        TextView  mDocDate;
        ImageView mStateIcon;


        public ViewHolder(
                View itemView,
                OnItemClickListener listener)
        {
            super(itemView, listener);

            mTypeIcon = (ImageView) itemView.findViewById(R.id.type_icon);
            mTypeName = (TextView) itemView.findViewById(R.id.type_name);
            mDocDesc = (TextView) itemView.findViewById(R.id.doc_desc);
            mDocDate = (TextView) itemView.findViewById(R.id.doc_date);
            mStateIcon = (ImageView) itemView.findViewById(R.id.state_icon);
        }
    }


    public DocumentsListAdapter(Context context)
    {
        super(context);

        mDocuments = new LinkedList<>();
        mMap = (MapEventSource) MapBase.getInstance();
        mDocsId = mNotesId = -10;

        for (int i = 0; i < mMap.getLayerCount(); ++i) {
            ILayer layer = mMap.getLayer(i);

            if (layer instanceof DocumentsLayer) {
                mDocsId = layer.getId();
            } else if (layer.getName().equals(mContext.getString(R.string.notes))) {
                mNotesId = layer.getId();
            }

            if (mDocsId > -10 && mNotesId > -10) {
                break;
            }
        }

        loadData();

        if (null != mMap) {
            mMap.addListener(this);
        }
    }


    @Override
    protected void finalize()
            throws Throwable
    {
        if (null != mMap) {
            mMap.removeListener(this);
        }
        super.finalize();
    }


    protected void loadData()
    {
        mDocuments.clear();

        ILayer docs = mMap.getLayerById(mDocsId);

        if (docs != null) {
            VectorLayer vlayer = (VectorLayer) docs;

            //order by datetime(datetimeColumn) DESC LIMIT 100
            Cursor cursor = vlayer.query(
                    new String[] {
                            com.nextgis.maplib.util.Constants.FIELD_ID,
                            Constants.FIELD_DOCUMENTS_TYPE,
                            Constants.FIELD_DOC_ID,
                            Constants.FIELD_DOCUMENTS_DATE,
                            Constants.FIELD_DOCUMENTS_NUMBER,
                            Constants.FIELD_DOCUMENTS_STATUS,
                            Constants.FIELD_DOCUMENTS_VIOLATION_TYPE}, null, null,
                    Constants.FIELD_DOCUMENTS_DATE + " DESC", " " + Constants.MAX_DOCUMENTS);

            if (null != cursor) {
                int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int typePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_TYPE);
                int docIdPos = cursor.getColumnIndex(Constants.FIELD_DOC_ID);
                int datePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_DATE);
                int numberPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_NUMBER);
                int statusPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_STATUS);
                int violatePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE);

                if (cursor.moveToFirst()) {
                    do {
                        int nParentDocId = cursor.getInt(docIdPos);
                        if (nParentDocId > 0) //don't show connected documents
                        {
                            continue;
                        }

                        Document doc = new Document();
                        doc.mType = cursor.getInt(typePos);
                        switch (doc.mType) {
                            case Constants.DOC_TYPE_INDICTMENT:
                                doc.mName = mContext.getString(R.string.indictment);
                                break;
                            case Constants.DOC_TYPE_SHEET:
                                doc.mName = mContext.getString(R.string.sheet_item_name);
                                break;
                            default:
                                continue;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(datePos));
                        doc.mDate = calendar.getTime();

                        doc.mName += " " + cursor.getString(numberPos);
                        doc.mStatus = cursor.getInt(statusPos);
                        doc.mDesc = cursor.getString(violatePos);

                        doc.mId = cursor.getLong(idPos);

                        mDocuments.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        ILayer notes = mMap.getLayerById(mNotesId);
        if (notes != null) {
            VectorLayer notesLayer = (VectorLayer) notes;

            String selection = Constants.FIELD_NOTES_DATE_END + " >= " + System.currentTimeMillis();

            Cursor cursor = notesLayer.query(
                    new String[] {
                            com.nextgis.maplib.util.Constants.FIELD_ID,
                            Constants.FIELD_NOTES_DATE_BEG,
                            Constants.FIELD_NOTES_DATE_END,
                            Constants.FIELD_NOTES_DESCRIPTION}, selection, null,
                    Constants.FIELD_NOTES_DATE_BEG + " DESC", " " + Constants.MAX_NOTES);

            if (null != cursor) {
                int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int dateBegPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DATE_BEG);
                int dateEndPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DATE_END);
                int descPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DESCRIPTION);

                if (cursor.moveToFirst()) {
                    do {
                        Document doc = new Document();
                        doc.mType = Constants.DOC_TYPE_NOTE;

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(dateBegPos));
                        doc.mDate = calendar.getTime();

                        doc.mName = mContext.getString(R.string.note_item_name);

                        doc.mStatus = -1; //note status
                        doc.mDesc = cursor.getString(descPos);

                        doc.mId = cursor.getLong(idPos);

                        mDocuments.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        Collections.sort(mDocuments);
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mDocuments) {
            return super.getItemId(position);
        }

        return position;
    }


    @Override
    public int getItemCount()
    {
        if (null == mDocuments) {
            return 0;
        }
        return mDocuments.size();
    }


    @Override
    public void deleteSelected(int id)
            throws IOException
    {
        super.deleteSelected(id);

        ILayer notes = mMap.getLayerById(mNotesId);
        if (notes != null) {
            VectorLayer notesLayer = (VectorLayer) notes;
            String pathName = notesLayer.getPath().getName();
            Document item = mDocuments.get(id);
            Uri uri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" + pathName + "/" + item.mId);

            if (notesLayer.delete(uri, null, null) <= 0) {
                Log.d(Constants.FITAG, "delete feature into " + pathName + " failed");
            }
        }

        mDocuments.remove(id);
    }


    @Override
    public void onLayerAdded(int id)
    {

    }


    @Override
    public void onLayerDeleted(int id)
    {

    }


    @Override
    public void onLayerChanged(int id)
    {
        //work only on docs and notes layers
        if (id == mDocsId || id == mNotesId) {
            loadData();
            //reload
            notifyDataSetChanged();
        }
    }


    @Override
    public void onExtentChanged(
            float zoom,
            GeoPoint center)
    {

    }


    @Override
    public void onLayersReordered()
    {

    }


    @Override
    public void onLayerDrawFinished(
            int id,
            float percent)
    {

    }


    @Override
    public void onLayerDrawStarted()
    {

    }


    protected class Document
            implements Comparable<Document>
    {
        public String mName;
        public String mDesc;
        public Date   mDate;
        public int    mType;
        public int    mStatus;
        public long   mId;


        @Override
        public int compareTo(
                @NonNull
                Document another)
        {
            return mDate.compareTo(another.mDate);
        }
    }
}
