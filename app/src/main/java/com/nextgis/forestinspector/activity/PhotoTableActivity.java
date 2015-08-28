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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.fragment.PhotoTableFragment;
import com.nextgis.forestinspector.util.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.nextgis.maplib.util.Constants.TAG;


public class PhotoTableActivity
        extends FIActivity
{
    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected FloatingActionButton mCameraBtn;
    protected OnPhotoTakedListener mOnPhotoTakedListener;
    protected String mTempPhotoPath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_table);
        setToolbar(R.id.main_toolbar);

        mCameraBtn = (FloatingActionButton) findViewById(R.id.action_camera);
        if (null != mCameraBtn) {
            mCameraBtn.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (mCameraBtn.isEnabled()) {
                                showCameraActivity();
                            }
                        }
                    });
        }

        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        PhotoTableFragment photoTableFragment =
                (PhotoTableFragment) fm.findFragmentByTag(Constants.FRAGMENT_PHOTO_TABLE);

        if (photoTableFragment == null) {
            photoTableFragment = new PhotoTableFragment();
            setOnNewPhotoTakedListener(photoTableFragment);
        }

        ft.replace(R.id.photo_table_fragment, photoTableFragment, Constants.FRAGMENT_PHOTO_TABLE);
        ft.addToBackStack(null);
        ft.commit();
    }


    protected void showCameraActivity()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (null != cameraIntent.resolveActivity(getPackageManager())) {

            try {
                MainApplication app = (MainApplication) getApplication();
                File photoDir = app.getDocFeatureFolder();
                String timeStamp =
                        new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
                File tempFile = new File(photoDir, timeStamp + ".jpg");

                if (!tempFile.exists() && tempFile.createNewFile() ||
                    tempFile.exists() && tempFile.delete() &&
                    tempFile.createNewFile()) {

                    mTempPhotoPath = tempFile.getAbsolutePath();
                    Log.d(TAG, "mTempPhotoPath: " + mTempPhotoPath);

                    cameraIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }

            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        File tempPhotoFile = new File(mTempPhotoPath);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (null != mOnPhotoTakedListener) {
                mOnPhotoTakedListener.OnPhotoTaked(tempPhotoFile);
            }
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            if (tempPhotoFile.delete()) {
                Log.d(
                        TAG, "tempPhotoFile deleted on Activity.RESULT_CANCELED, path: " +
                             tempPhotoFile.getAbsolutePath());
            } else {
                Log.d(
                        TAG, "tempPhotoFile delete FAILED on Activity.RESULT_CANCELED, path: " +
                             tempPhotoFile.getAbsolutePath());
            }
        }
    }


    public void setOnNewPhotoTakedListener(OnPhotoTakedListener onPhotoTakedListener)
    {
        mOnPhotoTakedListener = onPhotoTakedListener;
    }


    public interface OnPhotoTakedListener
    {
        void OnPhotoTaked(File tempPhotoFile);
    }
}
