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

package com.nextgis.forestinspector.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.map.DocumentsLayer;
import com.nextgis.forestinspector.util.Constants;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplibui.control.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Form of indictment
 */
public class IndictmentActivity extends FIActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_indictment);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sUserDesc = prefs.getString(SettingsConstants.KEY_PREF_USERDESC, "");
        String sUserPassId = prefs.getString(SettingsConstants.KEY_PREF_USERPASSID, "");

        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.indictment));

        EditText indictmentNumber = (EditText) findViewById(R.id.indictment_num);
        indictmentNumber.setText(getNewNumber(sUserPassId));

        EditText author = (EditText) findViewById(R.id.author);

        author.setText(sUserDesc + getString(R.string.passid_is) + " " + sUserPassId);

        DateTime datetime = (DateTime)findViewById(R.id.create_datetime);
        datetime.init(null, null);
        datetime.setCurrentDate();

        MapBase map = MapBase.getInstance();
        DocumentsLayer docs = null;
        for(int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof DocumentsLayer) {
                docs = (DocumentsLayer) layer;
                break;
            }
        }

        if(docs != null) {
            NGWLookupTable violationTypeTable = (NGWLookupTable) docs.getLayerByName(Constants.KEY_LAYER_VIOLATE_TYPES);
            if (null != violationTypeTable) {
                Map<String, String> data = violationTypeTable.getData();
                List<String> violationTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    violationTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        violationTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner violationTypeSpinner = (Spinner) findViewById(R.id.violation_type);
                violationTypeSpinner.setAdapter(adapter);
            }

            NGWLookupTable forestCatTypeTable = (NGWLookupTable) docs.getLayerByName(Constants.KEY_LAYER_FOREST_CAT_TYPES);
            if (null != forestCatTypeTable) {
                Map<String, String> data = forestCatTypeTable.getData();
                List<String> violationTypeArray = new ArrayList<>();

                for (Map.Entry<String, String> entry : data.entrySet()) {
                    violationTypeArray.add(entry.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                        violationTypeArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner forestCatTypeSpinner = (Spinner) findViewById(R.id.forest_cat_type);
                forestCatTypeSpinner.setAdapter(adapter);
            }
        }

    }

    protected String getNewNumber(String passId){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;

        return passId + "/" + month + "-" + year;
    }
}
