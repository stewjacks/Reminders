package com.example.reminder.alarm;

import com.example.reminder.Constants;
import com.example.reminder.MainActivity;
import com.example.reminder.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AlarmService extends Service {
    private static final int mId = 0;
	private static final String TAG = "AlarmService";
	private TelephonyManager mTelephonyManager;
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        
        //this won't work because the intent 
		String title = intent.getStringExtra(Constants.ALARM_TITLE_INTENT);
		
		mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		//Sound the alarm. Also check whether or not someone's in a call for volume
		Log.d(TAG, "in call: "+ (mTelephonyManager.getCallState()!=TelephonyManager.CALL_STATE_IDLE));
		AlarmKlaxon.start(getApplicationContext(), 
				(mTelephonyManager.getCallState()!=TelephonyManager.CALL_STATE_IDLE));
	    
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.splash_cyclist)
				.setContentTitle(title==null?getString(R.string.default_label):title)
				.setContentText(getString(R.string.notification_message))
				.setAutoCancel(true);
		Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(notificationIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());
		sendAlarmUpdate();
		stopSelf();
	}
	
	private void sendAlarmUpdate(){
		Intent intent = new Intent();
		intent.setAction(Constants.ALARM_UPDATE_BROADCAST);
		this.sendBroadcast(intent);
//		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	@Override
	public void onDestroy(){
		AlarmAlertWakeLock.releaseCpuLock();
		Log.d(TAG, "inOnDestroy");
		super.onDestroy();
	}

}
