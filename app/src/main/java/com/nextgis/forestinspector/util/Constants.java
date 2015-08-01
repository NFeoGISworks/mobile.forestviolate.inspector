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
    int MAX_DOCUMENTS = 100;

    String KEY_INSPECTORS = "inspectors";
    String KEY_DOCUMENTS = "docs";
    String KEY_SHEET = "sheet";
    String KEY_PRODUCTION = "production";
    String KEY_NOTES = "notes";
    String KEY_TERRITORY = "territory";
    String KEY_VEHICLES = "vehicles";
    String KEY_CADASTRE = "cadastre";
    String KEY_VIOLATE_TYPES = "violation_types";
    String KEY_FOREST_CAT_TYPES = "forest_cat_types";
    String KEY_SPECIES_TYPES = "species_types";

    String KEY_LAYER_DOCUMENTS = "documents";
    String KEY_LAYER_SHEET = "sheet";
    String KEY_LAYER_PRODUCTION = "production";
    String KEY_LAYER_NOTES = "notes";
    String KEY_LAYER_TERRITORY = "territory";
    String KEY_LAYER_VEHICLES = "vehicles";
    String KEY_LAYER_CADASTRE = "cadastre";
    String KEY_LAYER_VIOLATE_TYPES = "violation_types";
    String KEY_LAYER_FOREST_CAT_TYPES = "forest_cat_types";
    String KEY_LAYER_SPECIES_TYPES = "species_types";

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

    /**
     * documents fields
     */
    String FIELD_DOCUMENTS_TYPE = "type";
    String FIELD_DOCUMENTS_DATE = "date";
    String FIELD_DOCUMENTS_NUMBER = "number";
    String FIELD_DOCUMENTS_STATUS = "status";
    String FIELD_DOCUMENTS_VIOLATE = "violate";
    String FIELD_DOCUMENTS_PARENT_ID = "doc_id";
    String FIELD_DOCUMENTS_AUTHOR = "author";
    String FIELD_DOCUMENTS_USER = "user";
    String FIELD_DOCUMENTS_PLACE = "place";
    String FIELD_DOCUMENTS_VIOLATION_TYPE = "violate";
    String FIELD_DOCUMENTS_LAW = "law";
    String FIELD_DOCUMENTS_USER_PICK = "user_pick";
    String FIELD_DOCUMENTS_USER_TRANS = "user_trans";
    String FIELD_DOCUMENTS_DATE_VIOLATE = "date_violate";
    String FIELD_DOCUMENTS_CRIME = "crime";
    String FIELD_DOCUMENTS_DESC_DETECTOR = "desc_detector";
    String FIELD_DOCUMENTS_DESC_CRIME = "desc_crime";
    String FIELD_DOCUMENTS_DESC_AUTHOR = "desc_author";
    String FIELD_DOCUMENTS_DESCRIPTION = "descript";

    /**
     * notes fields
     */
    String FIELD_NOTES_DATE_BEG = "date_beg";
    String FIELD_NOTES_DATE_END = "date_end";
    String FIELD_NOTES_DESCRIPTION = "descript";

    /**
     * territory fields
     */
    String FIELD_TERRITORY_AREA = "forestarea";
    String FIELD_TERRITORY_DISTRICT = "forestdist";
    String FIELD_TERRITORY_PARCEL = "parcel";
    String FIELD_TERRITORY_UNIT = "unit";

    /**
     * vehicle fields
     */
    String FIELD_VEHICLE_NAME = "name";
    String FIELD_VEHICLE_DESCRIPTION = "descript";
    String FIELD_VEHICLE_ENGINE_NUM = "engine_num";
    String FIELD_VEHICLE_USER = "user";

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
