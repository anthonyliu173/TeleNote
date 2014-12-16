package com.example.telenote.broadcast;

import com.example.telenote.CallStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/*
 * When phone status changes, PhoneStateReceriver calls PhoneStateMonitor 
 */

public class PhoneStateReceiver extends BroadcastReceiver {
	
	TelephonyManager manager;       
	PhoneStateMonitor phoneStateListener;
	static boolean isAlreadyListening=false;
	CallStatus CS = new CallStatus();
	

	public PhoneStateReceiver() {
		
    }
	
    public void onReceive(Context context, Intent intent) {
		Log.d("CallRecorder", "CallBroadcastReceiver::onReceive got Intent: "
				+ intent.toString());
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String numberToCall = intent
					.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//			System.out.println("拨出");
			PhoneStateMonitor.Number = numberToCall;
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	         
	        System.out.println("Outgoing: "+number);
//	        CS.callStatus = "out";
	        System.out.println("CS status "+CS.callStatus);
			Log.d("CallRecorder",
					"CallBroadcastReceiver intent has EXTRA_PHONE_NUMBER: "
							+ numberToCall);
		} else {
//			System.out.println("接收");
			PhoneStateMonitor phoneListener = new PhoneStateMonitor(context);
			TelephonyManager telephony = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(phoneListener,
					PhoneStateListener.LISTEN_CALL_STATE);
			Log.d("PhoneStateReceiver::onReceive", "set PhoneStateListener");
		}
		



	}

}