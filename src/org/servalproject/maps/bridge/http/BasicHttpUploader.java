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
package org.servalproject.maps.bridge.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import android.text.TextUtils;
import android.util.Log;

/**
 * a basic implementation of a class that undertakes uploading the data 
 */
public class BasicHttpUploader {
	
	/*
	 * private class level constants
	 */
	private static String sTag = "BasicHttpUploader";
	
	public static boolean doBasicPost(String url, String data) {
		
		// check on parameters
		if(TextUtils.isEmpty(url)) {
			throw new IllegalArgumentException("the url parameter is required");
		}
		
		if(TextUtils.isEmpty(data)) {
			throw new IllegalArgumentException("the data parameter is required");
		}
		
		// convert the string url into a url object
		
		URL mPostUrl;
		try {
			mPostUrl = new URL(url);
		} catch (MalformedURLException e) {
			Log.e(sTag, "unable to interpret the URL", e);
			return false;
		}
		
		// encode the parameter
		String mFormParameters;
		try {
			mFormParameters = "jsondata=" + URLEncoder.encode(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(sTag, "unable to encode the data", e);
			return false;
		}
		
		// connect to the website
		HttpURLConnection mConnection;
		try {
			mConnection = (HttpURLConnection) mPostUrl.openConnection();
		} catch (IOException e) {
			Log.e(sTag, "unable to connect to the website", e);
			return false;
		}
		
		// set up some connection parameters
		mConnection.setDoOutput(true);
		try {
			mConnection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			Log.e(sTag, "unable to use the 'POST' request method", e);
			return false;
		}
		
		// set the length of the content
		mConnection.setFixedLengthStreamingMode(mFormParameters.getBytes().length);

		// set the content type of the request
		mConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// send the data
		PrintWriter mPrintWriter;
		try {
			mPrintWriter = new PrintWriter(mConnection.getOutputStream());
			mPrintWriter.print(mFormParameters);
			mPrintWriter.close();
		} catch (IOException e) {
			Log.e(sTag, "unable to upload the data", e);
			return false;
		}
		
		// get the response from the server
		try {
			BufferedReader mBufferedReader = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
			
		    StringBuilder mResponseBuilder = new StringBuilder();
			String mContent = "";
	        
	        while((mContent = mBufferedReader.readLine()) != null) {
	        	mResponseBuilder.append(mContent);
	        }
	        
	        mBufferedReader.close();
	        
	        mContent = mResponseBuilder.toString();
	        
	        if(mContent.contains("upload OK") == true) {
	        	return true;
			} else {
				Log.e(sTag, "upload failed with message: " + mContent);
				return false;
			}
		} catch (IOException e) {
			Log.e(sTag, "unable to read the response from the server", e);
			return false;
		} finally {
			mConnection.disconnect();
		}
	}
}
