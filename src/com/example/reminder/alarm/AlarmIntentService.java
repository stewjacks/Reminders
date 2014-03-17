package com.example.reminder.alarm;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class AlarmIntentService extends IntentService {
	
	
	
	public AlarmIntentService(String name) {
		super(name);
	}
	
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (state != TelephonyManager.CALL_STATE_IDLE) {
            	
            }
        }
    };

	@Override
	protected void onHandleIntent(Intent intent) {
		
	    
		// TODO Auto-generated method stub
		
	}

}
