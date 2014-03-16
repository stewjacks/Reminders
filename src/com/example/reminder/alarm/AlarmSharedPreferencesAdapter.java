package com.example.reminder.alarm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.example.reminder.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AlarmSharedPreferencesAdapter {
    private static final int LIST_EMPTY = 0;
	private static final int LIST_POPULATED = 1;
	private final Context context;
    
    private SharedPreferences sp;
    public ArrayList<Alarm> reminders;
    
    public AlarmSharedPreferencesAdapter(Context context){
    	this.context = context;
    	this.reminders = new ArrayList<Alarm>();
    }
  
	public void checkSharedPreferences(){
    	sp = PreferenceManager.getDefaultSharedPreferences(context);
    	reminders = new ArrayList<Alarm>();
    	
    	reminders = (ArrayList<Alarm>) ObjectSerializer.deserialize(context, Constants.ALARM_FILENAME);
    }
    
    public boolean saveSharedPreferences(ArrayList<Alarm> reminders){
		ObjectSerializer.serialize(context, reminders, Constants.ALARM_FILENAME);
		return true;
    }
    
    public int listState(){
    	if(reminders == null || reminders.size() == 0){
    		return LIST_EMPTY;
    	}else
    		return LIST_POPULATED;
    	}
    
    public void populateList(){
    	for (int i = 0; i < reminders.size(); i++){
    		
    	}
    }


}
