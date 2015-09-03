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
import com.nextgis.forestinspector.activity.PhotoTableActivity;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PhotoTableCursorAdapter
        extends PhotoTableAdapter
{
    protected long mFeatureId;
    protected Uri  mAttachesUri;


    public PhotoTableCursorAdapter(
            AppCompatActivity activity,
            long featureId,
            Map<String, AttachItem> attachItemMap,
            Uri attachesUri,
            boolean isPhotoViewer)
    {
        super(activity, attachItemMap, isPhotoViewer);

        mFeatureId = featureId;
        mAttachesUri = attachesUri;
    }


    @Override
    public void onBindViewHolder(
            final ViewHolder viewHolder,
            final int position)
    {
        super.onBindViewHolder(viewHolder, position);

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

                            Intent intent = new Intent(mActivity, PhotoTableActivity.class);
                            intent.putExtra("photo_viewer", true);
                            intent.putExtra("photo_item_key", key);
                            intent.putExtra("feature_id", mFeatureId);
                            mActivity.startActivity(intent);
                        }
                    });

            //addListener(viewHolder); // it is in super
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
}
