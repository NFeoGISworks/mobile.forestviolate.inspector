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

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.PhotoTableFillerActivity;
import com.nextgis.forestinspector.dialog.PhotoDescEditorDialog;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.util.AttachItem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static com.nextgis.maplib.util.Constants.TAG;


public abstract class PhotoTableAdapter
        extends ListSelectorAdapter
{
    protected final static int CREATE_PREVIEW_DONE   = 0;
    protected final static int CREATE_PREVIEW_OK     = 1;
    protected final static int CREATE_PREVIEW_FAILED = 2;

    protected int mImageSizePx;

    protected AppCompatActivity mActivity;

    protected Map<String, AttachItem>             mAttachItemMap;
    protected List<Map.Entry<String, AttachItem>> mAttachItemList;

    protected boolean mIsPhotoViewer = false;
    protected Integer mClickedId;


    protected abstract InputStream getPhotoInputStream(int position)
            throws IOException;


    public PhotoTableAdapter(
            AppCompatActivity activity,
            Map<String, AttachItem> attachItemMap,
            boolean isPhotoViewer)
    {
        super();

        mActivity = activity;
        mIsPhotoViewer = isPhotoViewer;
        setAttachItems(attachItemMap);
    }


    public void setAttachItems(Map<String, AttachItem> attachItemMap)
    {
        mAttachItemMap = attachItemMap;
        if (null != mAttachItemList) {
            mAttachItemList.clear();
        }

        mAttachItemList = new ArrayList<>(mAttachItemMap.size());

        if (mAttachItemList.addAll(mAttachItemMap.entrySet())) {
            Collections.sort(
                    mAttachItemList, new Comparator<Map.Entry<String, AttachItem>>()
                    {
                        @Override
                        public int compare(
                                Map.Entry<String, AttachItem> lhs,
                                Map.Entry<String, AttachItem> rhs)
                        {
                            if (null == lhs && null == rhs) {
                                return 0;
                            }

                            if (null == lhs) {
                                return -1;
                            }

                            if (null == rhs) {
                                return 1;
                            }

                            AttachItem valueL = lhs.getValue();
                            AttachItem valueR = rhs.getValue();

                            if (null == valueL && null == valueR) {
                                return 0;
                            }

                            if (null == valueL) {
                                return -1;
                            }

                            if (null == valueR) {
                                return 1;
                            }

                            String displayNameL = valueL.getDisplayName();
                            String displayNameR = valueR.getDisplayName();

                            if (null == displayNameL && null == displayNameR) {
                                return 0;
                            }

                            if (null == displayNameL) {
                                return -1;
                            }

                            if (null == displayNameR) {
                                return 1;
                            }

                            return displayNameL.compareTo(displayNameR);
                        }
                    });
        }
    }


    public void setImageSizePx(int imageSizePx)
    {
        mImageSizePx = imageSizePx;
    }


    @Override
    protected int getItemViewResId()
    {
        return R.layout.item_photo_table;
    }


    @Override
    protected ListSelectorAdapter.ViewHolder getViewHolder(View itemView)
    {
        return new PhotoTableAdapter.ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(
            final ListSelectorAdapter.ViewHolder holder,
            final int position)
    {
        super.onBindViewHolder(holder, position);

        final PhotoTableAdapter.ViewHolder viewHolder = (PhotoTableAdapter.ViewHolder) holder;

        if (mIsPhotoViewer) {
            viewHolder.mCheckBox.setVisibility(View.GONE);

            viewHolder.mPhotoDesc.setEllipsize(null);
            viewHolder.mPhotoDesc.setMaxLines(999);

            // http://stackoverflow.com/a/9409391
            int[] attrs = new int[] { R.attr.photoDescBackgroundColor};
            TypedArray ta = mActivity.obtainStyledAttributes(attrs);
            int backgroundColor = ta.getColor(0, 0);
            ta.recycle();

            viewHolder.mPhotoDesc.setBackgroundColor(backgroundColor);
        }

        viewHolder.mPhotoDesc.setTag(position);

        try {
            viewHolder.mPhotoDesc.setText(getDescription(position));
        } catch (IOException e) {
            Toast.makeText(
                    mActivity, "onBindViewHolder() ERROR: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
        }

        viewHolder.mPhotoDesc.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        TextView descView = (TextView) view;
                        final int clickedPos = (Integer) descView.getTag();

                        final PhotoDescEditorDialog dialog = new PhotoDescEditorDialog();
                        dialog.setPhotoDesc(
                                mAttachItemList.get(clickedPos).getValue().getDescription());
                        dialog.setOnPositiveClickedListener(
                                new PhotoDescEditorDialog.OnPositiveClickedListener()
                                {
                                    @Override
                                    public void onPositiveClicked()
                                    {
                                        String descText = dialog.getText();
                                        mAttachItemList.get(clickedPos)
                                                .getValue()
                                                .setDescription(descText);
                                        notifyItemChanged(clickedPos);
                                    }
                                });
                        dialog.show(
                                mActivity.getSupportFragmentManager(),
                                Constants.FRAGMENT_PHOTO_DESC_EDITOR_DIALOG);

                    }
                });

        ViewGroup.LayoutParams layoutParams = viewHolder.mImageView.getLayoutParams();
        layoutParams.height = mImageSizePx;
        layoutParams.width = mImageSizePx;

        viewHolder.mImageView.setLayoutParams(layoutParams);
        viewHolder.mImageView.setImageBitmap(null);
        viewHolder.mImageView.setTag(position);

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
                            intent.putExtra("photo_viewer", true);
                            intent.putExtra("photo_item_key", key);
                            mActivity.startActivity(intent);
                        }
                    });
        }

        final Handler handler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case CREATE_PREVIEW_DONE:
                        break;

                    case CREATE_PREVIEW_OK:
                        if (viewHolder.mPosition == position) {
                            viewHolder.mImageView.setImageBitmap((Bitmap) msg.obj);
                        }
                        break;

                    case CREATE_PREVIEW_FAILED:
                        Toast.makeText(
                                mActivity, "onBindViewHolder() ERROR: " + msg.obj,
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        RunnableFuture<Bitmap> future = new FutureTask<Bitmap>(
                new Callable<Bitmap>()
                {
                    @Override
                    public Bitmap call()
                            throws Exception
                    {
                        InputStream attachInputStream = getPhotoInputStream(position);

                        if (null == attachInputStream) {
                            String error = "onBindViewHolder() ERROR: null == attachInputStream";
                            Log.d(TAG, error);
                            throw new IOException(error);
                        }

                        Bitmap bitmap = createImagePreview(attachInputStream);

                        try {
                            attachInputStream.close();
                        } catch (IOException e) {
                            String error = "onBindViewHolder() ERROR: " + e.getLocalizedMessage();
                            Log.d(TAG, error);
                            e.printStackTrace();
                            throw new IOException(error);
                        }

                        return bitmap;
                    }
                })
        {
            @Override
            protected void done()
            {
                super.done();
                handler.sendEmptyMessage(CREATE_PREVIEW_DONE);
            }


            @Override
            protected void set(Bitmap result)
            {
                super.set(result);
                Message msg = handler.obtainMessage(CREATE_PREVIEW_OK, result);
                msg.sendToTarget();
            }


            @Override
            protected void setException(Throwable t)
            {
                super.setException(t);

                String error = t.getLocalizedMessage();
                Log.d(TAG, error);
                t.printStackTrace();

                Message msg = handler.obtainMessage(CREATE_PREVIEW_FAILED, error);
                msg.sendToTarget();
            }
        };

        new Thread(future).start();
    }


    public static class ViewHolder
            extends ListSelectorAdapter.ViewHolder
    {
        public ImageView mImageView;
        public TextView  mPhotoDesc;


        public ViewHolder(View itemView)
        {
            super(itemView, null);

            mImageView = (ImageView) itemView.findViewById(R.id.photo_table_item);
            mPhotoDesc = (TextView) itemView.findViewById(R.id.photo_desc);
        }
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mAttachItemList) {
            Log.d(TAG, "getItemId(), null == mAttachItemList");
            return super.getItemId(position);
        }

        return position;
    }


    @Override
    public int getItemCount()
    {
        if (null == mAttachItemList) {
            Log.d(TAG, "getItemCount(), null == mAttachItemList");
            return 0;
        }

        return mAttachItemList.size();
    }


    protected void deleteSelected(int id)
            throws IOException
    {
        super.deleteSelected(id);

        String key = mAttachItemList.get(id).getKey();
        mAttachItemMap.remove(key);
        mAttachItemList.remove(id);
    }


    protected Bitmap createImagePreview(InputStream inputStream)
            throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap result = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buffer[] = new byte[10240];
            int len;
            while ((len = bis.read(buffer, 0, buffer.length)) > 0) {
                baos.write(buffer, 0, len);
            }
            bis.close();

            byte[] imageData = baos.toByteArray();
            baos.close();

            int targetW = mImageSizePx;
            int targetH = mImageSizePx;

            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap small = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);

            if (mIsPhotoViewer) {
                return small;
            }

            int smallW = small.getWidth();
            int smallH = small.getHeight();

            if (smallW >= small.getHeight()) {
                result = Bitmap.createBitmap(small, (smallW - smallH) / 2, 0, smallH, smallH);
            } else {
                result = Bitmap.createBitmap(small, 0, (smallH - smallW) / 2, smallW, smallW);
            }


        } catch (IOException e) {
            String error = "PhotoTableAdapter, ERROR: " + e.getLocalizedMessage();
            Log.d(TAG, error);
            e.printStackTrace();
            throw new IOException(error);
        }

        if (null == result) {
            String error = "PhotoTableAdapter, ERROR: null == result";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        return result;
    }


    protected String getDescription(int id)
            throws IOException
    {
        AttachItem item = mAttachItemList.get(id).getValue();

        if (null == item) {
            String error = "PhotoTableAdapter, getDescription(), null == item";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        return item.getDescription();
    }


    public Integer getClickedId()
    {
        Integer id = mClickedId;
        mClickedId = null;
        return id;
    }
}
