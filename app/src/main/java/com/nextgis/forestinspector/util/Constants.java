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
    int    MONTH_TO_LOAD_FV_DATA        = -6;
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
    String KEY_NOTES_QUERY      = "notes_query";
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
    String KEY_FIELDWORK_TYPES  = "fieldwork_types";
    String KEY_CONTRACT_TYPES   = "contract_types";
    String KEY_FV               = "fv";
    String KEY_FV_REGIONS       = "fv_regions";

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
    String KEY_LAYER_FIELDWORK_TYPES  = "fieldwork_types";
    String KEY_LAYER_CONTRACT_TYPES   = "contract_types";
    String KEY_LAYER_FV               = "fv";
    String KEY_LAYER_FV_REGIONS       = "fv_regions";

    /**
     * inspectors keys
     */
    String KEY_INSPECTOR_USER         = "user"; // STRING
    String KEY_INSPECTOR_USER_DESC    = "user_desc"; // STRING
    String KEY_INSPECTOR_LOGIN        = "login"; // STRING
    String KEY_INSPECTOR_USER_PASS_ID = "pass_id"; // STRING

    /**
     * document types
     */
    int DOC_TYPE_FIELD_WORKS  = 1;
    int DOC_TYPE_INDICTMENT   = 2;
    int DOC_TYPE_SHEET        = 3;
    int DOC_TYPE_VEHICLE      = 4;
    int DOC_TYPE_DOCUMENT_ANY = 21;
    int DOC_TYPE_TARGET       = 22;
    int DOC_TYPE_NOTE         = 23;

    int STEP_STATE_WAIT   = 0;
    int STEP_STATE_WORK   = 1;
    int STEP_STATE_DONE   = 2;
    int STEP_STATE_FINISH = 3;
    int STEP_STATE_ERROR  = 4;
    int STEP_STATE_CANCEL = 5;


    /**
     * common fields
     */
    String FIELD_DOC_ID = "doc_id"; // INTEGER

    /**
     * documents fields
     */
    String FIELD_DOCUMENTS_TYPE            = "type"; // INTEGER
    String FIELD_DOCUMENTS_DATE            = "date"; // DATETIME
    String FIELD_DOCUMENTS_PLACE           = "place"; // STRING
    String FIELD_DOCUMENTS_NUMBER          = "number"; // STRING
    String FIELD_DOCUMENTS_USER            = "user"; // STRING
    String FIELD_DOCUMENTS_VIOLATION_TYPE  = "violate"; // STRING
    String FIELD_DOCUMENTS_LAW             = "law"; // STRING
    String FIELD_DOCUMENTS_POS             = "pos"; // STRING
    String FIELD_DOCUMENTS_USER_PICK       = "user_pick"; // STRING
    String FIELD_DOCUMENTS_CRIME           = "crime"; // STRING
    String FIELD_DOCUMENTS_USER_TRANS      = "user_trans"; // STRING
    String FIELD_DOCUMENTS_AUTHOR          = "author"; // STRING
    String FIELD_DOCUMENTS_DESCRIPTION     = "descript"; // STRING
    String FIELD_DOCUMENTS_VECTOR          = "vector"; // STRING
    String FIELD_DOCUMENTS_STATUS          = "status"; // INTEGER
    String FIELD_DOCUMENTS_DATE_PICK       = "date_pick"; // DATETIME
    String FIELD_DOCUMENTS_FOREST_CAT_TYPE = "forest_cat"; // STRING
    String FIELD_DOCUMENTS_DATE_VIOLATE    = "date_violate"; // STRING
    String FIELD_DOCUMENTS_DESC_DETECTOR   = "desc_detector"; // STRING
    String FIELD_DOCUMENTS_DESC_CRIME      = "desc_crime"; // STRING
    String FIELD_DOCUMENTS_DESC_AUTHOR     = "desc_author"; // STRING
    String FIELD_DOCUMENTS_TERRITORY       = "territory"; // STRING
    String FIELD_DOCUMENTS_REGION          = "region"; // STRING
    String FIELD_DOCUMENTS_USER_ID         = "user_id"; // INTEGER
    String FIELD_DOCUMENTS_CONTRACT_DATE   = "contract_date"; // DATE

    /**
     * sheet fields
     */
    String FIELD_SHEET_UNIT      = "unit"; // STRING
    String FIELD_SHEET_SPECIES   = "poroda"; // STRING
    String FIELD_SHEET_CATEGORY  = "additions"; // STRING
    String FIELD_SHEET_COUNT     = "count"; // INTEGER
    String FIELD_SHEET_THICKNESS = "diameter"; // INTEGER
    String FIELD_SHEET_CHANGE    = "change"; // DATETIME
    String FIELD_SHEET_HEIGHTS   = "heights"; // STRING

    /**
     * production fields
     */
    String FIELD_PRODUCTION_TYPE      = "type_desc"; // STRING
    String FIELD_PRODUCTION_THICKNESS = "diameter"; // REAL
    String FIELD_PRODUCTION_LENGTH    = "length"; // REAL
    String FIELD_PRODUCTION_COUNT     = "count"; // INTEGER
    String FIELD_PRODUCTION_CHANGE    = "change"; // DATETIME
    String FIELD_PRODUCTION_SPECIES   = "poroda"; // STRING

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
    String FIELD_FV_STATUS    = "status";
    String FIELD_FV_DATE      = "date";
    String FIELD_FV_FORESTRY  = "forestery";
    String FIELD_FV_PRECINCT  = "precinct";
    String FIELD_FV_REGION    = "region";
    String FIELD_FV_TERRITORY = "territory";


    String FV_STATUS_NEW_FOREST_CHANGE = "Новое лесоизменение";

    /**
     * vehicle fields
     */
    String FIELD_VEHICLE_NAME        = "name";
    String FIELD_VEHICLE_DESCRIPTION = "descript";
    String FIELD_VEHICLE_ENGINE_NUM  = "engine_num";
    String FIELD_VEHICLE_USER        = "user";

    /**
     * cadastre fields
     */
    String FIELD_CADASTRE_REGION = "REGION";
    String FIELD_CADASTRE_LV     = "NAME_LV";
    String FIELD_CADASTRE_ULV    = "NAME_ULV";
    String FIELD_CADASTRE_KV     = "KV";

    /**
     * regions fields
     */
    String FIELD_REGIONS_NAME_RU = "NAME_RU";
    String FIELD_REGIONS_NAME_EN = "NAME_EN";
    String FIELD_REGIONS_PHONE   = "PHONE";

    int DOWNLOAD_SEPARATE_THREADS = 10;

    int DOCUMENT_STATUS_NEW      = 0;   //the document is new
    int DOCUMENT_STATUS_FOR_SEND = 1;   //the document is sent but some connected tables - no
    int DOCUMENT_STATUS_CHECKING = 2;   //the moderator check document
    int DOCUMENT_STATUS_OK       = 3;   //the document accepted
    int DOCUMENT_STATUS_WARNING  = 4;   //the document denied
    int DOCUMENT_STATUS_DELETED  = 5;   //the document deleted

    /**
     * layer types
     */
    int LAYERTYPE_DOCS  = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 1);
    int LAYERTYPE_LV    = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 2);
    int LAYERTYPE_ULV   = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 3);
    int LAYERTYPE_KV    = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 4);
    int LAYERTYPE_NOTES = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 5);
    int LAYERTYPE_FV    = 1 << (com.nextgis.maplib.util.Constants.LAYERTYPE_SYSMAX + 6);

    String FRAGMENT_PRODUCTION_FILLER        = "production_filler";
    String FRAGMENT_SHEET_FILLER             = "sheet_filler";
    String FRAGMENT_VEHICLE_FILLER           = "vehicle_filler";
    String FRAGMENT_NOTE_FILLER              = "note_filler";
    String FRAGMENT_LIST_FILLER_DIALOG       = "list_filler_dialog";
    String FRAGMENT_SIGN_DIALOG              = "sign_dialog";
    String FRAGMENT_PHOTO_TABLE              = "photo_table";
    String FRAGMENT_PHOTO_DESC_EDITOR_DIALOG = "photo_desc_editor_dialog";
    String FRAGMENT_TARGETING_DIALOG         = "targeting_dialog";
    String FRAGMENT_CLICKED_LIST_DIALOG      = "clicked_list_dialog";
    String FRAGMENT_MAP                      = "map";
    String FRAGMENT_PREFERENCE               = "preference";
    String FRAGMENT_NGW_SETTINGS             = "ngw_settings";
    String FRAGMENT_SYNC_SETTINGS            = "sync_settings";
    String FRAGMENT_LAYER_LIST               = "layer_list";
    String FRAGMENT_SHEET_TABLE_FILLER       = "sheet_table_filler";
    String FRAGMENT_TABLE_NUMBER_DIALOG      = "table_number_dialog";
    String FRAGMENT_UNIT_EDITOR_DIALOG       = "unit_editor_dialog";

    int DOCS_VECTOR_SCOPE = 10000;

    int INDICTMENT_ACTIVITY  = 1101;
    int SHEET_ACTIVITY       = 1102;
    int FIELD_WORKS_ACTIVITY = 1103;

    String DOCUMENT_VIEWER = "document_viewer";

    int DOCUMENTS_LOADER = 0;
    int PARCELS_LOADER   = 1;
    int TARGETING_LOADER = 2;
    int CLICKED_LOADER   = 3;

    float LV_MIN_ZOOM  = 0f;
    float LV_MAX_ZOOM  = 9.5f;
    float ULV_MIN_ZOOM = 9.5f;
    float ULV_MAX_ZOOM = 13.5f;
    float KV_MIN_ZOOM  = 13.5f;
    float KV_MAX_ZOOM  = 23f;

    int DEFAULT_COORDINATES_FRACTION_DIGITS = 2;
}
