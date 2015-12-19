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

package com.nextgis.forestinspector.util;

/**
 * Constants
 */
public interface Constants
{

    String FITAG                        = "forest inspector";
    int    MAX_DOCUMENTS                = 100;
    int    MAX_NOTES                    = 100;
    String TEMP_DOCUMENT_FEATURE_FOLDER = "temp_document";
    String SIGN_FILENAME                = "sign.png";
    String SIGN_DESCRIPTION             = "sign";

    /**
     * init sync status
     */
    String BROADCAST_MESSAGE = "forestviolate.inspector.sync_message";
    String KEY_STEP          = "sync_step";
    String KEY_STATE         = "sync_state";
    String KEY_MESSAGE       = "sync_message";

    String KEY_INSPECTORS       = "inspectors";
    String KEY_DOCUMENTS        = "docs";
    String KEY_SHEET            = "sheet";
    String KEY_PRODUCTION       = "production";
    String KEY_NOTES            = "notes";
    String KEY_VEHICLES         = "vehicles";
    String KEY_KV               = "kv";
    String KEY_LV               = "lv";
    String KEY_ULV              = "ulv";
    String KEY_VIOLATE_TYPES    = "violation_types";
    String KEY_FOREST_CAT_TYPES = "forest_cat_types";
    String KEY_SPECIES_TYPES    = "species_types";
    String KEY_THICKNESS_TYPES  = "thickness_types";
    String KEY_TREES_TYPES      = "trees_types";
    String KEY_HEIGHT_TYPES     = "height_types";
    String KEY_FV               = "fv";

    String KEY_LAYER_DOCUMENTS        = "documents";
    String KEY_LAYER_SHEET            = "sheet";
    String KEY_LAYER_PRODUCTION       = "production";
    String KEY_LAYER_NOTES            = "notes";
    String KEY_LAYER_VEHICLES         = "vehicles";
    String KEY_LAYER_KV               = "kv";
    String KEY_LAYER_LV               = "lv";
    String KEY_LAYER_ULV              = "ulv";
    String KEY_LAYER_VIOLATE_TYPES    = "violation_types";
    String KEY_LAYER_FOREST_CAT_TYPES = "forest_cat_types";
    String KEY_LAYER_SPECIES_TYPES    = "species_types";
    String KEY_LAYER_THICKNESS_TYPES  = "thickness_types";
    String KEY_LAYER_TREES_TYPES      = "trees_types";
    String KEY_LAYER_HEIGHT_TYPES     = "height_types";
    String KEY_LAYER_FV               = "fv";

    /**
     * inspectors keys
     */
    String KEY_INSPECTOR_USER         = "user";
    String KEY_INSPECTOR_USER_DESC    = "user_desc";
    String KEY_INSPECTOR_USER_PASS_ID = "pass_id";

    /**
     * notes keys
     */
    String KEY_NOTES_USERID = "user_id";

    /**
     * document types
     */
    int DOC_TYPE_NOTE       = 0;
    int DOC_TYPE_CHECK      = 1;
    int DOC_TYPE_INDICTMENT = 2;
    int DOC_TYPE_SHEET      = 3;
    int DOC_TYPE_VEHICLE    = 4;

    int STEP_STATE_WAIT  = 0;
    int STEP_STATE_WORK  = 1;
    int STEP_STATE_DONE  = 2;
    int STEP_STATE_ERROR = 3;

