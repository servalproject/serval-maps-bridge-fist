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

import org.json.JSONException;
import org.servalproject.maps.PointsOfInterestContract;
import org.servalproject.maps.bridge.http.BasicHttpUploader;
import org.servalproject.maps.bridge.json.BasicJsonMaker;
import org.servalproject.maps.bridge.provider.LogContract;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * receive broadcast intents about new POIs and automatically upload them
 */
public class AutoUploadReceiver extends BroadcastReceiver {
	
	/*
	 * private class level constants
	 */
	private static String sTag = "AutoUploadReceiver";
	private static boolean V_LOG = true;

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// check on the intent action
		if(intent.getAction().equals("org.servalproject.maps.NEW_POI_RECORD") == true) {
			// potentially process a new POI record
			
			if(V_LOG) {
				Log.v(sTag, "receiver called");
			}
			
			// check to see if we should be doing auto uploads
			SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			
			boolean mProceed = mPreferences.getBoolean("preferences_auto_upload", false);
			
			if(mProceed) {
				
				String mUploadUrl = mPreferences.getString("preferences_upload_url", null);
				
				if(TextUtils.isEmpty(mUploadUrl)) {
					Log.w(sTag, "called but upload url preference is missing");
				} else {
					doUpload(context, intent, mUploadUrl);
				}
				
			} else {
				Log.w(sTag, "called but preference disabled auto upload");
			}
		} else {
			Log.w(sTag, "called with the wrong action '" + intent.getAction() + "'");
		}
	}
	
	/*
	 * private function to do the upload
	 */
	private void doUpload(Context context, Intent intent, String uploadUrl){
		
		// get the URI of the new POI
		Uri mNewPoiUri = (Uri) intent.getParcelableExtra("uri");
		
		if(mNewPoiUri == null) {
			Log.w(sTag, "missing uri data in intent");
			return;
		}
		
		// get the poi data
		ContentResolver mContentResolver = context.getContentResolver();
		
		Cursor mCursor = mContentResolver.query(
				mNewPoiUri,
				null, 
				null,
				null, 
				null);
		
		// check on what was returned
		if(mCursor == null) {
			Log.w(sTag, "unable to lookup new POI details");
			return;
		}
		
		if(mCursor.getCount() == 0) {
			Log.w(sTag, "unable to lookup new POI details");
			return;
		}
		
		if(!mCursor.moveToFirst()) {
			
			Log.w(sTag, "unable to lookup new POI details");
			return;
		}
			
		/*
		 * TODO refactor this code to make it more portable
		 * including the same code in the BatchUploadTask class
		 */
		
		ContentValues mContentValues = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(mCursor, mContentValues);
		
		String mJsonData = null;

		// convert the data to JSON
		try {
			mJsonData = BasicJsonMaker.makePoiJson(mContentValues);
		} catch (JSONException e) {
			Log.e(sTag, "unable to encode JSON data for '" + mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table._ID)), e);
			mJsonData = null;
		}

		// build the log entry
		mContentValues = new ContentValues();
		mContentValues.put(LogContract.Table.POI_ID, mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table._ID)));
		mContentValues.put(LogContract.Table.POI_TITLE, mCursor.getString(mCursor.getColumnIndex(PointsOfInterestContract.Table.TITLE)));
		mContentValues.put(LogContract.Table.JSON_CONTENT, mJsonData);

		// add the appropriate upload flag
		if(mJsonData == null) {
			mContentValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.INVALID_JSON_FLAG);
		} else {
			mContentValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.UPLOAD_PENDING_FLAG);
		}

		mContentValues.put(LogContract.Table.TIMESTAMP, System.currentTimeMillis());

		// add the table row
		Uri mNewLogRecord = mContentResolver.insert(LogContract.CONTENT_URI, mContentValues);
		
		/*
		 * upload the new record
		 */
		
		ContentValues mUpdateValues;
		
		String[] mProjection = new String[2];
		mProjection[0] = LogContract.Table._ID;
		mProjection[1] = LogContract.Table.JSON_CONTENT;

		mCursor = mContentResolver.query(
				mNewLogRecord, 
				mProjection, 
				null, 
				null,
				null);
		
		if(mCursor == null || mCursor.getCount() == 0 || mCursor.moveToFirst() == false) {
			Log.w(sTag, "unable to lookup new log entry");
			return;
		}
		
		boolean mUploadStatus = BasicHttpUploader.doBasicPost(
				uploadUrl, 
				mCursor.getString(mCursor.getColumnIndex(mProjection[1]))
				);

		// define selection criteria for the update
		String mSelection = LogContract.Table._ID + " = ?";
		String[] mSelectionArgs = new String[1];
		mSelectionArgs[0] = mCursor.getString(mCursor.getColumnIndex(LogContract.Table._ID));

		if(mUploadStatus) {
			// the upload was a success
			mUpdateValues = new ContentValues();
			mUpdateValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.UPLOAD_SUCCESS_FLAG);
			mUpdateValues.put(LogContract.Table.TIMESTAMP, System.currentTimeMillis());
		} else {
			// the upload was a failure
			mUpdateValues = new ContentValues();
			mUpdateValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.UPLOAD_FAILED_FLAG);
			mUpdateValues.put(LogContract.Table.TIMESTAMP, System.currentTimeMillis());
		}

		// update the log entry
		mContentResolver.update(
				LogContract.CONTENT_URI, 
				mUpdateValues,
				mSelection, 
				mSelectionArgs);
	}
}
