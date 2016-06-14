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
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplib.util.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class PhotoTableCursorAdapter
        extends PhotoTableAdapter
{
    protected Uri mAttachesUri;


    public PhotoTableCursorAdapter(
            Fragment parentFragment,
            long featureId,
            Map<String, AttachItem> attachItemMap,
            boolean isDocumentViewer,
            boolean isOnePhotoViewer)
    {
        super(parentFragment, featureId, attachItemMap, isDocumentViewer, isOnePhotoViewer);

        MainApplication app = (MainApplication) parentFragment.getActivity().getApplication();
        String docsLayerPathName = app.getDocsLayer().getPath().getName();

        mAttachesUri = Uri.parse(
                "content://" + app.getAuthority() + "/" + docsLayerPathName + "/" + featureId + "/"
                        + Constants.URI_ATTACH);
    }


    @Override
    protected InputStream getPhotoInputStream(int position)
            throws IOException
    {
        String attachId = mAttachItemList.get(position).getValue().getAttachId();
        Uri attachUri = ContentUris.withAppendedId(mAttachesUri, Integer.valueOf(attachId));

        InputStream inputStream;
        try {
            inputStream = mFragment.getActivity().getContentResolver().openInputStream(attachUri);

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