    String FIELD_DOC_ID                    = "doc_id";
    /**
     * documents fields
     */
    String FIELD_DOCUMENTS_TYPE            = "type";
    String FIELD_DOCUMENTS_DATE            = "date";
    String FIELD_DOCUMENTS_NUMBER          = "number";
    String FIELD_DOCUMENTS_STATUS          = "status";
    String FIELD_DOCUMENTS_AUTHOR          = "author";
    String FIELD_DOCUMENTS_USER            = "user";
    String FIELD_DOCUMENTS_PLACE           = "place";
    String FIELD_DOCUMENTS_VIOLATION_TYPE  = "violate";
    String FIELD_DOCUMENTS_LAW             = "law";
    String FIELD_DOCUMENTS_DATE_PICK       = "date_pick";
    String FIELD_DOCUMENTS_USER_PICK       = "user_pick";
    String FIELD_DOCUMENTS_USER_TRANS      = "user_trans";
    String FIELD_DOCUMENTS_DATE_VIOLATE    = "date_violate";
    String FIELD_DOCUMENTS_CRIME           = "crime";
    String FIELD_DOCUMENTS_DESC_DETECTOR   = "desc_detector";
    String FIELD_DOCUMENTS_DESC_CRIME      = "desc_crime";
    String FIELD_DOCUMENTS_DESC_AUTHOR     = "desc_author";
    String FIELD_DOCUMENTS_DESCRIPTION     = "descript";
    String FIELD_DOCUMENTS_FOREST_CAT_TYPE = "forest_cat";
    String FIELD_DOCUMENTS_TERRITORY       = "territory";
    String FIELD_DOCUMENTS_REGION          = "region";
    String FIELD_DOCUMENTS_VECTOR          = "vector";
    String FIELD_DOCUMENTS_POS             = "pos";

    /**
     * notes fields
     */
    String FIELD_NOTES_DATE_BEG    = "date_beg";
    String FIELD_NOTES_DATE_END    = "date_end";
    String FIELD_NOTES_DESCRIPTION = "descript";
    String FIELD_NOTES_LOGIN       = "login";
    String FIELD_NOTES_USER_ID     = "user_id";

    /**
     * fv fields
     */
    String FIELD_FV_OBJECTID  = "objectID";
    String FIELD_FV_DATE      = "date";
    String FIELD_FV_FORESTRY  = "forestery";
    String FIELD_FV_PRECINCT  = "precinct";
    String FIELD_FV_REGION    = "region";
    String FIELD_FV_TERRITORY = "territory";

    /**
     * vehicle fields
     */
    String FIELD_VEHICLE_NAME        = "name";
    String FIELD_VEHICLE_DESCRIPTION = "descript";
    String FIELD_VEHICLE_ENGINE_NUM  = "engine_num";
    String FIELD_VEHICLE_USER        = "user";

    /**
     * sheet fields
     */
    String FIELD_SHEET_UNIT      = "unit";
    String FIELD_SHEET_SPECIES   = "poroda";
    String FIELD_SHEET_CATEGORY  = "additions";
    String FIELD_SHEET_THICKNESS = "diameter";
    String FIELD_SHEET_HEIGHTS   = "heights";
    String FIELD_SHEET_COUNT     = "count";

    /**
     * cadastre fields
     */
    String FIELD_CADASTRE_LV     = "NAME_LV";
    String FIELD_CADASTRE_ULV    = "NAME_ULV";
    String FIELD_CADASTRE_PARCEL = "KV";

    /**
     * production fields
     */
    String FIELD_PRODUCTION_SPECIES   = "poroda";
    String FIELD_PRODUCTION_TYPE      = "type_desc";
    String FIELD_PRODUCTION_LENGTH    = "length";
    String FIELD_PRODUCTION_THICKNESS = "diameter";
    String FIELD_PRODUCTION_COUNT     = "count";

    int DOWNLOAD_SEPARATE_THREADS = 10;

    int DOCUMENT_STATUS_SEND     = 1;       //the document send but some connected tables - no
    int DOCUMENT_STATUS_CHECKING = 2;   //the moderator check document
    int DOCUMENT_STATUS_OK       = 3;         //the document accepted
    int DOCUMENT_STATUS_WARNING  = 4;    //the document denied
    int DOCUMENT_STATUS_DELETED  = 5;    //the document deleted

    /**
     * layer types
     */
    int LAYERTYPE_DOCS = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 1);

    String FRAGMENT_PRODUCTION_FILLER        = "production_filler";
    String FRAGMENT_SHEET_FILLER             = "sheet_filler";
    String FRAGMENT_VEHICLE_FILLER           = "vehicle_filler";
    String FRAGMENT_NOTE_FILLER              = "note_filler";
    String FRAGMENT_LIST_FILLER_DIALOG       = "list_filler_dialog";
    String FRAGMENT_SIGN_DIALOG              = "sign_dialog";
    String FRAGMENT_PHOTO_TABLE              = "photo_table";
    String FRAGMENT_PHOTO_DESC_EDITOR_DIALOG = "photo_desc_editor_dialog";
    String FRAGMENT_TARGETING_DIALOG         = "targeting_dialog";
}
