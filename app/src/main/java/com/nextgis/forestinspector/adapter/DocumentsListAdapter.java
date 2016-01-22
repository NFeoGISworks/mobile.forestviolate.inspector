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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.internal.widget.ThemeUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.DocumentViewActivity;
import com.nextgis.forestinspector.activity.FieldWorksCreatorActivity;
import com.nextgis.forestinspector.activity.IndictmentCreatorActivity;
import com.nextgis.forestinspector.activity.NoteCreatorActivity;
import com.nextgis.forestinspector.activity.SheetCreatorActivity;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.GeoGeometry;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapEventSource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.nextgis.maplib.util.Constants.*;


/**
 * The main document list
 */
public class DocumentsListAdapter
        extends ListSelectorAdapter
{
    protected MapEventSource mMap;
    protected String mDocsPathName;
    protected String mNotesPathName;
    protected List<DocumentsListItem> mDocuments;
    protected long mUserId;
    protected int mUserItemBackgroundColor;

    protected OnDocLongClickListener mOnDocLongClickListener;


    public DocumentsListAdapter(Context context)
    {
        super(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUserId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
        mUserItemBackgroundColor =
                ThemeUtils.getThemeAttrColor(mContext, R.attr.docItemBackgroundForCurrentUser);

        mMap = (MapEventSource) MapBase.getInstance();

        ILayer docsLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_DOCUMENTS);
        if (null != docsLayer) {
            mDocsPathName = docsLayer.getPath().getName();
        }

        ILayer notesLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_NOTES);
        if (null != notesLayer) {
            mNotesPathName = notesLayer.getPath().getName();
        }
    }


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
                        DocumentsListItem item = mDocuments.get(position);
                        Intent intent = null;
                        if (item.mType == Constants.DOC_TYPE_NOTE) {
                            //show note activity
                            intent = new Intent(mContext, NoteCreatorActivity.class);
                        } else {
                            if (item.mIsSigned) {
                                //show documents viewer activity
                                intent = new Intent(mContext, DocumentViewActivity.class);
                                intent.putExtra(Constants.DOCUMENT_VIEWER, true);
                            } else {
                                //show documents creator activity
                                switch (item.mType) {
                                    case Constants.DOC_TYPE_INDICTMENT:
                                        intent = new Intent(
                                                mContext, IndictmentCreatorActivity.class);
                                        break;
                                    case Constants.DOC_TYPE_SHEET:
                                        intent = new Intent(mContext, SheetCreatorActivity.class);
                                        break;
                                    case Constants.DOC_TYPE_FIELD_WORKS:
                                        intent = new Intent(
                                                mContext, FieldWorksCreatorActivity.class);
                                        break;
                                }
                            }
                        }

                        if (null != intent) {
                            intent.putExtra(FIELD_ID, item.mId);
                            mContext.startActivity(intent);
                        }
                    }
                });

        setOnItemLongClickListener(
                new ListSelectorAdapter.ViewHolder.OnItemLongClickListener()
                {
                    @Override
                    public void onItemLongClick(int position)
                    {
                        String layerPathName = null;
                        DocumentsListItem item = mDocuments.get(position);
                        boolean isPoint = false;

                        switch (item.mType) {
                            case Constants.DOC_TYPE_INDICTMENT:
                            case Constants.DOC_TYPE_SHEET:
                            case Constants.DOC_TYPE_FIELD_WORKS:
                                layerPathName = mDocsPathName;
                                break;

                            case Constants.DOC_TYPE_NOTE:
                                layerPathName = mNotesPathName;
                                isPoint = true;
                                break;
                        }

                        if (null != layerPathName) {
                            Uri uri = Uri.parse(
                                    "content://" + SettingsConstants.AUTHORITY + "/" + layerPathName
                                            + "/" + item.mId);

                            String[] columns = new String[] {FIELD_GEOM};

                            Cursor cursor = mContext.getContentResolver()
                                    .query(uri, columns, null, null, null);

                            if (null != cursor) {
                                if (cursor.moveToFirst()) {
                                    GeoGeometry geometry = null;
                                    try {
                                        geometry = GeoGeometryFactory.fromBlob(cursor.getBlob(0));
                                    } catch (IOException | ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    if (null != geometry && null != mOnDocLongClickListener) {
                                        mOnDocLongClickListener.onDocLongClick(geometry, isPoint);
                                    }
                                }
                                cursor.close();
                            }
                        }
                    }
                });

        return new DocumentsListAdapter.ViewHolder(
                itemView, mOnItemClickListener, mOnItemLongClickListener);
    }


    @Override
    public void setSelection(
            int position,
            boolean selection)
    {
        DocumentsListItem item = mDocuments.get(position);

        switch (item.mType) {
            case Constants.DOC_TYPE_INDICTMENT:
            case Constants.DOC_TYPE_SHEET:
            case Constants.DOC_TYPE_FIELD_WORKS:
            default:
                if (item.mIsSigned) {
                    selection = false;
                }
                break;

            case Constants.DOC_TYPE_NOTE:
                break;
        }

        super.setSelection(position, selection);
    }


    @Override
    public void onBindViewHolder(
            ListSelectorAdapter.ViewHolder holder,
            int position)
    {
        super.onBindViewHolder(holder, position);

        DocumentsListAdapter.ViewHolder viewHolder = (DocumentsListAdapter.ViewHolder) holder;
        DocumentsListItem item = mDocuments.get(position);

        switch (item.mType) {
            case Constants.DOC_TYPE_INDICTMENT:
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_indicment));
                break;
            case Constants.DOC_TYPE_SHEET:
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_sheet));
                break;
            case Constants.DOC_TYPE_FIELD_WORKS:
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_fieldworks));
                break;
            case Constants.DOC_TYPE_NOTE:
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_bookmark));
                break;
        }


        if (!item.mIsSigned || item.mType == Constants.DOC_TYPE_NOTE) {
            viewHolder.mCheckBox.setEnabled(true);
        } else {
            viewHolder.mCheckBox.setEnabled(false);
        }


        int alpha = item.mIsSigned ? 255 : 50;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewHolder.mTypeIcon.setAlpha(alpha);
        } else {
            viewHolder.mTypeIcon.setImageAlpha(alpha);
        }


        if (mUserId == item.mUserId) {
            viewHolder.mItemLayout.setBackgroundColor(mUserItemBackgroundColor);
        } else {
            viewHolder.mItemLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        viewHolder.mTypeName.setText(item.mTypeName);
        viewHolder.mDocDesc.setText(item.mDesc);

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yy"); //(SimpleDateFormat) DateFormat.getDateInstance();
        viewHolder.mDocDate.setText(sdf.format(item.mDate));


        switch (item.mStatus) {
            case Constants.DOCUMENT_STATUS_NEW:
                viewHolder.mStateIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.ic_document_status_new));
                break;

            case Constants.DOCUMENT_STATUS_FOR_SEND:
                viewHolder.mStateIcon.setImageDrawable(
                        mContext.getResources()
                                .getDrawable(R.drawable.ic_document_status_sent_partially));
                break;

            case Constants.DOCUMENT_STATUS_OK:
                viewHolder.mStateIcon.setImageDrawable(
                        mContext.getResources()
                                .getDrawable(R.drawable.ic_document_status_sent_full));
                break;

            default:
                viewHolder.mStateIcon.setImageDrawable(null);
                break;
        }
    }


    public static class ViewHolder
            extends ListSelectorAdapter.ViewHolder
            implements View.OnClickListener
    {
        LinearLayout mItemLayout;
        ImageView    mTypeIcon;
        TextView     mTypeName;
        TextView     mDocDesc;
        TextView     mDocDate;
        ImageView    mStateIcon;


        public ViewHolder(
                View itemView,
                OnItemClickListener clickListener,
                OnItemLongClickListener longClickListener)
        {
            super(itemView, clickListener, longClickListener);

            mItemLayout = (LinearLayout) itemView.findViewById(R.id.item_layout);
            mTypeIcon = (ImageView) itemView.findViewById(R.id.type_icon);
            mTypeName = (TextView) itemView.findViewById(R.id.type_name);
            mDocDesc = (TextView) itemView.findViewById(R.id.doc_desc);
            mDocDate = (TextView) itemView.findViewById(R.id.doc_date);
            mStateIcon = (ImageView) itemView.findViewById(R.id.state_icon);
        }
    }


    public void setDocuments(List<DocumentsListItem> documents)
    {
        if (documents == mDocuments) {
            return;
        }

        clearSelectionForAll();

        mDocuments = documents;
        notifyDataSetChanged();
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

        String layerPathName = null;
        DocumentsListItem item = mDocuments.get(id);
        boolean isSavedDoc = false;

        switch (item.mType) {
            case Constants.DOC_TYPE_INDICTMENT:
            case Constants.DOC_TYPE_SHEET:
            case Constants.DOC_TYPE_FIELD_WORKS:
                layerPathName = mDocsPathName;
                isSavedDoc = true;
                break;

            case Constants.DOC_TYPE_NOTE:
                layerPathName = mNotesPathName;
                break;
        }

        if (null != layerPathName) {
            Uri uri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" + layerPathName + "/"
                            + item.mId);

            if (isSavedDoc) {
                uri = uri.buildUpon()
                        .appendQueryParameter(URI_PARAMETER_NOT_SYNC, Boolean.FALSE.toString())
                        .build();
            }

            if (mContext.getContentResolver().delete(uri, null, null) <= 0) {
                Log.d(Constants.FITAG, "delete feature into " + layerPathName + " failed");
            }
        }

        mDocuments.remove(id);
    }


    public void setOnDocLongClickListener(OnDocLongClickListener onDocLongClickListener)
    {
        mOnDocLongClickListener = onDocLongClickListener;
    }


    public interface OnDocLongClickListener
    {
        void onDocLongClick(
                GeoGeometry geometry,
                boolean isPoint);
    }
}
