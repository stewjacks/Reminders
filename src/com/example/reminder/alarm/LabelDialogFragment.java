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

import com.example.reminder.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


/**
 * DialogFragment to edit label.
 */
public class LabelDialogFragment extends DialogFragment {

    private static final String KEY_LABEL = "label";
    private static final String KEY_ALARM = "alarm";
    private static final String KEY_TIMER = "timer";
    private static final String KEY_TAG = "tag";
	private static final String TAG = "LabelDialogFragment";
	
	private final Alarm alarm;
	private AlarmLabelDialogHandler mListener;
	private final String label;
    private EditText mLabelBox;
    
    public LabelDialogFragment (String label, Alarm alarm, AlarmLabelDialogHandler mListener){
    	this.mListener = mListener;
    	this.label = label;
    	this.alarm = alarm;
    }
    
    @Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }
    
    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	

        final View view = inflater.inflate(R.layout.label_dialog, container, false);

        mLabelBox = (EditText) view.findViewById(R.id.labelBox);
        mLabelBox.setText(label);
        mLabelBox.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    set(alarm);
                    return true;
                }
                return false;
            }
        });

        final Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Button setButton = (Button) view.findViewById(R.id.setButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set(alarm);
            }
        });

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    private void set(Alarm alarm) {
        String label = mLabelBox.getText().toString();
        if (label.trim().length() == 0) {
            // Don't allow user to input label with only whitespace.
            label = "";
        }

        if (alarm != null) {
            set(alarm, label);

        } else {
            Log.e(TAG, "No alarm or timer available.");
        }
    }

    private void set(Alarm alarm, String label) {
//            ((DeskClock) getActivity()).onDialogLabelSet(alarm, label, tag);
    	mListener.onDialogLabelSet(alarm, label);
        dismiss();
    }

    public interface AlarmLabelDialogHandler {
        public void onDialogLabelSet(Alarm alarm, String label);
    }
}
