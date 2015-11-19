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

package com.nextgis.forestinspector.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.fragment.PhotoTableFragment;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;


public class PhotoTableFillerActivity
        extends FIActivity
        implements IDocumentFeatureSource
{
    protected boolean mIsPhotoViewer = false;

    protected String          mDocumentsLayerPathName;
    protected DocumentFeature mFeature;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            mIsPhotoViewer = extras.getBoolean("photo_viewer");
            long featureId = extras.getLong("feature_id", -1);

            if (mIsPhotoViewer && -1 != featureId) {
                // get document from id
                MapBase map = MapBase.getInstance();
                DocumentsLayer docs = null;
                for (int i = 0; i < map.getLayerCount(); i++) {
                    ILayer layer = map.getLayer(i);
                    if (layer instanceof DocumentsLayer) {
                        docs = (DocumentsLayer) layer;
                        break;
                    }
                }

                if (null == docs) {
                    setContentView(R.layout.activity_document_noview);
                    setToolbar(R.id.main_toolbar);
                    return;
                }

                mDocumentsLayerPathName = docs.getPath().getName();
                mFeature = docs.getFeature(featureId);
                if (null == mFeature) {
                    setContentView(R.layout.activity_document_noview);
                    setToolbar(R.id.main_toolbar);
                    return;
                }
            }
        }

        setContentView(R.layout.activity_photo_table_filler);
        setToolbar(R.id.main_toolbar);

        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        PhotoTableFragment photoTableFragment =
                (PhotoTableFragment) fm.findFragmentByTag(Constants.FRAGMENT_PHOTO_TABLE);

        if (photoTableFragment == null) {
            photoTableFragment = new PhotoTableFragment("", mDocumentsLayerPathName);
        }

        ft.replace(R.id.photo_table_fragment, photoTableFragment, Constants.FRAGMENT_PHOTO_TABLE);
        ft.commit();
    }


    @Override
    public DocumentFeature getFeature()
    {
        return mFeature;
    }
}
