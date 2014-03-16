package com.example.reminder.alarm;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimePickerFragment extends DialogFragment{

	protected static final String TAG = "TimePickerFragment";
	private int hour;
	private int minute;
	TimePickerDialog.OnTimeSetListener listener;
	TimePickerDialog.OnTimeSetListener l;

	protected boolean set = false;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        this.listener = (OnTimeSetListener) activity;
    }
    
    @Override
    public void onDetach() {
        this.listener = null;
        super.onDetach();
    }
	
	public TimePickerFragment(int hour, int minute, 
			TimePickerDialog.OnTimeSetListener listener){
		this.hour = hour;
		this.minute = minute;
		this.listener = listener;
	}
	
	@TargetApi(11)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		if(this.hour==0&&this.minute==0){
			final Calendar c = Calendar.getInstance();
			this.hour = c.get(Calendar.HOUR_OF_DAY);
			this.minute = c.get(Calendar.MINUTE);
		}
		
		l = new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Log.d(TAG, ""+hourOfDay+":"+minute);
				if (set)
					listener.onTimeSet(view, hourOfDay, minute);
			}
		};

		//this is the timepicker that will return to the activity
		final TimePickerDialog picker = new TimePickerDialog(
				getActivity(), l, this.hour, this.minute, 
				DateFormat.is24HourFormat(getActivity()));
		
		if(hasJellyBeanAndAbove()) {
			picker.setButton(DialogInterface.BUTTON_POSITIVE, 
					getActivity().getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	set = true;
                }
            });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                //don't notify the listener.
                public void onClick(DialogInterface dialog, int which) {
                	set = false;
                }
            });
        }
        return picker;
	}
	
    private static boolean hasJellyBeanAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
    private OnTimeSetListener getConstructorListener(){
    	return hasJellyBeanAndAbove() ? null : listener;
    }
}
