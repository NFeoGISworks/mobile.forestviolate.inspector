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

package com.nextgis.forestinspector.util;

/**
 * Constants
 */
public interface Constants {

    String FITAG = "forest inspector";

    String KEY_INSPECTORS = "inspectors";
    String KEY_DOCUMENTS = "docs";
    String KEY_SHEET = "sheet";
    String KET_PRODUCTION = "production";
    String KEY_NOTES = "notes";
    String KEY_TERRITORY = "territory";
    String KEY_VEHICLES = "vehicles";
    String KEY_CADASTRE = "cadastre";

    String KEY_LAYER_DOCUMENTS = "documents";
    String KEY_LAYER_SHEET = "sheet";
    String KEY_LAYER_PRODUCTION = "production";
    String KEY_LAYER_NOTES = "notes";
    String KEY_LAYER_TERRITORY = "territory";
    String KEY_LAYER_VEHICLES = "vehicles";
    String KEY_LAYER_CADASTRE = "cadastre";

    /**
     * inspectors keys
     */
    String KEY_INSPECTOR_USER = "user";
    String KEY_INSPECTOR_USER_DESC = "user_desc";
    String KEY_INSPECTOR_USER_PASS_ID = "pass_id";

    /**
     * notes keys
     */
    String KEY_NOTES_USERID = "user_id";

    /**
     * document types
     */
    int TYPE_DOCUMENT = 2;
    int TYPE_SHEET = 3;
    int TYPE_VEHICLE = 4;
    int TYPE_NOTE = 0;

    int STEP_STATE_WAIT = 0;
    int STEP_STATE_WORK = 1;
    int STEP_STATE_DONE = 2;
    int STEP_STATE_ERROR = 3;

    String FIELD_DOCUMENTS_TYPE = "type";
    String FIELD_DOCUMENTS_DATE = "date";
    String FIELD_DOCUMENTS_NUMBER = "number";
    String FIELD_DOCUMENTS_STATUS = "status";
    String FIELD_DOCUMENTS_VIOLATE = "violate";
    String FIELD_DOCUMENTS_PARENT_ID = "doc_id";

    String FIELD_NOTES_DATE_BEG = "date_beg";
    String FIELD_NOTES_DATE_END = "date_end";
    String FIELD_NOTES_DESCRIPTION = "descript";

    int DOWNLOAD_SEPARATE_THREADS = 10;

    int DOCUMENT_STATUS_SEND = 1;       //the document send but some connected tables - no
    int DOCUMENT_STATUS_CHECKING = 2;   //the moderator check document
    int DOCUMENT_STATUS_OK = 3;         //the document accepted
    int DOCUMENT_STATUS_WARNING = 4;    //the document denied
    int DOCUMENT_STATUS_DELETED = 5;    //the document deleted

    /**
     * layer types
     */
    int LAYERTYPE_DOCS = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 1);
}
