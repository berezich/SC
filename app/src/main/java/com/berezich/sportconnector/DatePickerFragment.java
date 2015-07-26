package com.berezich.sportconnector;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;


import java.util.Calendar;

/**
 * Created by Sashka on 26.07.2015.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    OnActionDatePickerDialogListener listener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if(getTargetFragment()==null)
            throw new NullPointerException("Need to set targetFragment for DatePickerFragment");

        try {
            listener = (OnActionDatePickerDialogListener)getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnActionDialogListener for AlertDialogFragment");
        }

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getTargetFragment().getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        listener.onDateSet( year, month, day);
        this.dismiss();
    }
    public static interface OnActionDatePickerDialogListener
    {
        void onDateSet(int year, int month, int day);
    }
}
