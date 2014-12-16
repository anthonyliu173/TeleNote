package com.example.telenote.checkinternet;

import android.content.Context;
import android.net.ConnectivityManager;

/*
 * CheckInternetStatus checks status of Internet connection
 */

public class CheckInternetStatus {
	
	private Context _context;
	 
    public CheckInternetStatus(Context context) {
        this._context = context;
    }

    
    
	public boolean IsConnected() {
		ConnectivityManager connMgr = (ConnectivityManager)_context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		//For 3G check
		boolean is3g = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
		            .isConnectedOrConnecting();
		//For WiFi Check
		boolean isWifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
		            .isConnectedOrConnecting();

		if (isWifi) {
			return true;
		} else if (is3g) {
			return true;
		} else {
			return false;
		}
	}

}
