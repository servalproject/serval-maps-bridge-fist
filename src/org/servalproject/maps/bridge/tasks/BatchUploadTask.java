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
package org.servalproject.maps.bridge.tasks;

import org.json.JSONException;
import org.servalproject.maps.PointsOfInterestContract;
import org.servalproject.maps.bridge.BatchActivity;
import org.servalproject.maps.bridge.fist.R;
import org.servalproject.maps.bridge.http.BasicHttpUploader;
import org.servalproject.maps.bridge.json.BasicJsonMaker;
import org.servalproject.maps.bridge.provider.LogContract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * undertakes the task of preparing the JSON data for upload
 */
public class BatchUploadTask extends AsyncTask<Void, Integer, Void> {

	/*
	 * private class level constants
	 */
	private final boolean V_LOG = true;
	private static final String  sTag = "PrepareJsonTask";

	/*
	 * private class level variables
	 */
	private ContentResolver contentResolver;
	private Cursor        cursor;
	private ProgressBar   progressBar;
	private TextView      textView;
	private BatchActivity context;

	private static volatile boolean updateUi = false;

	public BatchUploadTask(BatchActivity context, Cursor cursor, ProgressBar progressBar, TextView textView) {
		this.context = context;
		this.contentResolver = context.getContentResolver();
		this.cursor = cursor;
		this.progressBar = progressBar;
		this.textView = textView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {

		if(V_LOG) {
			Log.v(sTag, "onPreExecute called");
		}

		progressBar.setMax(cursor.getCount());
		progressBar.setVisibility(View.VISIBLE);

		// set the text for preparing JSON
		textView.setText(R.string.batch_activity_ui_status_json_prepare);
		textView.setVisibility(View.VISIBLE);

	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {

		if(V_LOG) {
			Log.v(sTag, "onProgressUpdate called: " + progress[0].toString());
		}

		// update the progress bar
		super.onProgressUpdate(progress[0]);

		progressBar.setProgress(progress[0]);

		if(updateUi) {
			textView.setText(R.string.batch_activity_ui_status_json_upload);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void param) {

		if(V_LOG) {
			Log.v(sTag, "onPostExecute called: ");
		}

		// finalise the results
		progressBar.setVisibility(View.INVISIBLE);
		progressBar.setProgress(0);

		textView.setVisibility(View.INVISIBLE);

		// callback to activity
		context.uploadCompleted();
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void...params) {

		// prepare the JSON
		prepareJson();

		uploadJson();

		return null;

	}

	/*
	 * private method to build the JSON data
	 */
	private void prepareJson() {

		String mJsonData = null;
		ContentValues mContentValues;


		// loop through the cursor
		while(cursor.moveToNext()) {

			mContentValues = new ContentValues();

			DatabaseUtils.cursorRowToContentValues(cursor, mContentValues);

			// convert the data to JSON
			try {
				mJsonData = BasicJsonMaker.makePoiJson(mContentValues);
			} catch (JSONException e) {
				Log.e(sTag, "unable to encode JSON data for '" + cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table._ID)), e);
				mJsonData = null;
			}

			// build the log entry
			mContentValues = new ContentValues();
			mContentValues.put(LogContract.Table.POI_ID, cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table._ID)));
			mContentValues.put(LogContract.Table.POI_TITLE, cursor.getString(cursor.getColumnIndex(PointsOfInterestContract.Table.TITLE)));
			mContentValues.put(LogContract.Table.JSON_CONTENT, mJsonData);

			// add the appropriate upload flag
			if(mJsonData == null) {
				mContentValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.INVALID_JSON_FLAG);
			} else {
				mContentValues.put(LogContract.Table.UPLOAD_STATUS, LogContract.UPLOAD_PENDING_FLAG);
			}

			mContentValues.put(LogContract.Table.TIMESTAMP, System.currentTimeMillis());

			// add the table row
			contentResolver.insert(LogContract.CONTENT_URI, mContentValues);

			// update the ui
			publishProgress(cursor.getPosition());
		}

		cursor.close();
	}

	/*
	 * private method to upload the JSON
	 */
	private void uploadJson() {

		// update the display
		updateUi = true;
		publishProgress(0);

		// get the upload URL
		SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String uploadUrl = mPreferences.getString("preferences_upload_url", null);
		mPreferences = null;

		boolean mUploadStatus = false;

		// get the list of stuff to upload
		String[] mProjection = new String[2];
		mProjection[0] = LogContract.Table._ID;
		mProjection[1] = LogContract.Table.JSON_CONTENT;

		String mSelection = LogContract.Table.UPLOAD_STATUS + " = ?";
		String mSelectionArgs[] = new String[1];
		mSelectionArgs[0] = Integer.toString(LogContract.UPLOAD_PENDING_FLAG);

		ContentValues mUpdateValues;

		Cursor mCursor = contentResolver.query(
				LogContract.CONTENT_URI,
				mProjection,
				mSelection,
				mSelectionArgs,
				null);

		// loop through the cursor
		while(mCursor.moveToNext()) {

			if(V_LOG) {
				Log.v(sTag, "attempting upload of record: " + mCursor.getString(mCursor.getColumnIndex(LogContract.Table._ID)));
			}

			mUploadStatus = BasicHttpUploader.doBasicPost(
					uploadUrl, 
					mCursor.getString(mCursor.getColumnIndex(mProjection[1]))
					);

			// update the selection criteria for the update
			mSelection = LogContract.Table._ID + " = ?";
			mSelectionArgs = new String[1];
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
			contentResolver.update(
					LogContract.CONTENT_URI, 
					mUpdateValues,
					mSelection, 
					mSelectionArgs);
			
			publishProgress(mCursor.getPosition());
		}

		mCursor.close();

	}
}
