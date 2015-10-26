package com.berezich.sportconnector;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;


import com.google.api.client.util.DateTime;

import java.util.Calendar;

/**
 * Created by Sashka on 26.07.2015.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_DTBIRTHDAY = "dtBirthday_milsec";
    OnActionDatePickerDialogListener listener;

    public DatePickerFragment setArgs(String dBirthday) {
        Bundle args = new Bundle();
        DateTime dtBirthday = UsefulFunctions.parseDateTime(dBirthday);
        if(dtBirthday!=null)
            args.putLong(ARG_DTBIRTHDAY, dtBirthday.getValue());
        this.setArguments(args);
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Calendar c = Calendar.getInstance();
        Long birthday = getArguments().getLong(ARG_DTBIRTHDAY);
        if(birthday!=null && !birthday.equals(""))
            c.setTimeInMillis(birthday);
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

        DatePickerDialog pickerDialog = new DatePickerDialog(getTargetFragment().getActivity(), this, year, month, day);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-getResources().getInteger(R.integer.editProfile_minAge));
        pickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - getResources().getInteger(R.integer.editProfile_maxAge));
        pickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        return pickerDialog;
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
