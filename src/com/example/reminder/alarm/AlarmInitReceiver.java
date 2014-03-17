package com.example.reminder.alarm;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.reminder.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;

public class AlarmInitReceiver extends BroadcastReceiver {

    // A flag that indicates that switching the volume button default was done
    private static final String PREF_VOLUME_DEF_DONE = "vol_def_done";

    /**
     * Sets next alarm on ACTION_BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED,
     * after another alarm fires, and whenever settings are changed.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();

        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
            	
            	//get an instance of alarms
            	@SuppressWarnings("unchecked")
				ArrayList<Alarm> reminders = (ArrayList<Alarm>) ObjectSerializer.deserialize(context, Constants.ALARM_FILENAME);
            	
            	Calendar schedule = AlarmUtils.getNextAlarm(reminders);
            	if (schedule!=null){
        			Intent intent = new Intent(context, AlarmService.class);
        			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
        					Constants.ALARM_INTENT_CODE, intent, 0);
                	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                		alarmManager.setExact(AlarmManager.RTC_WAKEUP, schedule.getTimeInMillis(),
                				pendingIntent);
                	}else{
                		alarmManager.set(AlarmManager.RTC_WAKEUP, schedule.getTimeInMillis(),
                				pendingIntent);
                	}
        		}
        	}
        });
        
        result.finish();
        wl.release();
    }

}
