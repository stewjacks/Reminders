package com.example.reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.reminder.alarm.Alarm;
import com.example.reminder.alarm.AlarmSharedPreferencesAdapter;
import com.example.reminder.alarm.AlarmUtils;
import com.example.reminder.alarm.DaysOfWeek;
import com.example.reminder.alarm.LabelDialogFragment;
import com.example.reminder.alarm.LabelDialogFragment.AlarmLabelDialogHandler;

public class ReminderFragment<mSelectedAlarm> extends Fragment implements 
	TimePickerDialog.OnTimeSetListener
	{

    private static final float EXPAND_DECELERATION = 1f;
    private static final float COLLAPSE_DECELERATION = 0.7f;
    private static final int ANIMATION_DURATION = 300;
    
    private static final String KEY_EXPANDED_IDS = "expandedIds";
    private static final String KEY_REPEAT_CHECKED_IDS = "repeatCheckedIds";
    private static final String KEY_RINGTONE_TITLE_CACHE = "ringtoneTitleCache";
    private static final String KEY_SELECTED_ALARMS = "selectedAlarms";
    private static final String KEY_DELETED_ALARM = "deletedAlarm";
    private static final String KEY_UNDO_SHOWING = "undoShowing";
    private static final String KEY_PREVIOUS_DAY_MAP = "previousDayMap";
    private static final String KEY_SELECTED_ALARM = "selectedAlarm";
    private static final String KEY_DELETE_CONFIRMATION = "deleteConfirmation";
    
	private static final String TAG = "ReminderFragment";
    private static final String FRAG_TAG_LABEL_DIALOG = "label_dialog";

	private DecelerateInterpolator mExpandInterpolator;
	private DecelerateInterpolator mCollapseInterpolator;

    private Animator mFadeIn;
    private Animator mFadeOut;

    private ImageButton mAddAlarmButton;

	private ListView mAlarmsList;
	private View mEmptyView;
	private Object mAlarmsView;
	private Alarm mSelectedAlarm;
	private long mScollToAlarmId;
	private ArrayList<Alarm> reminderList;
	private AlarmItemAdapter mAdapter;
	public ListView mList;
	private Bundle mPreviousDaysOfWeekMap = new Bundle();
	public Context mContext;
	private CharSequence[] mShortWeekDayStrings;
//	public CharSequence[] mLongWeekDayStrings;
	private ImageButton mSaveAlarmsButton;
	private AlarmSharedPreferencesAdapter alarmPreferences;

    public ReminderFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.alarm_clock, container, false);
		
        mExpandInterpolator = new DecelerateInterpolator(EXPAND_DECELERATION);
        mCollapseInterpolator = new DecelerateInterpolator(COLLAPSE_DECELERATION);
    	mShortWeekDayStrings = getResources().getStringArray(R.array.shortDaysOfWeek);

        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mSelectedAlarm = null;
                startCreatingAlarm();
            }
        };
        
        mAddAlarmButton = (ImageButton) v.findViewById(R.id.alarm_add_alarm);
        mAddAlarmButton.setOnClickListener(l);
        
        mEmptyView = v.findViewById(R.id.alarms_empty_view);
        mEmptyView.setOnClickListener(l);
        
        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) mAddAlarmButton.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        mAddAlarmButton.setLayoutParams(layoutParams);

        mAlarmsList = (ListView) v.findViewById(R.id.alarms_list);
        
        mFadeIn = AnimatorInflater.loadAnimator(getActivity(), R.anim.fade_in);
        mFadeIn.setDuration(ANIMATION_DURATION);
        mFadeIn.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Do nothing.
            }
        });
        mFadeIn.setTarget(mEmptyView);
        mFadeOut = AnimatorInflater.loadAnimator(getActivity(), R.anim.fade_out);
        mFadeOut.setDuration(ANIMATION_DURATION);
        mFadeOut.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {
                mEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mEmptyView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
                // Do nothing.
            }
        });
        mFadeOut.setTarget(mEmptyView);

        //retrieve SharedPreferences Alarm list TODO: check if exists
        alarmPreferences = new AlarmSharedPreferencesAdapter(getActivity());
        reminderList = alarmPreferences.reminders;
        
        if (reminderList.size()>0){
			mEmptyView.setVisibility(View.GONE);
			mAlarmsList.setVisibility(View.VISIBLE);
        }
        
        mAdapter = new AlarmItemAdapter(getActivity(), R.id.alarms_list, reminderList, mAlarmsList);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
        	@Override
        	public void onChanged(){
        		final int count = mAdapter.getCount();
        		if (count == 0){
        			mAlarmsList.setVisibility(View.GONE);
        			mEmptyView.setVisibility(View.VISIBLE);
        		} else{
        			mEmptyView.setVisibility(View.GONE);
        			mAlarmsList.setVisibility(View.VISIBLE);
        		}
        	}
		});
		
        mSaveAlarmsButton = (ImageButton) v.findViewById(R.id.save_button);
        mSaveAlarmsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alarmPreferences.saveSharedPreferences(mAdapter.getAlarmList());
				Intent i = new Intent();
				i.setAction(Constants.ALARM_UPDATE_BROADCAST);
				getActivity().sendBroadcast(i);
				
			}
		});
		
		mAlarmsList.setAdapter(mAdapter);
		return v;
    }

    private void startCreatingAlarm() {
        // Set the "selected" alarm as null, and we'll create the new one when the timepicker
        // comes back.
//        mSelectedAlarm = null;
        AlarmUtils.showTimeEditDialog(getChildFragmentManager(), null,
        		ReminderFragment.this, DateFormat.is24HourFormat(getActivity()));
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //going to lock the view horizontal to avoid SP.
//        outState.putLongArray(KEY_REPEAT_CHECKED_IDS, mAdapter.getRepeatArray());
//        outState.putLongArray(KEY_SELECTED_ALARMS, mAdapter.getSelectedAlarmsArray());
//        outState.putBundle(KEY_RINGTONE_TITLE_CACHE, mRingtoneTitleCache);
//        outState.putParcelable(KEY_DELETED_ALARM, mDeletedAlarm);
//        outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
//        outState.putBundle(KEY_PREVIOUS_DAY_MAP, mAdapter.getPreviousDaysOfWeekMap());
//        outState.putParcelable(KEY_SELECTED_ALARM, mSelectedAlarm);
    }

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
		// TODO Auto-generated method stub
		
		if(mSelectedAlarm == null){
			Alarm a = new Alarm(); //create a new alarm because the user added one
			a.hour = hourOfDay;
			a.minutes = minutes;
			a.enabled = true;
			a.alert = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), 
					RingtoneManager.TYPE_NOTIFICATION).toString();
			asyncAddAlarm(a);
		}else{
			mSelectedAlarm.hour = hourOfDay;
			mSelectedAlarm.minutes = minutes;
			mSelectedAlarm.enabled = true;
			mScollToAlarmId = mSelectedAlarm.id;
			asyncUpdateAlarm(mSelectedAlarm, true);
			mSelectedAlarm = null;
			
		}
	}
	
	
    private void asyncDeleteAlarm(final Alarm alarm, final View viewToRemove) {
        final Context context = ReminderFragment.this.getActivity().getApplicationContext();
        
        mAdapter.remove(alarm);
        

    }
	
    private void asyncAddAlarm(final Alarm alarm) {
    	
//    	reminderList.add(alarm);
    	mAdapter.add(alarm);
    	
    }

    private void asyncUpdateAlarm(final Alarm alarm, final boolean popToast) {
    	int i = alarm.id;
    	mAdapter.remove(mAdapter.getItem(i));
    	mAdapter.insert(alarm, i);
    }
    


    public class AlarmItemAdapter extends ArrayAdapter<Alarm> implements 
    	LabelDialogFragment.AlarmLabelDialogHandler  {
    	private static final String TAG = "AlarmAdapter";
        
    	// This determines the order in which it is shown and processed in the UI.
        private final int[] DAY_ORDER = new int[] {
                Calendar.SUNDAY,
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
        };
    	private ArrayList<Alarm> reminders;

		private LayoutInflater mFactory;
        
        public class ItemHolder{

            // views for optimization
            LinearLayout alarmItem;
            TextView clock;
            Switch onoff;
            TextView daysOfWeek;
            TextView label;
            ImageView delete;
            LinearLayout repeatDays;
            ViewGroup[] dayButtonParents = new ViewGroup[7];
            ToggleButton[] dayButtons = new ToggleButton[7];
            CheckBox vibrate;
            TextView ringtone;
            View hairLine;
            View arrow;
            View collapseExpandArea;
            View footerFiller;

            // Other states
            Alarm alarm;
        }
    	
        public AlarmItemAdapter (Context context, int listViewId, 
        		ArrayList<Alarm> reminders,
        		ListView list
        		){
        	super(context, listViewId, reminders);
        	mList = list;
        	this.reminders = reminders;
        	mContext = context;
        }
        
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            View vi = view;
            final Alarm alarm = reminders.get(position);
            alarm.id = position;
            final ItemHolder holder;
//            try {
        		mFactory = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (view == null) {
                	// first check to see if the view is null. if so, we have to inflate it.
                    vi = mFactory.inflate(R.layout.reminder_list_item, null);
                    
                }
                
                if (alarm != null){
                    //Set up a new holder for this new view
                    holder = new ItemHolder();
                    
                    holder.clock = (TextView) vi.findViewById(R.id.reminder_textclock);
                    holder.onoff = (Switch) vi.findViewById(R.id.reminder_switch);
                    holder.label = (TextView) vi.findViewById(R.id.reminder_label);
                    holder.delete = (ImageView) vi.findViewById(R.id.reminder_delete);
                    holder.repeatDays = (LinearLayout) vi.findViewById(R.id.repeat_days);
                    
                    vi.setTag(holder);
                } else {
                    holder = (ItemHolder) vi.getTag();
                }
                
                /*Bind the view to data from Alarm*/
                holder.alarm = alarm;
                // We must unset the listener first because this maybe a recycled view so changing the
                // state would affect the wrong alarm.
                holder.onoff.setOnCheckedChangeListener(null);
                holder.onoff.setChecked(alarm.enabled); 

                /*Set up the clock*/
                Log.d(TAG, "item: "+position+" time: "+alarm.hour+":"+alarm.minutes);
                CharSequence c = setTime(alarm.hour, alarm.minutes);
                holder.clock.setText(c);
//                holder.clock.
                holder.clock.setClickable(true);
                holder.clock.setOnClickListener(new View.OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					mSelectedAlarm = holder.alarm;
    					AlarmUtils.showTimeEditDialog(getChildFragmentManager(), alarm, ReminderFragment.this, DateFormat.is24HourFormat(getContext()));
    					
    				}
    			});
                
                /*Set up the label*/
                
                if (alarm.label != null && alarm.label.length() > 0) {
                    holder.label.setText(alarm.label);
                    holder.label.setTextColor(getResources().getColor(android.R.color.primary_text_dark));

                } else {
                	holder.label.setText(R.string.default_label);
                    holder.label.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                }
                holder.label.setText(alarm.label);
                holder.label.setOnClickListener(new View.OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					// TODO DialogFragment popup with text edit
    					
    				}
    			});
                
                /*Set up the toggle switch*/
                holder.onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    				
    				@Override
    				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    					alarm.enabled=isChecked;
    				}
    			});

                /*Set up the delete switch*/
                holder.delete.setOnClickListener(new View.OnClickListener() {
    				
    				@Override
    				public void onClick(View v) {
    					asyncDeleteAlarm(alarm, v);
    					
    				}
    			});
                
                holder.label.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    	//TODO:add clickable label dialog   
                    	showLabelDialog(getChildFragmentManager(), alarm);
                    }
                });
                //TODO: hack - fix the loop so this only happens once
                holder.repeatDays.removeAllViews();
                
                for (int i = 0; i < 7; i++) {
                	final int buttonIndex = i;
                	final int bitSet = alarm.daysOfWeek.getBitSet();    
                	
                    ViewGroup viewgroup = (ViewGroup) mFactory.inflate(R.layout.day_button,
                            holder.repeatDays, false);
                    final ToggleButton button = (ToggleButton) viewgroup.getChildAt(0);
//                    final int dayToShowIndex = DAY_ORDER[i-1];
                    button.setText(mShortWeekDayStrings[i]);
                    button.setTextOn(mShortWeekDayStrings[i]);
                    button.setTextOff(mShortWeekDayStrings[i]);
                    button.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
//							holder.dayButtons[buttonIndex].toggle();
                        	final boolean checked = holder.dayButtons[buttonIndex].isChecked();
                        	int day = DAY_ORDER[buttonIndex];
                        	alarm.daysOfWeek.setDaysOfWeek(checked, day);
                        	if (checked) {
                        		turnOnDayOfWeek(holder, buttonIndex);
                        	} else {
                        		turnOffDayOfWeek(holder, buttonIndex);
                        	}
						}
                    });
                    
                holder.repeatDays.addView(viewgroup);
                
                holder.dayButtons[i] = button;
                holder.dayButtonParents[i] = viewgroup;

            }
            updateDaysOfWeekButtons(holder, alarm.daysOfWeek);

            

            return vi;
        }
        
        private void bindView(int position, View v){
        	
        }
        
        private int hour;
        private int minute;
        private CharSequence mFormat;
        private final CharSequence DEFAULT_FORMAT_12_HOUR = "h:mm a";

        public final CharSequence DEFAULT_FORMAT_24_HOUR = "H:mm";
        
        public CharSequence setTime(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
            chooseFormat();
            final Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            return (DateFormat.format(mFormat, calendar));
        }
        
        private void chooseFormat() {
            final boolean format24Requested = DateFormat.is24HourFormat(getContext());
            if (format24Requested) {
                mFormat = DEFAULT_FORMAT_24_HOUR;
            } else {
                mFormat = DEFAULT_FORMAT_12_HOUR;
            }
//            mFormat.toString();
        }
        
        private View getViewById(long id) {
            for (int i = 0; i < mList.getCount(); i++) {
                View v = mList.getChildAt(i);
                if (v != null) {
                    ItemHolder h = (ItemHolder)(v.getTag());
                    if (h != null && h.alarm.id == id) {
                        return v;
                    }
                }
            }
            return null;
        }


        public Bundle getPreviousDaysOfWeekMap() {
            return mPreviousDaysOfWeekMap ;
        }

        private void buildHashSetFromArray(long[] ids, HashSet<Long> set) {
            for (long id : ids) {
                set.add(id);
            }
        }
        
        private void updateDaysOfWeekButtons(ItemHolder holder, DaysOfWeek daysOfWeek) {
            HashSet<Integer> setDays = daysOfWeek.getSetDays();
            for (int i = 0; i < 7; i++) {
                if (setDays.contains(DAY_ORDER[i])) {
                    turnOnDayOfWeek(holder, i);
                } else {
                    turnOffDayOfWeek(holder, i);
                }
            }
        }
        
        private void turnOffDayOfWeek(ItemHolder holder, int dayIndex) {
            holder.dayButtons[dayIndex].setChecked(false);
//            holder.dayButtons[dayIndex].setTextColor(mColorDim);
//            holder.dayButtons[dayIndex].setTypeface(mRobotoNormal);
        }

        private void turnOnDayOfWeek(ItemHolder holder, int dayIndex) {
            holder.dayButtons[dayIndex].setChecked(true);
//            holder.dayButtons[dayIndex].setTextColor(mColorLit);
//            holder.dayButtons[dayIndex].setTypeface(mRobotoBold);
        }

        public void setLabel(Alarm alarm, String label) {
            alarm.label = label;
            asyncUpdateAlarm(alarm, false);
        }
        
        public ArrayList<Alarm> getAlarmList(){
        	return reminders;
        }
        
        public void showLabelDialog(FragmentManager manager, final Alarm alarm) {
        	String label = alarm.label;
        	final LabelDialogFragment ldf = new LabelDialogFragment(alarm.label, alarm, this);
        	
        	manager.executePendingTransactions();
        	final FragmentTransaction ft = manager.beginTransaction();
        	final Fragment prev = manager.findFragmentByTag(FRAG_TAG_LABEL_DIALOG);
        	if (prev!=null){
        		ft.remove(prev);
        	}
        	ft.commit();
        	
        	ldf.show(manager, "label_dialog");
        }
        

		@Override
		public void onDialogLabelSet(Alarm alarm, String label) {
			alarm.label = label;
			
		}
        
    }
    
}


