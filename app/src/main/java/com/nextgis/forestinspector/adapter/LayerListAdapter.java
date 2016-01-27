/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:  NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (c) 2015-2016. NextGIS, info@nextgis.com
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

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.MapEventListener;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.Layer;
import com.nextgis.maplib.map.MapDrawable;

import static com.nextgis.maplib.util.Constants.NOT_FOUND;


public class LayerListAdapter
        extends RecyclerView.Adapter<LayerListAdapter.ViewHolder>
        implements MapEventListener
{
    protected Activity    mActivity;
    protected MapDrawable mMap;

    protected Drawable mVisibilityOn;
    protected Drawable mVisibilityOff;


    public LayerListAdapter(
            Activity activity,
            @NonNull
            MapDrawable map)
    {
        mActivity = activity;
        mMap = map;

        int[] attrs = new int[] {
                R.attr.ic_action_visibility_on, R.attr.ic_action_visibility_off};
        TypedArray ta = mActivity.obtainStyledAttributes(attrs);
        mVisibilityOn = ta.getDrawable(0);
        mVisibilityOff = ta.getDrawable(1);
        ta.recycle();

        mMap.addListener(this);
    }


    @Override
    protected void finalize()
            throws Throwable
    {
        if (null != mMap) {
            mMap.removeListener(this);
        }
        super.finalize();
    }


    @Override
    final public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType)
    {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.item_layer, parent, false);
        return new LayerListAdapter.ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(
            final ViewHolder holder,
            final int position)
    {
        holder.mLayerName.setText(getItem(position).getName());

        Layer layer = (Layer) getItem(position);
        holder.mVisibility.setImageDrawable(layer.isVisible() ? mVisibilityOn : mVisibilityOff);

        holder.mVisibility.setTag(position);
        holder.mVisibility.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ImageButton button = (ImageButton) view;
                        int clickedPos = (Integer) button.getTag();
                        Layer lr = (Layer) getItem(clickedPos);
                        lr.setVisible(!lr.isVisible());
                        lr.save();
                    }
                });
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public TextView    mLayerName;
        public ImageButton mVisibility;


        public ViewHolder(View itemView)
        {
            super(itemView);

            mLayerName = (TextView) itemView.findViewById(R.id.layer_name);
            mVisibility = (ImageButton) itemView.findViewById(R.id.visibility);
        }
    }


    @Override
    public long getItemId(int position)
    {
        if (position < 0 || position >= mMap.getLayerCount()) {
            return NOT_FOUND;
        }

        ILayer layer = getItem(position);
        if (null != layer) {
            return layer.getId();
        }

        return NOT_FOUND;
    }


    @Override
    public int getItemCount()
    {
        return mMap.getLayerCount();
    }


    public ILayer getItem(int position)
    {
        int index = getItemCount() - 1 - position;
        return mMap.getLayer(index);
    }


    @Override
    public void onLayerAdded(int id)
    {
        notifyDataChanged();
    }


    @Override
    public void onLayerDeleted(int id)
    {
        notifyDataChanged();
    }


    @Override
    public void onLayerChanged(int id)
    {
        notifyDataChanged();
    }


    @Override
    public void onExtentChanged(
            float zoom,
            GeoPoint center)
    {

    }


    @Override
    public void onLayersReordered()
    {
        notifyDataChanged();
    }


    @Override
    public void onLayerDrawFinished(
            int id,
            float percent)
    {

    }


    @Override
    public void onLayerDrawStarted()
    {

    }


    protected void notifyDataChanged()
    {
        mActivity.runOnUiThread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        notifyDataSetChanged();
                    }
                });
    }
}
