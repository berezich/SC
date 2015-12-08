package com.berezich.sportconnector;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.google.api.client.util.DateTime;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String ARG_DTBIRTHDAY = "dtBirthday_milsec";
    OnActionDatePickerDialogListener listener;

    public DatePickerFragment() {
    }

    public DatePickerFragment setArgs(String dBirthday) {
        Bundle args = new Bundle();

        DateTime dtBirthday = UsefulFunctions.parseDateTime(dBirthday);
        if(dtBirthday!=null)
            args.putLong(ARG_DTBIRTHDAY, dtBirthday.getValue());
        this.setArguments(args);
        return this;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getTargetFragment()==null)
            throw new NullPointerException("Need to set targetFragment for DatePickerFragment");
        try {
            listener = (OnActionDatePickerDialogListener)getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString() + " must implement OnActionDialogListener for AlertDialogFragment");
        }

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        try {
            Long birthday = getArguments().getLong(ARG_DTBIRTHDAY);
            if(birthday>0) {
                c.setTimeInMillis(birthday);
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }
            DatePickerDialog pickerDialog = new DatePickerDialog(getTargetFragment().getActivity(), this, year, month, day);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-getResources().getInteger(R.integer.editProfile_minAge));
            pickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - getResources().getInteger(R.integer.editProfile_maxAge));
            pickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            return pickerDialog;
        } catch (Exception e) {
            e.printStackTrace();
            return new DatePickerDialog(getTargetFragment().getActivity(), this,year, month, day);
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        listener.onDateSet( year, month, day);
        this.dismiss();
    }
    public interface OnActionDatePickerDialogListener
    {
        void onDateSet(int year, int month, int day);
    }
}
