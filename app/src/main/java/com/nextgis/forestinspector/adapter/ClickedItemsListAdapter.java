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

package com.nextgis.forestinspector.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;

import java.text.SimpleDateFormat;


public class ClickedItemsListAdapter
        extends DocumentsListAdapter
{
    protected String mTargetsPathName;

    protected OnListItemClickListener mOnListItemClickListener;


    public ClickedItemsListAdapter(Context context)
    {
        super(context);

        ILayer targetsLayer = mMap.getLayerByPathName(Constants.KEY_LAYER_FV);
        if (null != targetsLayer) {
            mTargetsPathName = targetsLayer.getPath().getName();
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
                        if (null != mOnListItemClickListener) {
                            mOnListItemClickListener.onListItemClick(mDocuments.get(position));
                        }
                    }
                });

        setOnItemLongClickListener(null);

        return new ClickedItemsListAdapter.ViewHolder(
                itemView, mOnItemClickListener, mOnItemLongClickListener);
    }


    @Override
    public void onBindViewHolder(
            ListSelectorAdapter.ViewHolder holder,
            int position)
    {
        ClickedItemsListAdapter.ViewHolder viewHolder = (ClickedItemsListAdapter.ViewHolder) holder;
        DocumentsListItem item = mDocuments.get(position);

        viewHolder.mCheckBox.setVisibility(View.GONE);


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
            case Constants.DOC_TYPE_TARGET:
                viewHolder.mTypeIcon.setImageDrawable(
                        mContext.getResources().getDrawable(R.mipmap.ic_target));
                viewHolder.mDocDesc.setMaxLines(4);
                break;
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


    public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener)
    {
        mOnListItemClickListener = onListItemClickListener;
    }


    public interface OnListItemClickListener
    {
        void onListItemClick(DocumentsListItem item);
    }
}
