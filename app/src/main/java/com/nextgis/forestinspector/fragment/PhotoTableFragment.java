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

package com.nextgis.forestinspector.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.PhotoTableAdapter;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.maplib.util.AttachItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.nextgis.maplib.util.Constants.TAG;


public class PhotoTableFragment
        extends Fragment
        implements PhotoTableAdapter.OnSelectionChangedListener, ActionMode.Callback
{
    protected static final int MIN_IMAGE_SIZE_DP      = 130;
    protected static final int CARD_VIEW_MARGIN_DP    = 8;
    protected static final int CARD_ELEVATION_DP      = 2;
    protected static final int CONTENT_PADDING_DP     = 2;
    protected static final int MAX_ITEM_COUNT_V       = 4;
    protected static final int MAX_ITEM_COUNT_H       = 7;
    protected static final int PHOTO_SPACES           =
            CONTENT_PADDING_DP * 2 + CARD_ELEVATION_DP * 2 + CARD_VIEW_MARGIN_DP;
    protected static final int CARD_VIEW_MIN_WIDTH_DP = MIN_IMAGE_SIZE_DP + PHOTO_SPACES;

    protected static final int REQUEST_TAKE_PHOTO = 1;

    protected RecyclerView         mPhotoTable;
    protected PhotoTableAdapter    mPhotoTableAdapter;
    protected FloatingActionButton mCameraBtn;

    protected String mTempPhotoPath = null;
    protected DocumentEditFeature mTempFeature;

    private ActionMode mActionMode;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        MainApplication app = (MainApplication) getActivity().getApplication();
        mTempFeature = app.getTempFeature();
        mPhotoTableAdapter = new PhotoTableAdapter(
                (AppCompatActivity) getActivity(), mTempFeature.getAttachments());
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        int orientation = getResources().getConfiguration().orientation;
        int maxItemCount;

        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
            default:
                maxItemCount = MAX_ITEM_COUNT_V;
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                maxItemCount = MAX_ITEM_COUNT_H;
                break;
        }

        float density = getResources().getDisplayMetrics().density;
        int widthPX = getResources().getDisplayMetrics().widthPixels;
        int widthDP = (int) (widthPX / density);
        int widthRestDP = widthDP - CARD_VIEW_MARGIN_DP;
        int minItemCount = widthRestDP / CARD_VIEW_MIN_WIDTH_DP;

        int realPhotoCount = minItemCount > maxItemCount ? maxItemCount : minItemCount;
        int cardViewRealWidthDP = (widthRestDP + CARD_VIEW_MARGIN_DP) / realPhotoCount;
        int photoRealWidthDp = cardViewRealWidthDP - PHOTO_SPACES;
        int photoRealWidthPX = (int) (photoRealWidthDp * density);


        View view = inflater.inflate(R.layout.fragment_photo_table, null);
        mPhotoTable = (RecyclerView) view.findViewById(R.id.photo_table_rv);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getActivity(), realPhotoCount, GridLayoutManager.VERTICAL, false);

        mPhotoTableAdapter.setImageSizePx(photoRealWidthPX);
        mPhotoTableAdapter.addListener(this);

        mPhotoTable.setLayoutManager(layoutManager);
        mPhotoTable.setAdapter(mPhotoTableAdapter);
        mPhotoTable.setHasFixedSize(true);


        mCameraBtn = (FloatingActionButton) view.findViewById(R.id.camera_btn);
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

        return view;
    }


    @Override
    public void onDestroyView()
    {
        if (null != mPhotoTableAdapter) {
            mPhotoTableAdapter.removeListener(this);
        }

        super.onDestroyView();
    }


    protected void showCameraActivity()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (null != cameraIntent.resolveActivity(getActivity().getPackageManager())) {

            try {
                MainApplication app = (MainApplication) getActivity().getApplication();
                File photoDir = app.getDocFeatureFolder();
                String timeStamp =
                        new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
                File tempFile = new File(photoDir, timeStamp + ".jpg");

                if (!tempFile.exists() && tempFile.createNewFile() ||
                    tempFile.exists() && tempFile.delete() &&
                    tempFile.createNewFile()) {

                    mTempPhotoPath = tempFile.getAbsolutePath();
                    Log.d(TAG, "mTempPhotoPath: " + mTempPhotoPath);

                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
                }

            } catch (IOException e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
            OnPhotoTaked(tempPhotoFile);
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


    protected void OnPhotoTaked(File tempPhotoFile)
    {
        AttachItem photoAttach = new AttachItem("-1", tempPhotoFile.getName(), "image/jpeg", "");
        mTempFeature.addAttachment(photoAttach);

        if (null != mPhotoTableAdapter) {
            mPhotoTableAdapter.setAttachItems(mTempFeature.getAttachments());
            mPhotoTableAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onSelectionChanged(
            int position,
            boolean selection)
    {
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
            mActionMode.setTitle("" + mPhotoTableAdapter.getSelectedItemCount());
            mCameraBtn.setVisibility(View.GONE);

        } else if (!mPhotoTableAdapter.isSelectedItems()) {
            mActionMode.setTitle("");
            mActionMode.finish();
            mCameraBtn.setVisibility(View.VISIBLE);

        } else {
            mActionMode.setTitle("" + mPhotoTableAdapter.getSelectedItemCount());
        }
    }


    @Override
    public boolean onCreateActionMode(
            ActionMode mode,
            Menu menu)
    {
        mode.getMenuInflater().inflate(R.menu.actionmode_photo_table, menu);
        return true;
    }


    @Override
    public boolean onPrepareActionMode(
            ActionMode mode,
            Menu menu)
    {
        return false;
    }


    @Override
    public boolean onActionItemClicked(
            ActionMode mode,
            MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.menu_delete:
                try {
                    mPhotoTableAdapter.deleteSelected();
                } catch (IOException e) {
                    String error = e.getLocalizedMessage();
                    Log.d(TAG, error);
                    e.printStackTrace();
                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                }
                mode.finish();
                return true;

            case R.id.menu_select_all:
                mPhotoTableAdapter.toggleSelection();
                return true;

            default:
                return false;
        }
    }


    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        mPhotoTableAdapter.clearSelection();
        mActionMode = null;
    }
}
