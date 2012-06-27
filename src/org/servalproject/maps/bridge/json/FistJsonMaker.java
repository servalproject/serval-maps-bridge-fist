/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Maps - FIST Bridge
 *
 * Serval Maps - FIST Bridge Software is free software; you can redistribute 
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
package org.servalproject.maps.bridge.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.servalproject.maps.PointsOfInterestContract;

import android.content.ContentValues;

public class FistJsonMaker {
	
	/*
	 * private class level constants
	 */
	private static final String sImei = "100000000000001";
	private static final int sMessageCode = 3;
	
	private static final String[] sAddresses = {"2078005752244","support@delorme.com","maps@servalproject.org"};
	
	private static final int sMissingPointValues = 0;
	
	private static final int sMissingStatusValues = 0;
	
	/**
	 * take a table row as represented by a ContentValues object and return a JSON encoded string
	 * @param values a ContentValues object representing a row in the database
	 * @return a JSON encoded string of the data
	 * @throws JSONException if the encode to JSON fails
	 */
	public static String makePoiJson(ContentValues values) throws JSONException {
		
		// construct the JSON objects / arrays
		
		//add boilerplate to the container
		JSONObject mContainer = new JSONObject();
		mContainer.put("Version", "1.0");
		
		// build the event object
		JSONObject mEvent = new JSONObject();
		mEvent.put("imei", sImei);
		mEvent.put("messageCode", sMessageCode);
		mEvent.put("freeText", values.get(PointsOfInterestContract.Table.TITLE) + "<br/>" + values.get(PointsOfInterestContract.Table.DESCRIPTION));
		mEvent.put("timeStamp", values.get(PointsOfInterestContract.Table.TIMESTAMP));
		
		// build the addresses array
		JSONArray mAddresses = new JSONArray();
		
		for(String mAddress : sAddresses) {
			mAddresses.put(new JSONObject().put("address", mAddress));
		};
		
		mEvent.put("addresses", mAddresses);
		
		// build the point object
		JSONObject mPoint = new JSONObject();
		mPoint.put("latitude", values.get(PointsOfInterestContract.Table.LATITUDE));
		mPoint.put("longitude", values.get(PointsOfInterestContract.Table.LONGITUDE));
		mPoint.put("altitude", values.get(PointsOfInterestContract.Table.ALTITUDE));
		mPoint.put("gpsfix", values.get(PointsOfInterestContract.Table.ACCURACY));
		mPoint.put("course", sMissingPointValues);
		mPoint.put("speed", sMissingPointValues);
		
		// add the point object
		mEvent.put("point", mPoint);
		
		// build the status object
		JSONObject mStatus = new JSONObject();
		mStatus.put("autonomous", sMissingStatusValues);
		mStatus.put("lowBattery", sMissingStatusValues);
		mStatus.put("intervalChange", sMissingStatusValues);
		
		mEvent.put("status", mStatus);
		
		// build the events array
		JSONArray mEvents = new JSONArray();
		mEvents.put(mEvent);
		
		// finalise the object
		mContainer.put("Events", mEvents);
		
		return mContainer.toString();
	}
}
