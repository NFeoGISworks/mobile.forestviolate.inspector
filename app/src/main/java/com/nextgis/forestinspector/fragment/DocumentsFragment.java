/*
 * Project: Forest violations
 * Purpose: Mobile application for registering facts of the forest violations.
 * Author:  Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.activity.MainActivity;
import com.nextgis.forestinspector.adapter.DocumentsListAdapter;

/**
 * Documents and notes list fragment
 */
public class DocumentsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        DocumentsListAdapter adapter = new DocumentsListAdapter(getActivity());

        ListView list = (ListView) rootView.findViewById(R.id.documentsList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter);

        final View addIndictment = rootView.findViewById(R.id.add_indictment);
        if (null != addIndictment) {
            addIndictment.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MainActivity activity = (MainActivity)getActivity();
                            activity.addIndictment();
                        }
                    });
        }

        final View addSheet = rootView.findViewById(R.id.add_sheet);
        if (null != addSheet) {
            addSheet.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity activity = (MainActivity) getActivity();
                            activity.addSheet();
                        }
                    });
        }

        final View addBookmark = rootView.findViewById(R.id.add_bookmark);
        if (null != addBookmark) {
            addBookmark.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity activity = (MainActivity) getActivity();
                            activity.addBookmark();
                        }
                    });
        }

        return rootView;
    }
}
