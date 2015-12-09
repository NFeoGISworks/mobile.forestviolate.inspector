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

import android.support.v7.widget.RecyclerView;
import com.nextgis.forestinspector.datasource.DocumentFeature;
import com.nextgis.maplib.datasource.Feature;

import java.io.IOException;
import java.util.List;


public abstract class ListFillerAdapter
        extends ListSelectorAdapter
{
    protected DocumentFeature mFeature;
    protected List<Feature>   mFeatures;


    protected abstract String getLayerName();


    public ListFillerAdapter(DocumentFeature feature)
    {
        super();
        mFeature = feature;
        mFeatures = mFeature.getSubFeatures(getLayerName());
        registerAdapterDataObserver(new CheckListDataObserver());
    }


    protected class CheckListDataObserver
            extends RecyclerView.AdapterDataObserver
    {
        @Override
        public void onChanged()
        {
            super.onChanged();
            mFeatures = mFeature.getSubFeatures(getLayerName());
        }
    }


    @Override
    public long getItemId(int position)
    {
        if (null == mFeatures) {
            return super.getItemId(position);
        }

        return position;
    }


    @Override
    public int getItemCount()
    {
        if (null == mFeatures) {
            return 0;
        }
        return mFeatures.size();
    }


    @Override
    public void deleteSelected(int id)
            throws IOException
    {
        super.deleteSelected(id);
        mFeatures.remove(id);
    }


    public Feature getFeature(int id)
    {
        return mFeatures.get(id);
    }
}
