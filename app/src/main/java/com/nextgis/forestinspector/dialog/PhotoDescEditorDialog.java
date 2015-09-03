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

package com.nextgis.forestinspector.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import com.nextgis.forestinspector.R;


public class PhotoDescEditorDialog
        extends YesNoDialog
{
    protected EditText mEditor;
    protected String mPhotoDesc;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View view = View.inflate(getActivity(), R.layout.dialog_photo_description, null);
        mEditor = (EditText) view.findViewById(R.id.photo_desc_editor);
        mEditor.setText(mPhotoDesc);

        setIcon(R.drawable.ic_action_image_edit);
        setTitle(R.string.photo_desc);
        setView(view);
        setPositiveText(R.string.ok);

        return super.onCreateDialog(savedInstanceState);
    }


    public void setPhotoDesc(String photoDesc)
    {
        mPhotoDesc = photoDesc;
    }


    public String getText()
    {
        return mEditor.getText().toString();
    }
}
