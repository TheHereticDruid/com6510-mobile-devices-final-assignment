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

import java.util.Calendar;

import com6510.dcs.shef.ac.uk.gallery.R;

public class EditActivity extends AppCompatActivity {

    private EditText titleEdit;
    private EditText dateEdit;
    private EditText descEdit;
    private EditText latEdit;
    private EditText lngEdit;
    private Photo photo;

    public static class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar=Calendar.getInstance();
            return new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            EditText dateEditText=(EditText) getActivity().findViewById(R.id.edit_date);
            dateEditText.setText(day+"-"+(month+1)+"-"+year);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        TextView instructions = (TextView) findViewById(R.id.edit_instructions);
        instructions.setTextSize(TypedValue.COMPLEX_UNIT_PX, instructions.getTextSize() * 1.5f);
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
        latEdit.setText(Float.toString(photo.getImLat()));
        lngEdit.setText(Float.toString(photo.getImLng()));

        ImageView dateEditPicker=(ImageView) findViewById(R.id.dateEditPicker);
        dateEditPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new FilterActivity.DatePickerDialogFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
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
