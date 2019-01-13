package com6510.dcs.shef.ac.uk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com6510.dcs.shef.ac.uk.gallery.R;

public class FilterActivity extends AppCompatActivity {

    private EditText dateFilter;
    private EditText titleFilter;
    private EditText descFilter;

    final Calendar calendar = Calendar.getInstance();

    private void updateDateFilter() {
        String format = getResources().getString(R.string.date_format);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        dateFilter.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        dateFilter = (EditText) findViewById(R.id.dateFilter);
        titleFilter = (EditText) findViewById(R.id.titleFilter);
        descFilter = (EditText) findViewById(R.id.descFilter);

        Bundle sourceExtras = getIntent().getExtras();
        dateFilter.setText(sourceExtras.getString("DateFilter"));
        titleFilter.setText(sourceExtras.getString("TitleFilter"));
        descFilter.setText(sourceExtras.getString("DescFilter"));

        /* set date picker to popup on clicking text view */
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateFilter();
            }
        };
        dateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(FilterActivity.this,
                        date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        Button filterButton = (Button) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("DateFilter", dateFilter.getText().toString());
                resultIntent.putExtra("TitleFilter", titleFilter.getText().toString());
                resultIntent.putExtra("DescFilter", descFilter.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilter.setText("");
                titleFilter.setText("");
                descFilter.setText("");
            }
        });
    }
}
