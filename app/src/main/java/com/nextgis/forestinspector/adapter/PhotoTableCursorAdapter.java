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

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.activity.PhotoTableFillerActivity;
import com.nextgis.forestinspector.fragment.PhotoTableFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PhotoTableCursorAdapter
        extends PhotoTableAdapter
{
    protected DocumentsLayer mDocsLayer;
    protected long           mFeatureId;
    protected Uri            mAttachesUri;


    public PhotoTableCursorAdapter(
            AppCompatActivity activity,
            DocumentsLayer docsLayer,
            long featureId,
            Map<String, AttachItem> attachItemMap,
            boolean isPhotoViewer)
    {
        super(activity, attachItemMap, isPhotoViewer);

        mDocsLayer = docsLayer;
        mFeatureId = featureId;

        MainApplication app = (MainApplication) activity.getApplication();
        String docsLayerPathName = mDocsLayer.getPath().getName();

        mAttachesUri = Uri.parse(
                "content://" + app.getAuthority() + "/" + docsLayerPathName + "/" + featureId + "/"
                        + Constants.URI_ATTACH);
    }


    @Override
    public void onBindViewHolder(
            final ListSelectorAdapter.ViewHolder holder,
            final int position)
    {
        super.onBindViewHolder(holder, position);

        PhotoTableAdapter.ViewHolder viewHolder = (PhotoTableAdapter.ViewHolder) holder;

        viewHolder.mCheckBox.setVisibility(View.GONE);
        viewHolder.mPhotoDesc.setOnClickListener(null);

        if (!mIsPhotoViewer) {
            viewHolder.mImageView.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            ImageView imageView = (ImageView) view;
                            mClickedId = (Integer) imageView.getTag();

                            String key = mAttachItemList.get(mClickedId).getKey();

                            Intent intent = new Intent(mActivity, PhotoTableFillerActivity.class);
                            intent.putExtra(PhotoTableFragment.PHOTO_VIEWER, true);
                            intent.putExtra(PhotoTableFragment.PHOTO_ITEM_KEY, key);
                            intent.putExtra(com.nextgis.maplib.util.Constants.FIELD_ID, mFeatureId);
                            mActivity.startActivity(intent);
                        }
                    });

            //addOnSelectionChangedListener(viewHolder); // it is in super
        }
    }


    @Override
    public void setAttachItems(Map<String, AttachItem> attachItemMap)
    {
        super.setAttachItems(attachItemMap);

        for (Map.Entry<String, AttachItem> entry : mAttachItemList) {
            if (entry.getValue()
                    .getDescription()
                    .equals(com.nextgis.forestinspector.util.Constants.SIGN_DESCRIPTION)) {

                mAttachItemList.remove(entry);
                break;
            }
        }
    }


    @Override
    protected InputStream getPhotoInputStream(int position)
            throws IOException
    {
        String attachId = mAttachItemList.get(position).getValue().getAttachId();
        Uri attachUri = ContentUris.withAppendedId(mAttachesUri, Integer.valueOf(attachId));

        InputStream inputStream;
        try {
            inputStream = mActivity.getContentResolver().openInputStream(attachUri);

        } catch (FileNotFoundException e) {
            Log.d(
                    Constants.TAG, "PhotoTableCursorAdapter, position = " + position + ", ERROR: " +
                            e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }

        Log.d(
                Constants.TAG, "PhotoTableCursorAdapter, position = " + position + ", URI = " +
                        attachUri.toString());
        return inputStream;
    }


    @Override
    protected void deleteSelected(int id)
            throws IOException
    {
        AttachItem item = mAttachItemList.get(id).getValue();
        long attachId = Long.parseLong(item.getAttachId());

        if (mDocsLayer.deleteTempAttach(mFeatureId, attachId) <= 0) {
            String error = "PhotoTableCursorAdapter, deleteSelected(), deleteTempAttach() is fail";
            Log.d(Constants.TAG, error);
            throw new IOException(error);
        }

        super.deleteSelected(id);
    }
}
