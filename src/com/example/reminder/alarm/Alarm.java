package com.example.reminder.alarm;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.example.reminder.R;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public final class Alarm implements Serializable, Parcelable, AlarmInterface.AlarmsColumns{
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;

	/**
     * Alarms start with an invalid id when it hasn't been saved to the database.
     */
    public static final int INVALID_ID = -1;

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
        
    public int id;
    public boolean enabled;
    public int hour;
    public int minutes;
    public DaysOfWeek daysOfWeek;
    public boolean vibrate;
    public String label;
    public String alert;
    

    // Creates a default alarm at the current time.
    public Alarm() {
        this(0, 0);
    }

    public Alarm(int hour, int minutes) {
        this.id = (int) INVALID_ID;
        this.hour = hour;
        this.minutes = minutes;
        this.vibrate = true;
        this.daysOfWeek = new DaysOfWeek(0);
        this.label = "";
        this.alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
    }
    
    public int getId(){
    	return id;
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public int getHour(){
    	return hour;
    }
    
    public void setHour(int hour){
    	this.hour = hour;
    }
    
    public int getMinutes(){
    	return minutes;
    }
    
    public void setMinutes(int minutes){
    	this.minutes = minutes;
    }
    
    public boolean getVibrate(){
    	return vibrate;
    }
    
    public void setVibrate(boolean vibrate){
    	this.vibrate = vibrate;
    }
    
    public DaysOfWeek getDaysOfWeek(){
    	return daysOfWeek;
    }
    
    public void setDaysOfWeek(DaysOfWeek daysOfWeek){
    	this.daysOfWeek = daysOfWeek;
    }
    
    public String getLabel(){
    	return label;
    }
    
    public void setLabel(String label){
    	this.label = label;
    }
    
    public String getAlert(){
    	return alert;
    }
    
    public void setAlert(Uri alert){
    	this.alert = alert.toString();
    }
    
    public void setAlert(String alert){
    	this.alert = alert;
    }
    

    Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        vibrate = p.readInt() == 1;
        label = p.readString();
        alert = p.readString();
    }

    public String getLabelOrDefault(Context context) {
        if (label == null || label.length() == 0) {
            return context.getString(R.string.default_label);
        }
        return label;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeLong(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(daysOfWeek.getBitSet());
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeString(alert);
    }
    
    @Override
    public String toString() {
        return "Alarm{" +
                "alert=" + alert +
                ", id=" + id +
                ", enabled=" + enabled +
                ", hour=" + hour +
                ", minutes=" + minutes +
                ", daysOfWeek=" + daysOfWeek +
                ", vibrate=" + vibrate +
                ", label='" + label + '\'' +
                '}';
    }

    public int describeContents() {
        return 0;
    }

    public AlarmInstance createInstanceAfter(Calendar time) {
        Calendar nextInstanceTime = Calendar.getInstance();
        nextInstanceTime.set(Calendar.YEAR, time.get(Calendar.YEAR));
        nextInstanceTime.set(Calendar.MONTH, time.get(Calendar.MONTH));
        nextInstanceTime.set(Calendar.DAY_OF_MONTH, time.get(Calendar.DAY_OF_MONTH));
        nextInstanceTime.set(Calendar.HOUR_OF_DAY, hour);
        nextInstanceTime.set(Calendar.MINUTE, minutes);
        nextInstanceTime.set(Calendar.SECOND, 0);
        nextInstanceTime.set(Calendar.MILLISECOND, 0);

        // If we are still behind the passed in time, then add a day
        if (nextInstanceTime.getTimeInMillis() <= time.getTimeInMillis()) {
            nextInstanceTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // The day of the week might be invalid, so find next valid one
        int addDays = daysOfWeek.calculateDaysToNextAlarm(nextInstanceTime);
        if (addDays > 0) {
            nextInstanceTime.add(Calendar.DAY_OF_WEEK, addDays);
        }

        AlarmInstance result = new AlarmInstance(nextInstanceTime, id);
        result.mVibrate = vibrate;
        result.mLabel = label;
        result.mRingtone = Uri.parse(alert);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alarm)) return false;
        final Alarm other = (Alarm) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }


}


