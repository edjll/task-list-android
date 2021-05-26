package com.example.converter;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class DatePickerDialog extends DialogFragment {

    private final Consumer<Date> consumer;
    private DatePicker datePicker;
    private Date date;
    private Calendar calendar;

    public DatePickerDialog(Consumer<Date> consumer, Date date) {
        this.consumer = consumer;
        this.date = date;
        this.calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, null);
        this.datePicker = view.findViewById(R.id.datePicker);

        this.calendar.setTime(date);
        this.datePicker.updateDate(this.calendar.get(Calendar.YEAR), this.calendar.get(Calendar.MONTH), this.calendar.get(Calendar.DAY_OF_MONTH));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.datePicker.setOnDateChangedListener(
                    (v, year, monthOfYear, dayOfMonth) -> {
                        this.calendar.set(year, monthOfYear, dayOfMonth);
                        consumer.accept(this.calendar.getTime());
                    }
            );
        }

        return view;
    }
}