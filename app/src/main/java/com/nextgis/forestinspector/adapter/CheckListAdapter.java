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
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.nextgis.forestinspector.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public abstract class CheckListAdapter
        extends RecyclerView.Adapter<CheckListAdapter.ViewHolder>
{
    protected SparseBooleanArray mSelectedItems;
    protected boolean mSelectState  = false;
    protected boolean mHideCheckBox = false;

    protected Queue<OnSelectionChangedListener> mListeners;


    protected abstract int getItemViewResId();

    protected abstract CheckListAdapter.ViewHolder getViewHolder(View itemView);


    public CheckListAdapter()
    {
        mListeners = new ConcurrentLinkedQueue<>();
        mSelectedItems = new SparseBooleanArray();
    }


    @Override
    final public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                getItemViewResId(), parent, false);
        return getViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(
            final ViewHolder holder,
            final int position)
    {
        holder.mPosition = position;

        if (null != holder.mCheckBox) {
            holder.mCheckBox.setChecked(isSelected(position));

            if (mHideCheckBox) {
                holder.mCheckBox.setVisibility(View.GONE);
            } else {
                holder.mCheckBox.setTag(position);
                holder.mCheckBox.setOnClickListener(
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                CheckBox checkBox = (CheckBox) view;
                                int clickedPos = (Integer) checkBox.getTag();
                                setSelection(clickedPos, checkBox.isChecked());
                            }
                        });
            }
        }

        addListener(holder);
    }


    @Override
    public void onViewRecycled(ViewHolder holder)
    {
        removeListener(holder);
        super.onViewRecycled(holder);
    }


    public static abstract class ViewHolder
            extends RecyclerView.ViewHolder
            implements CheckListAdapter.OnSelectionChangedListener
    {
        public int      mPosition;
        public CheckBox mCheckBox;


        public ViewHolder(View itemView)
        {
            super(itemView);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
        }


        @Override
        public void onSelectionChanged(
                int position,
                boolean selection)
        {
            if (null != mCheckBox && position == getAdapterPosition()) {
                mCheckBox.setChecked(selection);
            }
        }
    }


    /**
     * Count the selected items
     *
     * @return Selected items count
     */
    public int getSelectedItemCount()
    {
        return mSelectedItems.size();
    }


    public boolean hasSelectedItems()
    {
        return getSelectedItemCount() > 0;
    }


    /**
     * Indicates if the item at position position is selected
     *
     * @param position
     *         Position of the item to check
     *
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position)
    {
        return mSelectedItems.get(position, false);
    }


    /**
     * Clear the selection status for all items
     */
    public void clearSelectionForAll()
    {
        mSelectState = false;
        setSelectionForAll(false);
    }


    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position
     *         Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position)
    {
        setSelection(position, mSelectedItems.get(position, false));
    }


    public void toggleSelectionForAll()
    {
        mSelectState = !mSelectState;
        setSelectionForAll(mSelectState);
    }


    /**
     * Set the selection status of the item at a given position to the given state
     *
     * @param position
     *         Position of the item to toggle the selection status for
     * @param selection
     *         State for the item at position
     */
    public void setSelection(
            int position,
            boolean selection)
    {
        if (selection) {
            mSelectedItems.put(position, true);
        } else {
            mSelectedItems.delete(position);
        }

        for (OnSelectionChangedListener listener : mListeners) {
            listener.onSelectionChanged(position, selection);
        }
    }


    public void setSelectionForAll(boolean selection)
    {
        for (int i = 0, size = getItemCount(); i < size; ++i) {
            if (selection != isSelected(i)) {
                setSelection(i, selection);
            }
        }
    }


    /**
     * Indicates the list of selected items
     *
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItemsIds()
    {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0, size = mSelectedItems.size(); i < size; ++i) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }


    protected void deleteSelected(int id)
            throws IOException
    {
        mSelectedItems.delete(id);
    }


    public void deleteAllSelected()
            throws IOException
    {
        int size = getItemCount();
        for (int i = size - 1; i >= 0; --i) {
            if (isSelected(i)) {
                deleteSelected(i);
                notifyItemRemoved(i);
            }
        }
        notifyItemRangeChanged(0, getItemCount());
    }


    public void addListener(OnSelectionChangedListener listener)
    {
        if (mListeners != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }


    public void removeListener(OnSelectionChangedListener listener)
    {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }


    public interface OnSelectionChangedListener
    {
        void onSelectionChanged(
                int position,
                boolean selection);
    }
}
