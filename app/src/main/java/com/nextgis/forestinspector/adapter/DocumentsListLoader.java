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
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapEventSource;
import com.nextgis.maplib.map.VectorLayer;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


// see example in
// http://developer.android.com/reference/android/content/AsyncTaskLoader.html
public class DocumentsListLoader
        extends AsyncTaskLoader<List<DocumentsListItem>>
        implements MapEventListener
{
    protected Context mContext;

    protected List<DocumentsListItem> mDocuments;
    protected MapEventSource          mMap;

    protected int mDocsId, mNotesId;

    protected boolean mShowIndictments = false;
    protected boolean mShowSheets      = false;
    protected boolean mShowFieldWorks  = false;
    protected boolean mShowNotes       = false;


    public DocumentsListLoader(
            Context context,
            Boolean showIndictments,
            Boolean showSheets,
            Boolean showFieldWorks,
            Boolean showNotes)
    {
        super(context);

        mContext = context;

        if (null == showIndictments && null == showSheets && null == showNotes) {
            mShowIndictments = mShowSheets = mShowFieldWorks = mShowNotes = true;
        } else {
            mShowIndictments = null == showIndictments ? false : showIndictments;
            mShowSheets = null == showSheets ? false : showSheets;
            mShowFieldWorks = null == showFieldWorks ? false : showFieldWorks;
            mShowNotes = null == showNotes ? false : showNotes;
        }

        mMap = (MapEventSource) MapBase.getInstance();
    }


    /**
     * This is where the bulk of our work is done.  This function is called in a background thread
     * and should generate a new set of data to be published by the loader.
     */
    @Override
    public List<DocumentsListItem> loadInBackground()
    {
        List<DocumentsListItem> documents = new LinkedList<>();

        if (null == mMap) {
            return documents;
        }


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


        if (mShowIndictments || mShowSheets || mShowFieldWorks) {
            ILayer docsLayer = mMap.getLayerById(mDocsId);
            if (docsLayer != null) {
                VectorLayer docs = (VectorLayer) docsLayer;

                //order by datetime(datetimeColumn) DESC LIMIT 100
                Cursor cursor = docs.query(
                        new String[] {
                                com.nextgis.maplib.util.Constants.FIELD_ID,
                                Constants.FIELD_DOCUMENTS_TYPE,
                                Constants.FIELD_DOC_ID,
                                Constants.FIELD_DOCUMENTS_DATE,
                                Constants.FIELD_DOCUMENTS_NUMBER,
                                Constants.FIELD_DOCUMENTS_STATUS,
                                Constants.FIELD_DOCUMENTS_VIOLATION_TYPE,
                                Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE}, null, null,
                        Constants.FIELD_DOCUMENTS_DATE + " DESC", " " + Constants.MAX_DOCUMENTS);

                if (null != cursor) {
                    int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                    int typePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_TYPE);
                    int docIdPos = cursor.getColumnIndex(Constants.FIELD_DOC_ID);
                    int datePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_DATE);
                    int numberPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_NUMBER);
                    int statusPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_STATUS);
                    int violatePos =
                            cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE);
                    int forestCatTypePos =
                            cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE);

                    if (cursor.moveToFirst()) {
                        do {
                            int nParentDocId = cursor.getInt(docIdPos);
                            if (nParentDocId > 0) //don't show connected documents
                            {
                                continue;
                            }

                            int docType = cursor.getInt(typePos);
                            if (!mShowIndictments && Constants.DOC_TYPE_INDICTMENT == docType
                                    || !mShowSheets && Constants.DOC_TYPE_SHEET == docType
                                    || !mShowFieldWorks
                                    && Constants.DOC_TYPE_FIELD_WORKS == docType) {
                                continue;
                            }

                            DocumentsListItem doc = new DocumentsListItem();
                            doc.mType = docType;
                            switch (docType) {
                                case Constants.DOC_TYPE_INDICTMENT:
                                    doc.mName = mContext.getString(R.string.indictment);
                                    doc.mDesc = cursor.getString(violatePos);
                                    break;
                                case Constants.DOC_TYPE_SHEET:
                                    doc.mName = mContext.getString(R.string.sheet_item_name);
                                    doc.mDesc = "";
                                    break;
                                case Constants.DOC_TYPE_FIELD_WORKS:
                                    doc.mName = mContext.getString(R.string.field_works_item_name);
                                    doc.mDesc = mContext.getString(R.string.field_works_for) + " "
                                            + cursor.getString(forestCatTypePos);
                                    break;
                                default:
                                    continue;
                            }

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(cursor.getLong(datePos));
                            doc.mDate = calendar.getTime();

                            doc.mName += " " + cursor.getString(numberPos);
                            doc.mStatus = cursor.getInt(statusPos);

                            doc.mId = cursor.getLong(idPos);

                            documents.add(doc);

                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }
        }


        if (mShowNotes) {
            ILayer notesLayer = mMap.getLayerById(mNotesId);
            if (notesLayer != null) {
                VectorLayer notes = (VectorLayer) notesLayer;

                String selection =
                        Constants.FIELD_NOTES_DATE_END + " >= " + System.currentTimeMillis();

                Cursor cursor = notes.query(
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
                            DocumentsListItem doc = new DocumentsListItem();
                            doc.mType = Constants.DOC_TYPE_NOTE;

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(cursor.getLong(dateBegPos));
                            doc.mDate = calendar.getTime();

                            doc.mName = mContext.getString(R.string.note_item_name);

                            doc.mStatus = -1; //note status
                            doc.mDesc = cursor.getString(descPos);

                            doc.mId = cursor.getLong(idPos);

                            documents.add(doc);

                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }
        }


        Collections.sort(documents);

        return documents;
    }


    /**
     * Called when there is new data to deliver to the client.  The super class will take care of
     * delivering it; the implementation here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<DocumentsListItem> documents)
    {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We don't need the result.
            if (documents != null) {
                onReleaseResources(documents);
            }
        }

        List<DocumentsListItem> oldDocuments = null;
        if (documents != mDocuments) {
            oldDocuments = mDocuments;
            mDocuments = documents;
        }

        if (isStarted()) {
            // If the Loader is currently started, we can immediately deliver its results.
            super.deliverResult(documents);
        }

        // At this point we can release the resources associated with 'oldDocuments' if needed;
        // now that the new result is delivered we know that it is no longer in use.
        if (oldDocuments != null) {
            onReleaseResources(oldDocuments);
        }
    }


    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading()
    {
        if (mDocuments != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mDocuments);
        }

        // Start watching for changes in the documents.
        if (null != mMap) {
            mMap.addListener(this);
        }

        if (takeContentChanged() || mDocuments == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }


    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading()
    {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }


    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(List<DocumentsListItem> documents)
    {
        super.onCanceled(documents);

        // At this point we can release the resources associated with 'documents' if needed.
        onReleaseResources(documents);
    }


    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset()
    {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'mDocuments'  if needed.
        if (mDocuments != null) {
            onReleaseResources(mDocuments);
            mDocuments = null;
        }

        // Stop monitoring for changes.
        if (null != mMap) {
            mMap.removeListener(this);
        }
    }


    /**
     * Helper function to take care of releasing resources associated with an actively loaded data
     * set.
     */
    protected void onReleaseResources(List<DocumentsListItem> documents)
    {
        if (null != documents) {
            documents.clear();
        }
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
            // Tell the loader about the change.
            onContentChanged();
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
}
