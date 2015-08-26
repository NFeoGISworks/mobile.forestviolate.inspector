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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import com.nextgis.forestinspector.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static com.nextgis.maplib.util.Constants.TAG;


public class PhotoTableAdapter
        extends RecyclerView.Adapter<PhotoTableAdapter.ViewHolder>
{
    protected final static int CREATE_PREVIEW_DONE   = 0;
    protected final static int CREATE_PREVIEW_OK     = 1;
    protected final static int CREATE_PREVIEW_FAILED = 2;

    protected final int IMAGE_SIZE_PX;

    protected Context         mContext;
    protected List<PhotoItem> mPhotoItems;


    private ViewHolder.OnCheckedChangeListener mOnCheckedChangeListener;


    public PhotoTableAdapter(
            Context context,
            List<PhotoItem> photoItems,
            int imageSizePx,
            ViewHolder.OnCheckedChangeListener onCheckedChangeListener)
    {
        mContext = context;
        mPhotoItems = photoItems;
        IMAGE_SIZE_PX = imageSizePx;
        mOnCheckedChangeListener = onCheckedChangeListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_table, parent, false);

        return new ViewHolder(view, mOnCheckedChangeListener);
    }


    @Override
    public void onBindViewHolder(
            final ViewHolder viewHolder,
            final int position)
    {
        ViewGroup.LayoutParams layoutParams = viewHolder.mImageView.getLayoutParams();
        layoutParams.height = IMAGE_SIZE_PX;
        layoutParams.width = IMAGE_SIZE_PX;

        viewHolder.mPosition = position;

        viewHolder.mCheckBox.setChecked(mPhotoItems.get(position).mIsChecked);
        viewHolder.mCheckBox.setTag(position);
        viewHolder.mCheckBox.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        CheckBox checkBox = (CheckBox) view;
                        int clickedPos = (Integer) checkBox.getTag();
                        mPhotoItems.get(clickedPos).mIsChecked = checkBox.isChecked();

                        if (viewHolder.mListener != null) {
                            viewHolder.mListener.onCheckedChange(position);
                        }
                    }
                });

        viewHolder.mImageView.setLayoutParams(layoutParams);
        viewHolder.mImageView.setImageBitmap(null);

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
                                mContext, "onBindViewHolder() ERROR: " + msg.obj, Toast.LENGTH_LONG)
                                .show();
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
                Message msg = handler.obtainMessage(CREATE_PREVIEW_FAILED, t.getLocalizedMessage());
                msg.sendToTarget();
            }
        };

        new Thread(future).start();
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mPhotoItems) {
            Log.d(TAG, "getItemId(), null == mPhotoFiles");
            return super.getItemId(position);
        }

        return position;
    }


    @Override
    public int getItemCount()
    {
        if (null == mPhotoItems) {
            Log.d(TAG, "getItemCount(), null == mPhotoFiles");
            return 0;
        }

        return mPhotoItems.size();
    }


    public void clearSelection() {
        for (int i = 0, size = mPhotoItems.size(); i < size; ++i) {
            mPhotoItems.get(i).mIsChecked = false;
            notifyItemChanged(i);
        }
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

            int targetW = IMAGE_SIZE_PX;
            int targetH = IMAGE_SIZE_PX;

            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap small = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bmOptions);
            int smallW = small.getWidth();
            int smallH = small.getHeight();

            if (smallW >= small.getHeight()) {
                result = Bitmap.createBitmap(small, (smallW - smallH) / 2, 0, smallH, smallH);
            } else {
                result = Bitmap.createBitmap(small, 0, (smallH - smallW) / 2, smallW, smallW);
            }


        } catch (IOException e) {
            String error = "ObjectPhotoAdapter ERROR: " + e.getLocalizedMessage();
            Log.d(TAG, error);
            e.printStackTrace();
            throw new IOException(error);
        }

        if (null == result) {
            String error = "ObjectPhotoAdapter ERROR: null == result";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        return result;
    }


    protected InputStream getPhotoInputStream(int position)
            throws IOException
    {
        if (null == mPhotoItems) {
            String error = "ObjectPhotoFileAdapter, getPhotoInputStream(), mPhotoFiles == null";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        long itemId = getItemId(position);

        if (RecyclerView.NO_ID == itemId) {
            String error =
                    "ObjectPhotoFileAdapter, getPhotoInputStream(), RecyclerView.NO_ID == itemId";
            Log.d(TAG, error);
            throw new IOException(error);
        }

        File photoFile = mPhotoItems.get((int) itemId).mFile;
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(photoFile);

        } catch (FileNotFoundException e) {
            String error = "ObjectPhotoFileAdapter, getPhotoInputStream(), position = " + position +
                           ", ERROR: " + e.getLocalizedMessage();
            Log.d(TAG, error);
            throw new IOException(error);
        }

        Log.d(
                TAG, "ObjectPhotoFileAdapter, getPhotoInputStream(), position = " + position +
                     ", photoFile = " + photoFile.getAbsolutePath());
        return inputStream;
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public int       mPosition;
        public ImageView mImageView;
        public CheckBox  mCheckBox;

        public OnCheckedChangeListener mListener;


        public ViewHolder(
                View itemView,
                OnCheckedChangeListener listener)
        {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.photo_table_item);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.photo_checkbox);
            mListener = listener;
        }


        public interface OnCheckedChangeListener
        {
            void onCheckedChange(int position);
        }
    }
}
