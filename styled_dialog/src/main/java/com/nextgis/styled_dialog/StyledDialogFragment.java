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

package com.nextgis.styled_dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class StyledDialogFragment
        extends DialogFragment
{
    protected Integer mIconId;

    protected Integer mTitleId;
    protected Integer mMessageId;
    protected Integer mPositiveTextId;
    protected Integer mNegativeTextId;

    protected CharSequence mTitleText;
    protected CharSequence mMessageText;
    protected CharSequence mPositiveText;
    protected CharSequence mNegativeText;

    protected Integer mThemeResId;
    protected boolean mIsThemeDark = false;

    protected ImageView    mIcon;
    protected TextView     mTitle;
    protected View         mTitleDivider;
    protected LinearLayout mDialogLayout;
    protected TextView     mMessage;
    protected View         mView;
    protected LinearLayout mButtons;
    protected Button       mButtonPositive;
    protected Button       mButtonNegative;

    protected OnPositiveClickedListener mOnPositiveClickedListener;
    protected OnNegativeClickedListener mOnNegativeClickedListener;
    protected OnCancelListener          mOnCancelListener;
    protected OnDismissListener         mOnDismissListener;

    protected Integer mTitleDividerVisibility;

    protected boolean mKeepInstance = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(mKeepInstance);
    }


    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }

        mDialogLayout.removeAllViews();

        super.onDestroyView();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Idea from here
        // http://thanhcs.blogspot.ru/2014/10/android-custom-dialog-fragment.html

        ContextThemeWrapper context;

        if (null != mThemeResId) {
            context = new ContextThemeWrapper(getActivity(), mThemeResId);
        } else if (mIsThemeDark) {
            context = new ContextThemeWrapper(getActivity(), R.style.SdfTheme_Dark);
        } else {
            context = new ContextThemeWrapper(getActivity(), R.style.SdfTheme_Light);
        }

        Dialog dialog = new Dialog(context);

        Window window = dialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setContentView(R.layout.sdf_layout);

        mIcon = (ImageView) dialog.findViewById(R.id.title_icon);
        mTitle = (TextView) dialog.findViewById(R.id.title_text);
        mTitleDivider = dialog.findViewById(R.id.title_divider);
        mDialogLayout = (LinearLayout) dialog.findViewById(R.id.dialog_body);
        mButtons = (LinearLayout) dialog.findViewById(R.id.buttons);
        mButtonPositive = (Button) dialog.findViewById(R.id.button_positive);
        mButtonNegative = (Button) dialog.findViewById(R.id.button_negative);

        if (null != mIconId) {
            mIcon.setVisibility(View.VISIBLE);
            mIcon.setImageResource(mIconId);
        }

        if (null != mTitleId) {
            mTitle.setText(mTitleId);
        }
        if (null != mTitleText) {
            mTitle.setText(mTitleText);
        }
        if (null != mTitleDividerVisibility) {
            mTitleDivider.setVisibility(mTitleDividerVisibility);
        }

        if (null != mMessageId) {
            setMessageView();
            mMessage.setText(mMessageId);
        }
        if (null != mMessageText) {
            setMessageView();
            mMessage.setText(mMessageText);
        }

        if (null != mView) {
            mDialogLayout.setVisibility(View.VISIBLE);
            mDialogLayout.addView(mView);
        }

        if (null != mPositiveTextId) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonPositive.setVisibility(View.VISIBLE);
            mButtonPositive.setText(mPositiveTextId);
        }
        if (null != mPositiveText) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonPositive.setVisibility(View.VISIBLE);
            mButtonPositive.setText(mPositiveText);
        }

        if (null != mNegativeTextId) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonNegative.setVisibility(View.VISIBLE);
            mButtonNegative.setText(mNegativeTextId);
        }
        if (null != mNegativeText) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonNegative.setVisibility(View.VISIBLE);
            mButtonNegative.setText(mNegativeText);
        }


        if (null != mOnPositiveClickedListener) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonPositive.setVisibility(View.VISIBLE);
            mButtonPositive.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (null != mOnPositiveClickedListener) {
                                mOnPositiveClickedListener.onPositiveClicked();
                            }
                            dismiss();
                        }
                    });
        }

        if (null != mOnNegativeClickedListener) {
            mButtons.setVisibility(View.VISIBLE);
            mButtonNegative.setVisibility(View.VISIBLE);
            mButtonNegative.setOnClickListener(
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (null != mOnNegativeClickedListener) {
                                mOnNegativeClickedListener.onNegativeClicked();
                            }
                            dismiss();
                        }
                    });
        }

        return dialog;
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (null != mOnCancelListener) {
            mOnCancelListener.onCancel();
        }
        super.onCancel(dialog);
    }


    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (null != mOnDismissListener) {
            mOnDismissListener.onDismiss();
        }
        super.onDismiss(dialog);
    }


    public StyledDialogFragment setKeepInstance(boolean keepInstance)
    {
        mKeepInstance = keepInstance;
        return this;
    }


    public void setThemeResId(Integer themeResId)
    {
        mThemeResId = themeResId;
    }


    public void setThemeDark(boolean isDarkTheme)
    {
        mIsThemeDark = isDarkTheme;
    }


    protected void setMessageView()
    {
        LinearLayout layout =
                (LinearLayout) View.inflate(getActivity(), R.layout.sdf_message, null);
        mMessage = (TextView) layout.findViewById(R.id.dialog_message);
        mDialogLayout.setVisibility(View.VISIBLE);
        mDialogLayout.addView(layout);
    }


    public void setView(View view)
    {
        mView = view;
    }


    public StyledDialogFragment setIcon(int iconId)
    {
        mIconId = iconId;
        return this;
    }


    public StyledDialogFragment setTitle(int titleId)
    {
        mTitleId = titleId;
        return this;
    }


    public StyledDialogFragment setTitle(CharSequence titleText)
    {
        mTitleText = titleText;
        return this;
    }


    public void setTitleDividerVisibility(int visibility)
    {
        mTitleDividerVisibility = visibility;
    }


    public StyledDialogFragment setMessage(int messageId)
    {
        mMessageId = messageId;
        return this;
    }


    public StyledDialogFragment setMessage(CharSequence messageText)
    {
        mMessageText = messageText;
        return this;
    }


    public StyledDialogFragment setPositiveText(int positiveTextId)
    {
        mPositiveTextId = positiveTextId;
        return this;
    }


    public StyledDialogFragment setPositiveText(CharSequence positiveText)
    {
        mPositiveText = positiveText;
        return this;
    }


    public StyledDialogFragment setNegativeText(int negativeTextId)
    {
        mNegativeTextId = negativeTextId;
        return this;
    }


    public StyledDialogFragment setNegativeText(CharSequence negativeText)
    {
        mNegativeText = negativeText;
        return this;
    }


    public StyledDialogFragment setOnPositiveClickedListener(OnPositiveClickedListener onPositiveClickedListener)
    {
        mOnPositiveClickedListener = onPositiveClickedListener;
        return this;
    }


    public StyledDialogFragment setOnNegativeClickedListener(OnNegativeClickedListener onNegativeClickedListener)
    {
        mOnNegativeClickedListener = onNegativeClickedListener;
        return this;
    }


    public StyledDialogFragment setOnCancelListener(OnCancelListener onCancelListener)
    {
        mOnCancelListener = onCancelListener;
        return this;
    }


    public StyledDialogFragment setOnDismissListener(OnDismissListener onDismissListener)
    {
        mOnDismissListener = onDismissListener;
        return this;
    }


    public interface OnPositiveClickedListener
    {
        void onPositiveClicked();
    }


    public interface OnNegativeClickedListener
    {
        void onNegativeClicked();
    }


    public interface OnCancelListener
    {
        void onCancel();
    }


    public interface OnDismissListener
    {
        void onDismiss();
    }
}
