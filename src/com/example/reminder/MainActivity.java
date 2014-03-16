package com.example.reminder;

import org.apache.http.conn.scheme.LayeredSocketFactory;

import com.example.reminder.alarm.Alarm;
import com.example.reminder.alarm.LabelDialogFragment.AlarmLabelDialogHandler;
import com.example.reminder.alarm.TimePickerFragment;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity{

	private FragmentManager fm;
	protected FragmentTransaction ft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fm = getSupportFragmentManager();
		ft = fm.beginTransaction();
		
		ReminderFragment rf = new ReminderFragment();
		ft.replace(R.id.fragment_container, rf);
		ft.commit();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
