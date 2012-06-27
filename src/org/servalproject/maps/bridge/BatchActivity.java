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

import java.util.Arrays;

import org.servalproject.maps.PointsOfInterestContract;
import org.servalproject.maps.bridge.fist.R;
import org.servalproject.maps.bridge.http.HttpUtils;
import org.servalproject.maps.bridge.provider.LogContract;
import org.servalproject.maps.bridge.tasks.BatchUploadTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * activity to support the batch uploading of POI data
 */
public class BatchActivity extends Activity implements OnClickListener {
	
	/*
	 * define class level constants
	 */
	private static final boolean V_LOG = true;
	private static final String sTag = "MainActivity";
	
	private static final int sNoUrlDialog = 0;
	private static final int sInvalidUrlDialog = 1;
	private static final int sNothingToDoDialog = 2;
	private static final int sUploadCompleteDialog = 3;
	private static final int sUploadFailedDialog = 4;
	private static final int sNoInternetDialog = 5;
	
	/*
	 * define class level variables
	 */
	private String uploadUrl = null;
	
	private Button uploadButton = null;
	private TextView statusText = null;
	private ProgressBar progressBar = null;
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch);
        
        // configure the buttons
        Button mButton = (Button) findViewById(R.id.batch_activity_ui_btn_close);
        mButton.setOnClickListener(this);
        
        uploadButton = (Button) findViewById(R.id.batch_activity_ui_btn_upload);
        uploadButton.setOnClickListener(this);
        
        statusText = (TextView) findViewById(R.id.batch_activity_ui_lbl_status);
        
        progressBar = (ProgressBar) findViewById(R.id.batch_activity_ui_progress);
        
        // get the url to use for uploads
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        uploadUrl = mPreferences.getString("preferences_upload_url", null);
        
        if(TextUtils.isEmpty(uploadUrl)) {
        	showDialog(sNoUrlDialog);
        	uploadButton.setEnabled(false);
        }else if(URLUtil.isValidUrl(uploadUrl) == false) {
        	showDialog(sInvalidUrlDialog);
        	uploadButton.setEnabled(false);
        }else if(HttpUtils.isOnline(this) == false) {
        	showDialog(sNoInternetDialog);
        	uploadButton.setEnabled(false);
        }
    }

	
	/*
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		
		// work out which button was touched
		switch(v.getId()) {
		case R.id.batch_activity_ui_btn_upload:
			// upload the data
			doUpload();
			break;
		case R.id.batch_activity_ui_btn_close:
			// close the activity
			finish();
			break;
		default:
			Log.w(sTag, "an unknown view fired the onClick method");
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
		case sNoUrlDialog:
			// url preference is missing
			mMessageId = R.string.batch_activity_ui_dialog_no_upload_url;
			break;
		case sInvalidUrlDialog:
			mMessageId = R.string.batch_activity_ui_dialog_invalid_upload_url;
			break;
		case sNothingToDoDialog:
			// all records have been done
			mMessageId = R.string.batch_activity_ui_dialog_nothing_to_do;
			break;
		case sUploadCompleteDialog:
			// everything has completed
			mMessageId = R.string.batch_activity_ui_dialog_upload_complete;
			break;
		case sUploadFailedDialog:
			// something bad happened
			mMessageId = R.string.batch_activity_ui_dialog_upload_failed;
			break;
		case sNoInternetDialog:
			// no internet access
			mMessageId = R.string.batch_activity_ui_dialog_no_internet;
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
	 * private method to undertake the upload of data
	 */
	private void doUpload() {
		
		ContentResolver mContentResolver = getContentResolver();
		
		Cursor mNewData = getNewData(mContentResolver);
		
		// check to see if there is any data to upload
		if(mNewData != null) {
			// there is new data to process
			
			// build the JSON data
			BatchUploadTask mBatchTask = new BatchUploadTask(this, mNewData, progressBar, statusText);
			mBatchTask.execute();
			
		} else {
			// there is no new data to process
			showDialog(sNothingToDoDialog);
		}
	}
	
	/*
	 * callback for when the async task completes
	 */
	public void uploadCompleted() {
		
		String[] mProjection = {LogContract.Table._ID};
		String mSelection = LogContract.Table.UPLOAD_STATUS + " != ?";
		String[] mSelectionArgs = {Integer.toString(LogContract.UPLOAD_SUCCESS_FLAG)};
		
		ContentResolver contentResolver = getContentResolver();
		
		Cursor mCursor = contentResolver.query(
				LogContract.CONTENT_URI, 
				mProjection, 
				mSelection,
				mSelectionArgs,
				null);
		
		if(mCursor == null || mCursor.getCount() > 0) {
			Log.w(sTag, "unable to lookup required data");
			showDialog(sUploadFailedDialog);
		} else {
			showDialog(sUploadCompleteDialog);
		}
		
		mCursor.close();
	}
	
	/*
	 * private method to get the new data
	 */
	private Cursor getNewData(ContentResolver contentResolver) {
		
		Cursor mResults = null;
		
		// get the id of the last POI that we uploaded
		String[] mProjection = {"MAX( " + LogContract.Table.POI_ID + ")"};
		String mSelection = null;
		String[] mSelectionArgs = null;
		
		mResults = contentResolver.query(
				LogContract.CONTENT_URI, 
				mProjection,
				null,
				null,
				null);
		
		// use the index of the last uploaded POI in the select for POI data
		if(mResults.getCount() > 0) {
			mResults.moveToFirst();
			
			if(mResults.getInt(0) > 0) {
				mSelection = PointsOfInterestContract.Table._ID + " > ?";
				mSelectionArgs = new String[1];
				mSelectionArgs[0] = mResults.getString(0);
			}
			
			mResults.close();
		}
		
		if(V_LOG) {
			Log.v(sTag, "selection args: " + Arrays.toString(mSelectionArgs));
		}

		// get the POI data
		mResults = contentResolver.query(
				PointsOfInterestContract.CONTENT_URI, 
				null, 
				mSelection, 
				mSelectionArgs, 
				null);
		
		if(mResults != null && mResults.getCount() > 0) {
			if(V_LOG) {
				Log.v(sTag, "poi data cursor count: " + mResults.getCount());
			}
			return mResults;
		} else {
			return null;
		}
	}
}
