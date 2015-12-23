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
import android.database.Cursor;
import android.database.DataSetObserver;


// http://stackoverflow.com/a/26673645/4727406
// https://gist.github.com/skyfishjy/443b7448f59be978bc59
// Here is our own implementation of the CursorAdapter's methods by just copy pasting code from CursorAdapter.
// Also see a docs to the methods from CursorAdapter.
public abstract class ListSelectorCursorAdapter
        extends ListSelectorAdapter
{
    protected Cursor mCursor;

    protected boolean mDataValid;
    protected int     mRowIDColumn;

    protected DataSetObserver mDataSetObserver;


    public ListSelectorCursorAdapter(
            Context context,
            Cursor cursor)
    {
        super(context);

        mDataSetObserver = new NotifyingDataSetObserver();

        initCursor(cursor);
    }


    protected void initCursor(Cursor cursor)
    {
        mCursor = cursor;
        if (cursor != null) {
            if (mDataSetObserver != null) {
                cursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = cursor.getColumnIndexOrThrow(com.nextgis.maplib.util.Constants.FIELD_ID);
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }
    }


    /**
     * Swap in a new Cursor, returning the old Cursor. Unlike {@link #changeCursor(Cursor)}, the
     * returned old Cursor is <em>not</em> closed.
     */
    public Cursor swapCursor(Cursor newCursor)
    {
        if (newCursor == mCursor) {
            return null;
        }

        clearSelectionForAll();

        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }

        initCursor(newCursor);
        notifyDataSetChanged();

        return oldCursor;
    }


    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor)
    {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }


    /**
     * See {@link #onBindViewHolder(ViewHolder, int)} for additional docs.
     *
     * @param holder
     *         The ViewHolder which should be updated to represent the contents of the item at the
     *         given position in the data set.
     * @param cursor
     *         The cursor from which to get the data. The cursor is already moved to the correct
     *         position.
     */
    public abstract void onBindViewHolder(
            ListSelectorAdapter.ViewHolder holder,
            Cursor cursor);


    @Override
    final public void onBindViewHolder(
            final ListSelectorAdapter.ViewHolder holder,
            final int position)
    {
        if (!mDataValid) {
            throw new IllegalStateException("This should only be called when the cursor is valid");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }

        super.onBindViewHolder(holder, position);

        onBindViewHolder(holder, mCursor);
    }


    public Cursor getCursor()
    {
        return mCursor;
    }


    @Override
    public int getItemCount()
    {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }


    @Override
    public long getItemId(int position)
    {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        } else {
            return super.getItemId(position);
        }
    }


    public Cursor getItem(int position)
    {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor;
        } else {
            return null;
        }
    }


    @Override
    public void setHasStableIds(boolean hasStableIds)
    {
        super.setHasStableIds(true);
    }


    private class NotifyingDataSetObserver
            extends DataSetObserver
    {
        @Override
        public void onChanged()
        {
            mDataValid = true;
            clearSelectionForAll();
            notifyDataSetChanged();
        }


        @Override
        public void onInvalidated()
        {
            mDataValid = false;
            clearSelectionForAll();
            notifyDataSetChanged();
            // There is not notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
