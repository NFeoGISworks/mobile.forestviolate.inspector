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

import android.support.v4.app.Fragment;
import android.util.Log;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.maplib.util.AttachItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.nextgis.maplib.util.Constants.TAG;


public class PhotoTableFileAdapter
        extends PhotoTableAdapter
{
    protected File mAttachDir;


    public PhotoTableFileAdapter(
            Fragment parentFragment,
            long featureId,
            Map<String, AttachItem> attachItemMap,
            boolean isDocumentViewer,
            boolean isOnePhotoViewer)
    {
        super(parentFragment, featureId, attachItemMap, isDocumentViewer, isOnePhotoViewer);

        MainApplication app =
                (MainApplication) parentFragment.getActivity().getApplicationContext();
        mAttachDir = app.getDocFeatureFolder();
    }


    @Override
    protected InputStream getPhotoInputStream(int position)
            throws IOException
    {
        File photoFile = getAttachFile((int) getItemId(position));
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(photoFile);

        } catch (FileNotFoundException e) {
            String error = "PhotoTableFileAdapter, getPhotoInputStream(), position = " + position +
                           ", ERROR: " + e.getLocalizedMessage();
            Log.d(TAG, error);
            throw new IOException(error);
        }

        Log.d(
                TAG, "PhotoTableFileAdapter, getPhotoInputStream(), position = " + position +
                     ", photoFile = " + photoFile.getAbsolutePath());
        return inputStream;
    }


    @Override
    protected void deleteSelected(int id)
            throws IOException
    {
        File attachFile = getAttachFile(id);
        if (!attachFile.delete()) {
            String error = "PhotoTableFileAdapter, can not delete the file: " +
                           attachFile.getAbsolutePath();
            Log.d(TAG, error);
            throw new IOException(error);
        }
        super.deleteSelected(id);
    }


    protected File getAttachFile(int id)
            throws IOException
    {
        AttachItem item = mAttachItemList.get(id).getValue();

        if (null == item) {
            String error = "PhotoTableFileAdapter, getAttachFile(), null == item";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        String attachDisplayName = item.getDisplayName();
        return new File(mAttachDir, attachDisplayName);
    }
}
