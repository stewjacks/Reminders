package com.example.reminder.alarm;

import java.util.ArrayList;
import java.util.Calendar;

import com.example.reminder.Constants;
import com.example.reminder.R;
import com.example.reminder.alarm.AlarmUtils.AlarmCalendarObject;

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
import android.util.Log;
import android.widget.Toast;

public class AlarmInitReceiver extends BroadcastReceiver {

    // A flag that indicates that switching the volume button default was done
    private static final String PREF_VOLUME_DEF_DONE = "vol_def_done";
	protected static final String TAG = "AlarmInitReceiver";

    /**
     * Sets next alarm on ACTION_BOOT_COMPLETED, TIME_SET, TIMEZONE_CHANGED,
     * after another alarm fires, and whenever settings are changed.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
    	scheduleAlarm(context);
//        final PendingResult result = goAsync();
//        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
//        wl.acquire();
//        
//        AsyncHandler.post(new Runnable() {
//            @Override public void run() {
            	
            	//get an instance of alarms

    }
    	
    static void scheduleAlarm(Context context){
    	@SuppressWarnings("unchecked")
		ArrayList<Alarm> reminders = (ArrayList<Alarm>) ObjectSerializer
		.deserialize(context, Constants.ALARM_FILENAME);
    	
    	AlarmCalendarObject schedule = AlarmUtils.getNextAlarm(reminders);
    	if (schedule.getAlarm()!=null && schedule.getCalendar()!=null){
			Intent alarmIntent = new Intent(context, AlarmReceiver.class);
			String s = schedule.getAlarm().getLabel();
			alarmIntent.putExtra(Constants.ALARM_TITLE_INTENT, 
					s);
			
			//testing
			Calendar test = Calendar.getInstance();
			Toast.makeText(context, "Next alarm in: "+(schedule.getCalendar().getTimeInMillis()/1000/60/60/24 - 
					test.getTimeInMillis()/1000/60/60/24) + " days", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Alarm in: "+(schedule.getCalendar().getTimeInMillis() - 
					test.getTimeInMillis())/1000+ " seconds");
			//

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
					Constants.ALARM_INTENT_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        	AlarmManager alarmManager = (AlarmManager) context.getSystemService(
        			Context.ALARM_SERVICE);
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        		alarmManager.setExact(AlarmManager.RTC_WAKEUP, schedule.getCalendar().getTimeInMillis(),
        				pendingIntent);
        	}else{
        		alarmManager.set(AlarmManager.RTC_WAKEUP, schedule.getCalendar().getTimeInMillis(),
        				pendingIntent);
        	}
        	
        	//this will update alarms to disabled that should only run once. 
        	if(schedule.getAlarm()!=null && !schedule.getEnabled()){
        		if(schedule.getIndex()!=-1){
        			schedule.getAlarm().enabled = false;
        			reminders.set(schedule.getIndex(), schedule.getAlarm());
        			ObjectSerializer.serialize(context, reminders, Constants.ALARM_FILENAME);
        		}
        	}
        	
		}
    }

}
