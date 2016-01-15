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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nextgis.forestinspector.MainApplication;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.adapter.PhotoTableAdapter;
import com.nextgis.forestinspector.adapter.PhotoTableCursorAdapter;
import com.nextgis.forestinspector.datasource.DocumentEditFeature;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.util.AttachItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.nextgis.maplib.util.Constants.TAG;


public class PhotoTableFragment
        extends TabFragment
        implements PhotoTableAdapter.OnSelectionChangedListener,
                   ActionMode.Callback,
                   PhotoTableAdapter.OnAttachChangingListener,
                   PhotoTableAdapter.OnDeleteSelectedListener
{
    public static final String PHOTO_ITEM_KEY  = "photo_item_key";
    public static final String PHOTO_VIEWER    = "photo_viewer";
    public static final String TEMP_PHOTO_PATH = "temp_photo_path";

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

    protected DocumentsLayer      mDocsLayer;
    protected DocumentEditFeature mEditFeature;

    protected RecyclerView         mPhotoTable;
    protected PhotoTableAdapter    mPhotoTableAdapter;
    protected FloatingActionButton mCameraBtn;

    protected String mTempPhotoPath = null;

    protected ActionMode mActionMode;

    protected boolean mIsOnePhotoViewer = false;
    protected boolean mIsDocumentViewer = false;


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (null != mTempPhotoPath) {
            outState.putString(TEMP_PHOTO_PATH, mTempPhotoPath);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState) {
            mTempPhotoPath = savedInstanceState.getString(TEMP_PHOTO_PATH);
        }

        if (null == getParentFragment()) {
            setRetainInstance(true);
        }

        Bundle extras = getActivity().getIntent().getExtras();
        if (null == extras || !extras.containsKey(com.nextgis.maplib.util.Constants.FIELD_ID)) {
            return;
        }

        long featureId = extras.getLong(com.nextgis.maplib.util.Constants.FIELD_ID);
        mIsOnePhotoViewer = extras.getBoolean(PHOTO_VIEWER);
        String photoItemKey = extras.getString(PHOTO_ITEM_KEY);
        mIsDocumentViewer = extras.getBoolean(Constants.DOCUMENT_VIEWER);


        AppCompatActivity activity = (AppCompatActivity) getActivity();
        MainApplication app = (MainApplication) activity.getApplication();
        mDocsLayer = app.getDocsLayer();

        Map<String, AttachItem> attaches;
        if (mIsDocumentViewer) {
            DocumentFeature feature = mDocsLayer.getFeatureWithAttaches(featureId);
            attaches = feature.getAttachments();
        } else {
            mEditFeature = app.getEditFeature(featureId);
            attaches = mEditFeature.getAttachments();
        }

        if (mIsOnePhotoViewer && !TextUtils.isEmpty(photoItemKey) && null != attaches) {
            Map<String, AttachItem> attachesTmp = attaches;
            attaches = new TreeMap<>();
            attaches.put(photoItemKey, attachesTmp.get(photoItemKey));
        }

        if (null == attaches) {
            String error = "PhotoTableFragment, onCreate(), null == attaches";
            Log.d(TAG, error);
            throw new RuntimeException(error);
        }

        mPhotoTableAdapter = new PhotoTableCursorAdapter(
                activity, featureId, attaches, mIsDocumentViewer, mIsOnePhotoViewer);
        mPhotoTableAdapter.setOnAttachChangingListener(this);
        mPhotoTableAdapter.setOnDeleteSelectedListener(this);
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

        int realPhotoCount;
        if (mIsOnePhotoViewer) {
            realPhotoCount = 1;
        } else {
            realPhotoCount = minItemCount > maxItemCount ? maxItemCount : minItemCount;
        }

        int cardViewRealWidthDP = (widthRestDP + CARD_VIEW_MARGIN_DP) / realPhotoCount;
        int photoRealWidthDp = cardViewRealWidthDP - PHOTO_SPACES;
        int photoRealWidthPX = (int) (photoRealWidthDp * density);


        if (mIsOnePhotoViewer) {
            getActivity().setTitle(R.string.view_photo);
        }


        View view = inflater.inflate(R.layout.fragment_photo_table, null);
        mPhotoTable = (RecyclerView) view.findViewById(R.id.photo_table_rv);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(
                getActivity(), realPhotoCount, GridLayoutManager.VERTICAL, false);

        mPhotoTableAdapter.setImageSizePx(photoRealWidthPX);
        mPhotoTableAdapter.addOnSelectionChangedListener(this);

        mPhotoTable.setLayoutManager(layoutManager);
        mPhotoTable.setAdapter(mPhotoTableAdapter);
        mPhotoTable.setHasFixedSize(true);


        mCameraBtn = (FloatingActionButton) view.findViewById(R.id.camera_btn);
        if (null != mCameraBtn) {

            if (mIsDocumentViewer || mIsOnePhotoViewer) {
                mCameraBtn.setVisibility(View.GONE);
            } else {
                mCameraBtn.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                if (mCameraBtn.isEnabled()) {
                                    onAdd();
                                }
                            }
                        });
            }
        }

        return view;
    }


    @Override
    public void onDestroyView()
    {
        if (null != mPhotoTableAdapter) {
            mPhotoTableAdapter.removeOnSelectionChangedListener(this);
        }

        super.onDestroyView();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        Integer clickedId = mPhotoTableAdapter.getClickedId();
        if (!mIsOnePhotoViewer && null != clickedId) {
            mPhotoTableAdapter.notifyItemChanged(clickedId);
        }
    }


    protected void onAdd()
    {
        showCameraActivity();
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

                if (!tempFile.exists() && tempFile.createNewFile()
                        || tempFile.exists() && tempFile.delete() &&
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
            onPhotoTook(tempPhotoFile);
        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_CANCELED) {
            if (tempPhotoFile.delete()) {
                Log.d(
                        TAG, "tempPhotoFile deleted on Activity.RESULT_CANCELED, path: "
                                + tempPhotoFile.getAbsolutePath());
            } else {
                Log.d(
                        TAG, "tempPhotoFile delete FAILED on Activity.RESULT_CANCELED, path: "
                                + tempPhotoFile.getAbsolutePath());
            }
        }
    }


    protected void onPhotoTook(File tempPhotoFile)
    {
        AttachItem photoAttach = mDocsLayer.getNewTempAttach(mEditFeature);

        if (null == photoAttach) {
            Log.d(TAG, "onPhotoTook(), photoAttach == null");
            return;
        }

        photoAttach.setDisplayName(tempPhotoFile.getName());
        photoAttach.setMimetype("image/jpeg");
        photoAttach.setDescription("");

        long featureId = mEditFeature.getId();
        long attachId = Long.parseLong(photoAttach.getAttachId());

        boolean res = mDocsLayer.insertAttachFile(featureId, attachId, tempPhotoFile);

        if (res) {
            res = mDocsLayer.updateAttachWithFlags(mEditFeature, photoAttach) > 0;
        }

        if (res && null != mPhotoTableAdapter) {
            mPhotoTableAdapter.setAttachItems(mEditFeature.getAttachments());
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

        } else if (!mPhotoTableAdapter.hasSelectedItems()) {
            mActionMode.setTitle("");
            mActionMode.finish();

        } else {
            mActionMode.setTitle("" + mPhotoTableAdapter.getSelectedItemCount());
        }
    }


    @Override
    public boolean onCreateActionMode(
            ActionMode mode,
            Menu menu)
    {
        mCameraBtn.setVisibility(View.GONE);
        mode.getMenuInflater().inflate(R.menu.list_actionmode, menu);
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
    public void onDestroyActionMode(ActionMode mode)
    {
        mPhotoTableAdapter.clearSelectionForAll();
        mActionMode = null;

        if (!mIsDocumentViewer) {
            mCameraBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onActionItemClicked(
            final ActionMode mode,
            MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {

            case R.id.menu_delete:

                Snackbar undoBar = Snackbar.make(
                        getView(), R.string.photos_will_be_deleted, Snackbar.LENGTH_LONG).setAction(
                        R.string.cancel, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                // cancel
                            }
                        }).setCallback(
                        new Snackbar.Callback()
                        {
                            @Override
                            public void onDismissed(
                                    Snackbar snackbar,
                                    int event)
                            {
                                switch (event) {
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        break;

                                    case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                                    case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                                    case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                                    case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                    default:
                                        try {
                                            mPhotoTableAdapter.deleteAllSelected();
                                            mode.finish();
                                        } catch (IOException e) {
                                            String error = e.getLocalizedMessage();
                                            Log.d(TAG, error);
                                            e.printStackTrace();
                                            Toast.makeText(
                                                    getActivity(), error, Toast.LENGTH_LONG).show();
                                        }
                                        break;

                                }

                                super.onDismissed(snackbar, event);
                            }
                        }).setActionTextColor(
                        getResources().getColor(R.color.color_undobar_action_text));

                View undoBarView = undoBar.getView();
                undoBarView.setBackgroundColor(
                        getResources().getColor(R.color.color_undobar_background));
                TextView tv = (TextView) undoBarView.findViewById(
                        android.support.design.R.id.snackbar_text);
                tv.setTextColor(getResources().getColor(R.color.color_undobar_text));

                undoBar.show();
                return true;

            case R.id.menu_select_all:
                mPhotoTableAdapter.toggleSelectionForAll();
                return true;

            default:
                return false;
        }
    }


    @Override
    public void onAttachChanging(AttachItem attachItem)
    {
        if (mDocsLayer.updateAttachWithFlags(mEditFeature, attachItem) <= 0) {
            Toast.makeText(getActivity(), getString(R.string.error_db_update), Toast.LENGTH_LONG)
                    .show();
        }
    }


    @Override
    public void onDeleteSelected(long attachId)
    {
        if (mDocsLayer.deleteAttachWithFlags(mEditFeature.getId(), attachId) <= 0) {
            Toast.makeText(getActivity(), getString(R.string.error_db_delete), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
