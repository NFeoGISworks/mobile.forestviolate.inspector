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

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;


/**
 * Form of sheet
 */
public class SheetCreatorActivity
        extends DocumentCreatorActivity
{
    @Override
    protected int getActivityCode()
    {
        return Constants.SHEET_ACTIVITY;
    }


    @Override
    protected int getDocType()
    {
        return Constants.DOC_TYPE_SHEET;
    }


    @Override
    protected int getContentViewRes()
    {
        return R.layout.activity_sheet_creator;
    }


    @Override
    protected CharSequence getTitleString()
    {
        return getText(R.string.sheet_title);
    }


    @Override
    protected int getMenuRes()
    {
        return R.menu.sheet_creator;
    }


    @Override
    protected void setControlViews()
    {
        if (null != mNewFeature) {

            Button createSheetBtn = (Button) findViewById(R.id.create_sheet);
            createSheetBtn.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            fillSheet();
                        }
                    });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_sheet:
                fillSheet();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void fillSheet()
    {
        Intent intent = new Intent(this, SheetFillerActivity.class);
        startActivity(intent);
    }
}
