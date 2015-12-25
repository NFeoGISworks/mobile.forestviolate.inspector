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
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.DocumentViewActivity;
import com.nextgis.forestinspector.activity.NoteCreatorActivity;
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


    public DocumentsListAdapter(Context context)
    {
        super(context);

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
    public void setSelection(
            int position,
            boolean selection)
    {
        DocumentsListItem item = mDocuments.get(position);

        switch (item.mType) {
            case Constants.DOC_TYPE_INDICTMENT:
            case Constants.DOC_TYPE_SHEET:
                selection = false;
                break;
            case Constants.DOC_TYPE_NOTE:
            default:
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
