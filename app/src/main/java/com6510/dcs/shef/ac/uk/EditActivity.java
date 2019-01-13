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
import java.util.Locale;

import com6510.dcs.shef.ac.uk.gallery.R;

public class EditActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText dateEdit;
    private EditText descEdit;
    private EditText latEdit;
    private EditText lngEdit;

    final Calendar calendar = Calendar.getInstance();

    private Photo photo;

    private void updateDateFilter() {
        String format = getResources().getString(R.string.date_format);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        dateEdit.setText(sdf.format(calendar.getTime()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        dateEdit = (EditText) findViewById(R.id.edit_date);
        titleEdit = (EditText) findViewById(R.id.edit_title);
        descEdit = (EditText) findViewById(R.id.edit_description);
        latEdit = (EditText) findViewById(R.id.edit_latitude);
        lngEdit = (EditText) findViewById(R.id.edit_longitude);

        Bundle sourceExtras = getIntent().getExtras();
        photo=(Photo) sourceExtras.get("Photo");
        dateEdit.setText(photo.getImDateTime());
        titleEdit.setText(photo.getImTitle());
        descEdit.setText(photo.getImDescription());
        latEdit.setText(photo.getImHasCoordinates() ? Float.toString(photo.getImLat()) : "");
        lngEdit.setText(photo.getImHasCoordinates() ? Float.toString(photo.getImLng()) : "");

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
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditActivity.this,
                        date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        Button editButton = (Button) findViewById(R.id.edit_submit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                photo.setImDateTime(dateEdit.getText().toString());
                photo.setImTitle(titleEdit.getText().toString());
                photo.setImDescription(descEdit.getText().toString());
                photo.setImLat(Float.parseFloat(latEdit.getText().toString()));
                photo.setImLng(Float.parseFloat(lngEdit.getText().toString()));
                resultIntent.putExtra("Photo", photo);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
