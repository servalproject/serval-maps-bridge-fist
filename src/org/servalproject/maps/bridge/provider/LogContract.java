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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * defines the contract for the Log table
 */
public class LogContract {
	
	/**
	 * path component of the URI
	 */
	public static final String CONTENT_URI_PATH = "poi";
	
	/**
	 * content URI for the poi log data
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + BridgeItems.AUTHORITY + "/" + CONTENT_URI_PATH);

	/**
	 * content type for a list of items
	 */
	public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd." + BridgeItems.AUTHORITY + "." + CONTENT_URI_PATH;
	
	/**
	 * content type for an individual item
	 */
	public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd." + BridgeItems.AUTHORITY + "." + CONTENT_URI_PATH;
	
	/**
	 * flag to indicate that upload is pending
	 */
	public static final int UPLOAD_PENDING_FLAG = 0;
	
	/**
	 * flag to indicate that the upload has occurred
	 */
	public static final int UPLOAD_SUCCESS_FLAG = 1;
	
	/**
	 * flag to indicate that the upload failed
	 */
	public static final int UPLOAD_FAILED_FLAG = 2;
	
	/**
	 * flag to indicate invalid JSON
	 */
	public static final int INVALID_JSON_FLAG = 3;
	
	/**
	 * table definition
	 */
	public static final class Table implements BaseColumns {
		
		/**
		 * table name
		 */
		public static final String TABLE_NAME = LogContract.CONTENT_URI_PATH;
		
		/**
		 * unique id column
		 */
		public static final String _ID = BaseColumns._ID;
		
		/**
		 * unique POI id
		 */
		public static final String POI_ID = "poi_id";
		
		/**
		 * title of the POI
		 */
		public static final String POI_TITLE = "poi_title";
		
		/**
		 * content of the JSON that was uploaded
		 */
		public static final String JSON_CONTENT = "json_content";
		
		/**
		 * timestamp of the last activity for this record
		 */
		public static final String TIMESTAMP = "timestamp";
		
		/**
		 * indicates if an upload was a success
		 */
		public static final String UPLOAD_STATUS = "upload_status";
	}
}
