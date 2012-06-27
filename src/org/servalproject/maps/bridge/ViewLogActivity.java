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

import org.servalproject.maps.bridge.fist.R;
import org.servalproject.maps.bridge.provider.LogContract;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * an activity that allows the user to view the upload log
 */
public class ViewLogActivity extends ListActivity implements OnItemClickListener {
	
	/*
	 * private class level constants
	 */
	private final String  sTag = "ViewLogActivity";
	
	/*
	 * private class level variables
	 */
	private Cursor cursor;
	private String[] columnNames;
	private int[] layoutElements;
	private ListView listView;
	
	LogEntryAdapter dataAdapter;
	
	/*
	 * create the activity
	 * 
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_log);
        
        // get the data
        cursor = getCursor();
        
        if(cursor == null) {
			Log.w(sTag, "a null cursor was returned when looking up log entries");
			Toast.makeText(getApplicationContext(), R.string.view_log_ui_toast_no_data, Toast.LENGTH_LONG).show();
			finish();
		}
        
        // get a data adapter
        dataAdapter = getDataAdapter(cursor);
        
        // use the data adapter with this activity
        setListAdapter(dataAdapter);
        
        // listen for touching on list items
		// get a reference to the list view
		listView = getListView();

		listView.setOnItemClickListener(this);
	}
	
	/*
	 * private function to get the data to display
	 */
	private Cursor getCursor() {
		
		ContentResolver mContentResolver = getContentResolver();
		
		// determine the order by
		String mOrderBy = LogContract.Table.TIMESTAMP + " DESC";
		
		return mContentResolver.query(
				LogContract.CONTENT_URI,
				null,
				null,
				null,
				mOrderBy);
	}
	
	/*
	 * private function to configure the data adapter
	 */
	/*
	 * get a populated data datapter
	 */
	private LogEntryAdapter getDataAdapter(Cursor cursor) { 
		
		// define the column and layout mapping
        columnNames = new String[3];
        columnNames[0] = LogContract.Table.POI_TITLE;
        columnNames[1] = LogContract.Table.TIMESTAMP;
        columnNames[2] = LogContract.Table.UPLOAD_STATUS;
        
        layoutElements = new int[3];
        layoutElements[0] = R.id.view_log_ui_entry_title;
        layoutElements[1] = R.id.view_log_ui_entry_status_last_updated_txt;
        layoutElements[2] = R.id.view_log_ui_entry_status_txt;
        
        // get a data adapter
        return new LogEntryAdapter(
        		this,
        		R.layout.view_log_entry,
        		cursor,
        		columnNames,
        		layoutElements);
		
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		
		// play nice and close the cursor
		cursor.close();
		cursor = null;
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		
		// get the data
		cursor = getCursor();
		super.onResume();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// play nice and close the cursor if necessary
		if(cursor != null) {
			cursor.close();
			cursor = null;
		}
		
		super.onDestroy();
	}

}
