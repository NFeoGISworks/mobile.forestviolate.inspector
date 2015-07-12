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

    String KEY_INSPECTORS = "inspectors";
    String KEY_DOCUMENTS = "documents";
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

    String KEY_INSPECTOR_USER = "user";
    String KEY_INSPECTOR_USER_DESC = "user_desc";

    int STEP_STATE_WAIT = 0;
    int STEP_STATE_WORK = 1;
    int STEP_STATE_DONE = 2;
    int STEP_STATE_ERROR = 3;

}
