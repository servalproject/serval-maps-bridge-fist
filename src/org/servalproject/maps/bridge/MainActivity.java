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

import java.util.List;

import org.servalproject.maps.bridge.provider.LogContract;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * main activity, displayed when the application is first started
 */
public class MainActivity extends Activity implements OnClickListener {
	
	/*
	 * define class level constants
	 */
	private static final String sTag = "MainActivity";
	
	private static final int sNoEmailDialog = 0;
	private static final int sResetLogDialog = 1;
	private static final int sResetLogCompleteDialog = 2;
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // configure the buttons
        Button mButton = (Button) findViewById(R.id.main_activity_ui_btn_settings);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_batch);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_log);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_reset_log);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_contact);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_close);
        mButton.setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		Intent mIntent;
		
		// determine which button was touched
		switch(v.getId()) {
		case R.id.main_activity_ui_btn_settings:
			// show the settings activity
			mIntent = new Intent(this, org.servalproject.maps.bridge.SettingsActivity.class);
			startActivity(mIntent);
			break;
		case R.id.main_activity_ui_btn_batch:
			// upload a batch of new POI records
			mIntent = new Intent(this, org.servalproject.maps.bridge.BatchActivity.class);
			startActivity(mIntent);
			break;
		case R.id.main_activity_ui_btn_log:
			// view the upload log
			mIntent = new Intent(this, org.servalproject.maps.bridge.ViewLogActivity.class);
			startActivity(mIntent);
			break;
		case R.id.main_activity_ui_btn_reset_log:
			// reset the activity log
			showDialog(sResetLogDialog);
			break;
		case R.id.main_activity_ui_btn_contact:
			// contact the developers
			sendEmail();
			break;
		case R.id.main_activity_ui_btn_close:
			// close the activity
			finish();
			break;
		default:
			Log.w(sTag, "an unknown view fired the onClick event");
		}
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
		case sNoEmailDialog:
			// show the no email client dialog
			mMessageId = R.string.main_activity_ui_dialog_no_email;
			break;
		case sResetLogDialog:
			// show the reset log dialog
			mMessageId = R.string.main_activity_ui_dialog_reset_log;
			break;
		case sResetLogCompleteDialog:
			// show dialog to alert user to process completion
			mMessageId = R.string.main_activity_ui_dialog_reset_log_complete;
			break;
		default:
			return super.onCreateDialog(id);
		}
		
		// build the dialog
		if(id != sResetLogDialog) {
			mBuilder.setMessage(mMessageId)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
		} else {
			mBuilder.setMessage(mMessageId)
			.setCancelable(false)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					resetActivityLog();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
		}
		
		// create and return the dialog
		mDialog = mBuilder.create();
		return mDialog;
	}
	
	// private method to start the email activity
	private void sendEmail() {
		// contact the author of the application
		// check to see if we can potentially send an email
		Intent mIntent = new Intent(android.content.Intent.ACTION_SEND);
		PackageManager mPackageManager = getPackageManager();
		
		List<ResolveInfo> mInfoList = mPackageManager.queryIntentActivities(mIntent, PackageManager.MATCH_DEFAULT_ONLY);
		
		if(mInfoList.size() > 0) {
			// an email client is likely to be installed
			// send a contact email
			mIntent.setType("plain/text");
			mIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.system_contact_email)});
			mIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.system_contact_email_subject));
			
			startActivity(Intent.createChooser(mIntent, getString(R.string.system_contact_email_chooser)));
		} else {
			// no email client is installed
			// show a dialog 
			
			String mMessage = String.format(getString(R.string.main_activity_ui_dialog_no_email), getString(R.string.system_contact_email));
			
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
			mBuilder.setMessage(mMessage)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog mAlert = mBuilder.create();
			mAlert.show();
		}
	}

	// private method to reset the activity log
	private void resetActivityLog() {
		
		ContentResolver mContentResolver = getContentResolver();
		
		mContentResolver.delete(
				LogContract.CONTENT_URI, 
				null,
				null);
		
		showDialog(sResetLogCompleteDialog);
	}
}