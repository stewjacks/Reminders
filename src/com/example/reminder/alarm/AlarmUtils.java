/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.example.reminder.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

import com.example.reminder.Constants;
import com.example.reminder.ReminderFragment.AlarmItemAdapter.ItemHolder;

import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

/**
 * Static utility methods for Alarms.
 */
public class AlarmUtils {
    public static final String FRAG_TAG_TIME_PICKER = "time_dialog";
    
    public static String getFormattedTime(Context context, Calendar time) {
        String skeleton = DateFormat.is24HourFormat(context) ? "EHm" : "Ehma";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        return (String) DateFormat.format(pattern, time);
    }

    public static void showTimeEditDialog(FragmentManager manager, final Alarm alarm,
            TimePickerDialog.OnTimeSetListener listener, boolean is24HourMode) {

        int hour, minutes;
        if (alarm == null) {
            hour = 0; minutes = 0;
        } else {
            hour = alarm.hour;
            minutes = alarm.minutes;
        }
        

    	Log.d("AlarmUtils", ""+hour+":"+minutes);
        
    	TimePickerFragment df = new TimePickerFragment(hour, minutes, listener);


        // Make sure the dialog isn't already added.
        manager.executePendingTransactions();
        final FragmentTransaction ft = manager.beginTransaction();
        final Fragment prev = manager.findFragmentByTag(FRAG_TAG_TIME_PICKER);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.commit();
        
        df.show(manager, "timePicker");
    }
    
    private static Calendar getAlarmTime(Alarm alarm) {
       
    	Calendar alarmCalendar = Calendar.getInstance();
    	int addDays = alarm.daysOfWeek.calculateDaysToNextAlarm(alarmCalendar);
    	if (addDays>0){
    		alarmCalendar.add(Calendar.DAY_OF_WEEK, addDays);
    	}
    	
    	alarmCalendar.set(Calendar.HOUR_OF_DAY, alarm.hour);
    	alarmCalendar.set(Calendar.MINUTE, alarm.minutes);
    	alarmCalendar.set(Calendar.MILLISECOND, 0);
    	
        return alarmCalendar;
    }
    
    /**
     * Get the closest calendar for AlarmManager to schedule
     * @param reminders
     * @return
     */
    
	public static Calendar getNextAlarm(ArrayList<Alarm> reminders){
		Calendar bestCalendar = null;
		for (Alarm alarm : reminders){
    		if(alarm.enabled){
    			Calendar c = getAlarmTime(alarm);
    			if (bestCalendar == null || 
    					c.getTimeInMillis() < bestCalendar.getTimeInMillis()){ 
    				bestCalendar = c;
    			}
    		}	
    	}
		return bestCalendar;
	}

}
