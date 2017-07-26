package com.kenung.vn.prettymusic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by sev_user on 5/31/2017.
 */
public class TimePickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //Create and return a new instance of TimePickerDialog
        TimePickerDialog.OnTimeSetListener mTimeSetListenerIni =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(android.widget.TimePicker view,
                                          int hourOfDay, int minute) {
                        MusicResource.timer = hourOfDay + "-" + minute;
                        getActivity().sendBroadcast(new Intent("EnableAlarm"));
                    }

                };
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), mTimeSetListenerIni, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", dialog);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().sendBroadcast(new Intent("DisableAlarm"));
            }
        });

        return dialog;
    }
}