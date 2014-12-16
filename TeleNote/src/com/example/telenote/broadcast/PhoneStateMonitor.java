package com.example.telenote.broadcast;

import com.example.telenote.CallStatus;
import com.example.telenote.MainActivity;
import com.example.telenote.fieldservice.CallService;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/*
 * When phone status changes, PhoneStateMonitor is called by PhoneStateReceiver
 * PhoneStateMonitor then calls CallService
 * PhoneStateMonitor passes phone number information as well
 */

public class PhoneStateMonitor extends PhoneStateListener {
	
	Context context;
	static String Number;
	String inout;
	MainActivity MA = new MainActivity();
	CallStatus CS = new CallStatus();
	boolean shouldCheck = true;

	public PhoneStateMonitor(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	// This Method Automatically called when changes is detected in Phone State
	public void onCallStateChanged(int state, String incomingNumber) {

		System.out.println("initial state " + CS.callStatus);
		if (MA.ReturnSettings().contains("Enable")) {
	
				if (incomingNumber.isEmpty()) {
					incomingNumber = Number;
					// }
					System.out.println("123" + CS.callStatus);
					// inout = "out";
					// System.out.println(inout);

				} else {
					
					Number = incomingNumber;
					System.out.println("456" + CS.callStatus);
				}
				
			
		}

		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// if (MA.ReturnSettings().contains("Enable")) {
			System.out.println("CALL_STATE_IDLE");
			Intent callIntents = new Intent(context, CallService.class);
			context.stopService(callIntents);
			CS.callStatus = "out";
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			System.out.println("CALL_STATE_RINGING");
			CS.callStatus = "in";
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			System.out.println("CALL_STATE_OFFHOOK");
			if (MA.Settings.contains("Enable")||MA.Settings.contains("啟用")) {
				//CS.callStatus = "out";
				// System.out.println(MA.Settings);
				Intent callIntent = new Intent(context, CallService.class);
				callIntent.putExtra("CallNumber", incomingNumber);
				callIntent.putExtra("In-N-Out", CS.callStatus);
				context.startService(callIntent);
				// System.out.println("VibrationService Start");
			}
			// context.stopService(callIntent);
			break;
		}
	}
}
