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

package com.nextgis.forestinspector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of initialize steps
 */
public class InitStepListAdapter extends BaseAdapter {

    protected List<InitStep> mSteps;
    protected Context mContext;

    public InitStepListAdapter(Context context) {
        mContext = context;
        mSteps = new ArrayList<>();

        // 1. check server
        InitStep step1 = new InitStep(context.getString(R.string.check_server),
                                      context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step1);

        // 2. get inspector detail
        InitStep step2 = new InitStep(context.getString(R.string.get_inspector_details),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step2);

        // 3. create base layers
        InitStep step3 = new InitStep(context.getString(R.string.create_base_layers),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step3);

        // 4. forest cadastre
        InitStep step4 = new InitStep(context.getString(R.string.get_forest_cadastre),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step4);

        // 5. load documents
        InitStep step5 = new InitStep(context.getString(R.string.load_documents),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step5);

        // 6. load linked tables
        InitStep step6 = new InitStep(context.getString(R.string.load_linked_layers),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step6);

        // 7. load notes
        InitStep step7 = new InitStep(context.getString(R.string.load_notes),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step7);

        // 8. load targeting
        InitStep step8 = new InitStep(context.getString(R.string.load_targeting),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step8);

        // 9. load targeting
        InitStep step9 = new InitStep(context.getString(R.string.load_regions),
                context.getString(R.string.waiting), Constants.STEP_STATE_WAIT);
        mSteps.add(step9);

        // 10. load other offline vector data (scanex points, etc.)
    }

    @Override
    public int getCount() {
        if(null == mSteps)
            return 0;
        return mSteps.size();
    }

    @Override
    public Object getItem(int position) {
        return mSteps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.item_initstep, null);
        }

        InitStep item = (InitStep) getItem(position);

        ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
        switch (item.mState){
            case Constants.STEP_STATE_WAIT:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_file_cloud_queue));
                break;
            case Constants.STEP_STATE_WORK:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_file_cloud_download));
                break;
            case Constants.STEP_STATE_DONE:
            case Constants.STEP_STATE_FINISH:
            case Constants.STEP_STATE_CANCEL:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_file_cloud_done));
                break;
            case Constants.STEP_STATE_ERROR:
                ivIcon.setImageDrawable( mContext.getResources().getDrawable(R.drawable.ic_action_file_cloud_off));
                break;
        }

        TextView tvStep = (TextView) v.findViewById(R.id.tvName);
        tvStep.setText(item.mStepName);

        TextView tvDesc = (TextView) v.findViewById(R.id.tvDesc);
        tvDesc.setText(item.mStepDescription);

        return v;
    }

    public void setMessage(int step, int state, String message) {
        if(step > 0) {
            for (int i = 0; i < step; i++) {
                InitStepListAdapter.InitStep initStep =
                        (InitStepListAdapter.InitStep) getItem(i);
                initStep.mStepDescription = mContext.getString(R.string.done);
                initStep.mState = Constants.STEP_STATE_DONE;
            }
        }

        InitStepListAdapter.InitStep initStep =
                (InitStepListAdapter.InitStep) getItem(step);
        initStep.mStepDescription = message;
        initStep.mState = state;

        notifyDataSetChanged();
    }

    public class InitStep {
        public String mStepName;
        public String mStepDescription;
        public int mState; //0 - wait, 1 - working, 2 - finished, 3 - error

        public InitStep(String stepName, String stepDescription, int state) {
            mStepDescription = stepDescription;
            mStepName = stepName;
            mState = state;
        }
    }
}
