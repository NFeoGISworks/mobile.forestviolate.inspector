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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapEventSource;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.LayerUtil;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

    protected int mDocsLayerId, mNotesLayerId, mTargetsLayerId;

    protected boolean mShowIndictments = false;
    protected boolean mShowSheets      = false;
    protected boolean mShowFieldWorks  = false;
    protected boolean mShowNotes       = false;
    protected boolean mShowTargets     = false;

    protected List<Long> mDocsIds;
    protected List<Long> mNotesIds;
    protected List<Long> mTargetsIds;

    protected long mUserId;


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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUserId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
    }


    public DocumentsListLoader(
            Context context,
            List<Long> docsIds,
            List<Long> notesIds,
            List<Long> targetsIds)
    {
        super(context);

        mContext = context;
        mDocsIds = docsIds;
        mNotesIds = notesIds;
        mTargetsIds = targetsIds;

        mMap = (MapEventSource) MapBase.getInstance();
        mShowTargets = true;
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

        mDocsLayerId = mNotesLayerId = mTargetsLayerId = -10;

        ILayer docsLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if (null != docsLayer) {
            mDocsLayerId = docsLayer.getId();
        }

        ILayer notesLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_NOTES);
        if (null != notesLayer) {
            mNotesLayerId = notesLayer.getId();
        }

        ILayer targetsLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_FV);
        if (null != targetsLayer) {
            mTargetsLayerId = targetsLayer.getId();
        }

        if ((mShowIndictments || mShowSheets || mShowFieldWorks
                || mShowTargets && null != mDocsIds && mDocsIds.size() > 0) && docsLayer != null) {

            VectorLayer docs = (VectorLayer) docsLayer;

            String[] columns = new String[] {
                    com.nextgis.maplib.util.Constants.FIELD_ID,
                    Constants.FIELD_DOCUMENTS_USER_ID,
                    Constants.FIELD_DOCUMENTS_TYPE,
                    Constants.FIELD_DOC_ID,
                    Constants.FIELD_DOCUMENTS_DATE,
                    Constants.FIELD_DOCUMENTS_NUMBER,
                    Constants.FIELD_DOCUMENTS_STATUS,
                    Constants.FIELD_DOCUMENTS_VIOLATION_TYPE,
                    Constants.FIELD_DOCUMENTS_FOREST_CAT_TYPE};

            String selection = null;
            if (mShowTargets) {
                selection = LayerUtil.getSelectionForIds(mDocsIds);
            }

            String sortOrder = Constants.FIELD_DOCUMENTS_DATE + " DESC";
            String limit = " " + Constants.MAX_DOCUMENTS;

            //order by datetime(datetimeColumn) DESC LIMIT 100
            Cursor cursor = docs.query(columns, selection, null, sortOrder, limit);

            if (null != cursor) {
                int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int userIdPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_USER_ID);
                int typePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_TYPE);
                int docIdPos = cursor.getColumnIndex(Constants.FIELD_DOC_ID);
                int datePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_DATE);
                int numberPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_NUMBER);
                int statusPos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_STATUS);
                int violatePos = cursor.getColumnIndex(Constants.FIELD_DOCUMENTS_VIOLATION_TYPE);
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
                        if (!mShowTargets && (
                                !mShowIndictments && Constants.DOC_TYPE_INDICTMENT == docType
                                        || !mShowSheets && Constants.DOC_TYPE_SHEET == docType
                                        || !mShowFieldWorks
                                        && Constants.DOC_TYPE_FIELD_WORKS == docType)) {
                            continue;
                        }

                        DocumentsListItem doc = new DocumentsListItem();

                        switch (docType) {
                            case Constants.DOC_TYPE_INDICTMENT:
                                doc.mTypeName = mContext.getString(R.string.indictment);
                                doc.mDesc = cursor.getString(violatePos);
                                break;
                            case Constants.DOC_TYPE_SHEET:
                                doc.mTypeName = mContext.getString(R.string.sheet_item_name);
                                doc.mDesc = "";
                                break;
                            case Constants.DOC_TYPE_FIELD_WORKS:
                                doc.mTypeName = mContext.getString(R.string.field_works_item_name);
                                doc.mDesc = mContext.getString(R.string.field_works_for) + " "
                                        + cursor.getString(forestCatTypePos);
                                break;
                            default:
                                continue;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(datePos));

                        doc.mType = docType;
                        doc.mDate = calendar.getTime();
                        doc.mTypeName += " " + cursor.getString(numberPos);
                        doc.mStatus = cursor.getInt(statusPos);
                        doc.mId = cursor.getLong(idPos);
                        doc.mUserId = cursor.getLong(userIdPos);
                        doc.mIsSigned = !docs.hasFeatureNotSyncFlag(doc.mId);

                        documents.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }


        if ((mShowNotes || mShowTargets && null != mNotesIds && mNotesIds.size() > 0)
                && notesLayer != null) {

            VectorLayer notes = (VectorLayer) notesLayer;

            String[] columns = new String[] {
                    com.nextgis.maplib.util.Constants.FIELD_ID,
                    Constants.FIELD_NOTES_DATE_BEG,
                    Constants.FIELD_NOTES_DATE_END,
                    Constants.FIELD_NOTES_DESCRIPTION};

            String selection;
            if (mShowTargets) {
                selection = LayerUtil.getSelectionForIds(mNotesIds);
            } else {
                selection = Constants.FIELD_NOTES_DATE_END + " >= " + System.currentTimeMillis();
            }

            String sortOrder = Constants.FIELD_NOTES_DATE_BEG + " DESC";
            String limit = " " + Constants.MAX_NOTES;

            Cursor cursor = notes.query(columns, selection, null, sortOrder, limit);

            if (null != cursor) {
                int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int dateBegPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DATE_BEG);
                int dateEndPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DATE_END);
                int descPos = cursor.getColumnIndex(Constants.FIELD_NOTES_DESCRIPTION);

                if (cursor.moveToFirst()) {
                    do {
                        DocumentsListItem doc = new DocumentsListItem();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(dateBegPos));

                        doc.mType = Constants.DOC_TYPE_NOTE;
                        doc.mDate = calendar.getTime();
                        doc.mTypeName = mContext.getString(R.string.note_item_name);
                        doc.mStatus = -1; //note status
                        doc.mDesc = cursor.getString(descPos);
                        doc.mId = cursor.getLong(idPos);
                        doc.mUserId = mUserId;

                        documents.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }


        if ((mShowTargets && null != mTargetsIds && mTargetsIds.size() > 0)
                && targetsLayer != null) {

            VectorLayer targets = (VectorLayer) targetsLayer;

            String[] columns = {
                    com.nextgis.maplib.util.Constants.FIELD_ID,
                    Constants.FIELD_FV_DATE,
                    Constants.FIELD_FV_FORESTRY,
                    Constants.FIELD_FV_PRECINCT,
                    Constants.FIELD_FV_REGION,
                    Constants.FIELD_FV_TERRITORY};

//            String selection =
//                    Constants.FIELD_FV_STATUS + " = " + Constants.FV_STATUS_NEW_FOREST_CHANGE;
            String selection = LayerUtil.getSelectionForIds(mTargetsIds);

            String sortOrder = Constants.FIELD_FV_DATE + " DESC";

            Cursor cursor = targets.query(columns, selection, null, sortOrder, null);

            if (null != cursor) {
                int idPos = cursor.getColumnIndex(com.nextgis.maplib.util.Constants.FIELD_ID);
                int datePos = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_DATE);
                int forestryPos = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_FORESTRY);
                int precinctPos = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_PRECINCT);
                int regionPos = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_REGION);
                int territoryPos = cursor.getColumnIndexOrThrow(Constants.FIELD_FV_TERRITORY);

                if (cursor.moveToFirst()) {
                    do {
                        DocumentsListItem doc = new DocumentsListItem();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(cursor.getLong(datePos));

                        doc.mType = Constants.DOC_TYPE_TARGET;
                        doc.mDate = calendar.getTime();
                        doc.mTypeName = mContext.getString(R.string.target_item_name);
                        doc.mStatus = -1; //target status
                        doc.mDesc = cursor.getString(regionPos) + " " +
                                cursor.getString(forestryPos) + "\n" +
                                cursor.getString(precinctPos) + " " +
                                cursor.getString(territoryPos);
                        doc.mId = cursor.getLong(idPos);

                        documents.add(doc);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        if (mShowTargets) {
            Collections.sort(
                    documents, new Comparator<DocumentsListItem>()
                    {
                        @Override
                        public int compare(
                                DocumentsListItem lhs,
                                DocumentsListItem rhs)
                        {
                            int res = compareTypes(lhs.mType, rhs.mType);
                            if (res == 0) {
                                return lhs.mDate.compareTo(rhs.mDate);
                            }
                            return res;
                        }
                    });
        } else {
            Collections.sort(documents);
        }

        return documents;
    }


    protected int compareTypes(
            int lhs,
            int rhs)
    {
        if (lhs <= Constants.DOC_TYPE_SHEET && rhs <= Constants.DOC_TYPE_SHEET) {
            return 0;
        }

        if (lhs <= Constants.DOC_TYPE_SHEET && rhs >= Constants.DOC_TYPE_TARGET) {
            return -1;
        }

        if (lhs >= Constants.DOC_TYPE_TARGET && rhs <= Constants.DOC_TYPE_SHEET) {
            return 1;
        }

        // lhs >= Constants.DOC_TYPE_TARGET && rhs >= Constants.DOC_TYPE_TARGET
        return compareInteger(lhs, rhs);
    }


    protected int compareInteger(
            int lhs,
            int rhs)
    {
        // from source Integer.compare()
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
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
        if (id == mDocsLayerId || id == mNotesLayerId || mShowTargets && id == mTargetsLayerId) {
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
