package org.servalproject.maps.bridge.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * a class exposing common utility methods related to http
 */
public class HttpUtils {
	
	/**
	 * check to see if the device is online, ie. has a valid Internet connection
	 * @param context a context used to gain access to system resources
	 * 
	 * @return true if there is an Internet connection, false if there isn't
	 */
	public static boolean isOnline(Context context) {
		
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo[] mNetworkInfos = mConnectivityManager.getAllNetworkInfo();
		
		if(mNetworkInfos == null) {
			return false;
		}
		
		for(NetworkInfo mInfo : mNetworkInfos) {
			
			if(mInfo.isConnected() == true) {
				return true;
			}
			
		}
		
		return false;	
	}
}