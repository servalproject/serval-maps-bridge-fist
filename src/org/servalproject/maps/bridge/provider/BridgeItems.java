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
package org.servalproject.maps.bridge.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * defines a content provider for the Serval Maps Bridge database
 */
public class BridgeItems extends ContentProvider {
	
	/**
	 * authority string for the content provider
	 */
	public static final String AUTHORITY = "org.servalproject.maps.bridge.fist.provider.items";
	
	// private class level constants
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	private static final int sLogListUri = 0;
	private static final int sLogItemUri = 1;
	
	private static final String sTag = "BridgeItems";
	
	/*
	 * private class level variables
	 */
	private MainDatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		
		// define URis that we'll match against
		sUriMatcher.addURI(AUTHORITY, LogContract.CONTENT_URI_PATH, sLogListUri);
		sUriMatcher.addURI(AUTHORITY, LogContract.CONTENT_URI_PATH + "/#", sLogItemUri);
		
		// create the database connection
		databaseHelper = new MainDatabaseHelper(getContext());
		
		return true;
	}
	
	/*
	 * execute a query
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		int mMatchedUri = -1;
		Cursor mResults = null;
		
		// choose the table name and sort order based on the URI
		switch(sUriMatcher.match(uri)) {
		case sLogListUri:
			// uri matches all of the table
			if(TextUtils.isEmpty(sortOrder) == true) {
				sortOrder = LogContract.Table._ID + " ASC";
			}
			mMatchedUri = sLogListUri;
			break;
		case sLogItemUri:
			// uri matches one record
			if(TextUtils.isEmpty(selection) == true) {
				selection = LogContract.Table._ID + " = " + uri.getLastPathSegment();
			} else {
				selection += " AND " + LogContract.Table._ID + " = " + uri.getLastPathSegment();
			}
			mMatchedUri = sLogItemUri;
			break;
		default:
			// unknown uri found
			Log.e(sTag, "unknown URI detected on query: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getReadableDatabase();
		
		if(mMatchedUri == sLogListUri || mMatchedUri == sLogItemUri) {
			// execute the query as provided
			mResults = database.query(LogContract.Table.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		}
		
		// return the results
		return mResults;
	}
	
	/*
	 * insert data into the database
	 * 
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		
		Uri mResults = null;
		String mTable = null;
		Uri mContentUri = null;
		
		// chose the table name
		switch(sUriMatcher.match(uri)) {
		case sLogListUri:
			mTable = LogContract.Table.TABLE_NAME;
			mContentUri = LogContract.CONTENT_URI;
			break;
		default:
			// unknown uri found
			Log.e(sTag, "unknown URI detected on insert: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		
		long mId = database.insertOrThrow(mTable, null, values);
		
		// play nice and tidy up
		database.close();
		
		mResults = ContentUris.withAppendedId(mContentUri, mId);
		getContext().getContentResolver().notifyChange(mResults, null);
		
		return mResults;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public synchronized String getType(Uri uri) {
		
		// choose the table name and sort order based on the URI
		switch(sUriMatcher.match(uri)) {
		case sLogListUri:
			return LogContract.CONTENT_TYPE_LIST;
		case sLogItemUri:
			return LogContract.CONTENT_TYPE_ITEM;
		default:
			// unknown uri found
			Log.e(sTag, "unknown URI detected on getType: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		int count;
		
		// determine what type of delete is required
		switch(sUriMatcher.match(uri)) {
		case sLogListUri:
			count = database.delete(LogContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		case sLogItemUri:
			if(TextUtils.isEmpty(selection) == true) {
				selection = LogContract.Table._ID + " = ?";
				selectionArgs = new String[0];
				selectionArgs[0] = uri.getLastPathSegment();
			}
			count = database.delete(LogContract.Table.TABLE_NAME, selection, selectionArgs);
			break;
		default:
			// unknown uri found
			Log.e(sTag, "unknown URI detected on delete: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
		
	}


	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		int count;
		
		// determine what type of delete is required
		switch(sUriMatcher.match(uri)) {
		case sLogListUri:
			count = database.update(LogContract.Table.TABLE_NAME, values, selection, selectionArgs);
			break;
		case sLogItemUri:
			if(TextUtils.isEmpty(selection) == true) {
				selection = LogContract.Table._ID + " = ?";
				selectionArgs = new String[0];
				selectionArgs[0] = uri.getLastPathSegment();
			}
			count = database.update(LogContract.Table.TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			// unknown uri found
			Log.e(sTag, "unknown URI detected on delete: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
		
	}
}
