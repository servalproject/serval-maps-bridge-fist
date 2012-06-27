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
package org.servalproject.maps.bridge.json;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

/**
 * a basic implementation of the AbstractJsonMaker class
 */
public class BasicJsonMaker {

	/**
	 * take a table row as represented by a ContentValues object and return a JSON encoded string
	 * @param values a ContentValues object representing a row in the database
	 * @return a JSON encoded string of the data
	 * @throws JSONException if the encode to JSON fails
	 */
	public static String makePoiJson(ContentValues values) throws JSONException {
		
		String mResults = null;
		Entry<String, Object> mEntry = null;
		
		JSONObject mJsonObject = new JSONObject();
		
		Set<Entry<String, Object>> mValueSet = values.valueSet();
		
		Iterator<Entry<String, Object>> mIterator = mValueSet.iterator();
		
		 while (mIterator.hasNext()){
			 
			 mEntry = mIterator.next();
			 
			 mJsonObject.put(mEntry.getKey(), mEntry.getValue());
			 
		 }
		 
		 mResults = mJsonObject.toString();
		
		return mResults;
	}
}
