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

package com.nextgis.forestinspector.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.nextgis.forestinspector.R;
import com.nextgis.forestinspector.util.SettingsConstants;
import com.nextgis.maplibui.fragment.NGWLoginFragment;


/**
 * The login fragment to the forest violations server
 */
public class LoginFragment
        extends NGWLoginFragment
{

    @Override
    public View     onCreateView(
            LayoutInflater inflater,
            @Nullable
            ViewGroup container,
            @Nullable
            Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        mURL = (EditText) view.findViewById(R.id.url);
        mLogin = (EditText) view.findViewById(R.id.login);
        mPassword = (EditText) view.findViewById(R.id.password);
        mSignInButton = (Button) view.findViewById(R.id.signin);

        TextWatcher watcher = new FvTextWatcher();
        mURL.addTextChangedListener(watcher);
        mLogin.addTextChangedListener(watcher);
        mPassword.addTextChangedListener(watcher);

        TextView loginDescription = (TextView) view.findViewById(R.id.login_description);
        if (mForNewAccount) {
            loginDescription.setText(R.string.fi_login_description);
            mURL.setText(SettingsConstants.SITE_URL);
        } else {
            loginDescription.setText(R.string.fi_edit_pass_description);
            mURL.setText(mUrlText);
            mLogin.setText(mLoginText);
            mURL.setEnabled(mChangeAccountUrl);
            mLogin.setEnabled(mChangeAccountLogin);
        }

        mURL.setEnabled(mForNewAccount);
        mLogin.setEnabled(mForNewAccount);

        // for debug
//        mLogin.setText("testuser");
//        mPassword.setText("userpass");

        return view;
    }


    @Override
    public void onClick(View v)
    {
        if (mForNewAccount) {
            mLogin.setText(mLogin.getText().toString().trim());
        }

        if (v == mSignInButton) {
            if (null != mLoader && mLoader.isStarted()) {
                mLoader = getLoaderManager().restartLoader(R.id.auth_token_loader, null, this);
            } else {
                mLoader = getLoaderManager().initLoader(R.id.auth_token_loader, null, this);
            }
        }

        mSignInButton.setEnabled(false);
    }


    @Override
    public void onTokenReceived(
            String accountName,
            String token)
    {
        accountName = getString(R.string.account_name);
        super.onTokenReceived(accountName, token);
    }


    protected void updateButtonState()
    {
        if (checkEditText(mURL) && checkEditText(mLogin) && checkEditText(mPassword)) {
            mSignInButton.setEnabled(true);
        }
    }


    public class FvTextWatcher
            implements TextWatcher
    {
        public void afterTextChanged(Editable s)
        {
            updateButtonState();
            mUrlText = mURL.getText().toString().trim();
        }


        public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after)
        {
        }


        public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count)
        {
        }
    }
}
