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
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapEventSource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * The main document list
 */
public class DocumentsListAdapter
        extends ListSelectorAdapter
{
    protected MapEventSource          mMap;
    protected String                  mNotesPathName;
    protected List<DocumentsListItem> mDocuments;
    protected long                    mUserId;
    protected int                     mUserItemBackgroundColor;


    public DocumentsListAdapter(Context context)
    {
        super(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUserId = prefs.getInt(SettingsConstants.KEY_PREF_USERID, -1);
        mUserItemBackgroundColor =
                ThemeUtils.getThemeAttrColor(mContext, R.attr.docItemBackgroundForCurrentUser);

        mMap = (MapEventSource) MapBase.getInstance();

        for (int i = 0, size = null != mMap ? mMap.getLayerCount() : 0; i < size; ++i) {
            ILayer layer = mMap.getLayer(i);

            if (layer.getName().equals(mContext.getString(R.string.notes))) {
                mNotesPathName = layer.getPath().getName();
                break;
            }
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
                            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, item.mId);
                            mContext.startActivity(intent);
                        }
                    }
                });

        return new DocumentsListAdapter.ViewHolder(itemView, mOnItemClickListener);
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

        viewHolder.mTypeName.setText(item.mName);
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
                        mContext.getResources().getDrawable(R.drawable.ic_document_status_sent_partially));
                break;

            case Constants.DOCUMENT_STATUS_OK:
                viewHolder.mStateIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.drawable.ic_document_status_sent_full));
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
                OnItemClickListener listener)
        {
            super(itemView, listener);

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

        if (null != mNotesPathName) {
            DocumentsListItem item = mDocuments.get(id);
            Uri uri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" + mNotesPathName + "/"
                            + item.mId);

            if (mContext.getContentResolver().delete(uri, null, null) <= 0) {
                Log.d(Constants.FITAG, "delete feature into " + mNotesPathName + " failed");
            }
        }

        mDocuments.remove(id);
    }
}
