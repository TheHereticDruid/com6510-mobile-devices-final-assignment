package com6510.dcs.shef.ac.uk;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText artistEdit;
    private EditText makeEdit;
    private EditText modelEdit;

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
        artistEdit = (EditText) findViewById(R.id.edit_artist);
        makeEdit = (EditText) findViewById(R.id.edit_make);
        modelEdit = (EditText) findViewById(R.id.edit_model);

        Bundle sourceExtras = getIntent().getExtras();
        photo=(Photo) sourceExtras.get("Photo");
        dateEdit.setText(photo.getImDateTime());
        titleEdit.setText(photo.getImTitle());
        descEdit.setText(photo.getImDescription());
        latEdit.setText(photo.getImHasCoordinates() ? Float.toString(photo.getImLat()) : "");
        lngEdit.setText(photo.getImHasCoordinates() ? Float.toString(photo.getImLng()) : "");
        artistEdit.setText(photo.getImArtist());
        makeEdit.setText(photo.getImMake());
        modelEdit.setText(photo.getImModel());

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
                if (latEdit.getText().toString().length() > 0 || lngEdit.getText().toString().length() > 0) {
                    try {
                        photo.setImLat(Float.parseFloat(latEdit.getText().toString()));
                        photo.setImLng(Float.parseFloat(lngEdit.getText().toString()));
                    } catch (NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Invalid coordinates!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                photo.setImArtist(artistEdit.getText().toString());
                photo.setImMake(makeEdit.getText().toString());
                photo.setImModel(modelEdit.getText().toString());
                resultIntent.putExtra("Photo", photo);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
