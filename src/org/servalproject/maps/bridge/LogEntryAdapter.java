package org.servalproject.maps.bridge;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DateFormat;

import org.servalproject.maps.bridge.provider.LogContract;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogEntryAdapter extends SimpleCursorAdapter {
	
	/*
	 * private class level variables
	 */
	private String[] from;
	private int[] to;
	
	private Calendar calendar;
	private DateFormat dateformat;

	public LogEntryAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);

		this.from = from;
		this.to = to;
		
		calendar = Calendar.getInstance();
		dateformat = SimpleDateFormat.getDateTimeInstance();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView mTextView;
		
		// populate the text views with data
		for(int i = 0; i < to.length; i++) {
			
			mTextView = (TextView) view.findViewById(to[i]);
			
			if(to[i] == R.id.view_log_ui_entry_status_txt) {
				
				// provide human readable status
				int mStatus = cursor.getInt(cursor.getColumnIndex(from[i]));
				
				switch(mStatus) {
				case LogContract.INVALID_JSON_FLAG:
					mTextView.setText(R.string.system_upload_status_invalid_json);
					break;
				case LogContract.UPLOAD_FAILED_FLAG:
					mTextView.setText(R.string.system_upload_status_upload_failed);
					break;
				case LogContract.UPLOAD_PENDING_FLAG:
					mTextView.setText(R.string.system_upload_status_upload_pending);
					break;
				case LogContract.UPLOAD_SUCCESS_FLAG:
					mTextView.setText(R.string.system_upload_status_upload_sucess);
					break;
				default:
					Log.w("LogEntryAdapter", "unknown status detected '" + mStatus + "'");
					mTextView.setText(Integer.toString(mStatus));
				}
			} else if(to[i] == R.id.view_log_ui_entry_status_last_updated_txt) {
				
				// provide human readable date and time
				calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(from[i])));
				mTextView.setText(dateformat.format(calendar.getTime()));
				
			} else {
				mTextView.setText(cursor.getString(cursor.getColumnIndex(from[i])));
			}
		}
	}
}
