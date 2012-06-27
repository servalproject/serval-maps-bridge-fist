/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Maps Bridge Template
 *
 * Serval Maps Bridge Template Software is free software; you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.maps.bridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.webkit.URLUtil;

/**
 * display a list of preferences that the user can configure
 */
public class SettingsActivity extends PreferenceActivity {
	
	/*
	 * private class level constants
	 */
	private static final int sEmptyUrlDialog = 0;
	private static final int sInvalidUrlDialog = 1;
	
	/*
	 * create the activity
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;
		
		int mMessageId;
		
		switch(id) {
		case sEmptyUrlDialog:
			mMessageId = R.string.settings_activity_ui_dialog_missing_url;
			break;
		case sInvalidUrlDialog:
			mMessageId = R.string.settings_activity_ui_dialog_invalid_url;
			break;
		default:
			return super.onCreateDialog(id);
		}
		
		// build the dialog
		mBuilder.setMessage(mMessageId)
		.setCancelable(false)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		});
		
		// create and return the dialog
		mDialog = mBuilder.create();
		return mDialog;
	}
	
	
	/*
	 * validate the preferences by listening for changes in preference values
	 */
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

		/* 
		 * (non-Javadoc)
		 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
		 */
		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
			
			// validate the preference
			if(key.equals("preferences_upload_url") == true) {
				
				String mPreference = preferences.getString("preferences_upload_url", null);
				
				// valide the url
				if(TextUtils.isEmpty(mPreference) == true) {
					showDialog(sEmptyUrlDialog);
				} else if(URLUtil.isValidUrl(mPreference) == false) {
					showDialog(sInvalidUrlDialog);
				}
			}
			
		}
	};	
}