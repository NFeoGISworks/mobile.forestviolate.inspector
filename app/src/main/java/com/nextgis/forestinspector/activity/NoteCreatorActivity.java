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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.dialog.NoteListFillerDialog;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;

import static com.nextgis.maplib.util.Constants.FIELD_ID;


/**
 * Form of the note
 */
public class NoteCreatorActivity
        extends FIActivity
{
    protected OnCreateNoteListener mOnCreateNoteListener;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MapBase map = MapBase.getInstance();
        VectorLayer notesLayer = null;

        for (int i = 0; i < map.getLayerCount(); ++i) {
            ILayer layer = map.getLayer(i);

            if (layer.getName().equals(getString(R.string.notes))) {
                notesLayer = (VectorLayer) layer;
                break;
            }
        }

        if (null == notesLayer) {
            setContentView(R.layout.activity_document_noview);
            setToolbar(R.id.main_toolbar);
            return;
        }

        Feature feature = null;
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            long featureId = extras.getLong(FIELD_ID);

            Cursor cur = notesLayer.query(null, FIELD_ID + " = " + featureId, null, null, null);
            if (null == cur) {
                setContentView(R.layout.activity_document_noview);
                setToolbar(R.id.main_toolbar);
                return;
            }
            cur.moveToFirst();

            feature = new Feature(featureId, notesLayer.getFields());
            feature.fromCursor(cur);

            cur.close();
        }


        setContentView(R.layout.activity_note_creator);

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.note_title));

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        NoteListFillerDialog fragment =
                (NoteListFillerDialog) fm.findFragmentByTag(Constants.FRAGMENT_NOTE_FILLER);

        if (fragment == null) {
            fragment = new NoteListFillerDialog();
            fragment.setShowsDialog(false);
            fragment.setFeature(feature);
            mOnCreateNoteListener = fragment;
        }

        ft.replace(R.id.fragment, fragment, Constants.FRAGMENT_NOTE_FILLER);
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.note_creator, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_create_note:
                createNote();
                return true;
            case R.id.action_cancel:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void createNote()
    {
        if (null != mOnCreateNoteListener) {
            mOnCreateNoteListener.onCreateNote();
        }

        finish();
    }


    public interface OnCreateNoteListener
    {
        void onCreateNote();
    }
}
