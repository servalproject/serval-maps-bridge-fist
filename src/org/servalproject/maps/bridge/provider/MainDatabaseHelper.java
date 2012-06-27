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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * main class that opens and manages the SQLite database
 */
public class MainDatabaseHelper extends SQLiteOpenHelper {
	
	/*
	 * declare private class level constants
	 */
	private static final String LOG_CREATE = "CREATE TABLE "
			+ LogContract.Table.TABLE_NAME + "( "
			+ LogContract.Table._ID + " INTEGER PRIMARY KEY, "
			+ LogContract.Table.POI_ID + " INTEGER UNIQUE, "
			+ LogContract.Table.POI_TITLE + " TEXT, "
			+ LogContract.Table.JSON_CONTENT + " TEXT, "
			+ LogContract.Table.TIMESTAMP + " INTEGER, "
			+ LogContract.Table.UPLOAD_STATUS + " INTEGER DEFAULT " + LogContract.UPLOAD_PENDING_FLAG + ")";
	
	/*
	 * declare public class level constants
	 */
	/**
	 * the name of the database file
	 */ 
	public static final String DB_NAME = "serval-maps-bridge.db";
	
	/**
	 * the version of the database file
	 */
	public static final int DB_VERSION = 1;
	
	/**
	 * Constructs a new MainDatabaseHelper object
	 * 
	 * @param context the context in which the database should be constructed
	 */
	MainDatabaseHelper(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);
	}
		
	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the database tables
		db.execSQL(LOG_CREATE);
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO add onUpgrade code if DB tables change once multiple release version are in the wild
	}
}
