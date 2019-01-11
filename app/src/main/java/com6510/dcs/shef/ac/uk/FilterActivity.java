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

public class FilterActivity extends AppCompatActivity {

    private EditText dateFilter;
    private EditText titleFilter;
    private EditText descFilter;

    public static class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar=Calendar.getInstance();
            return new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            EditText dateEditText=(EditText) getActivity().findViewById(R.id.dateFilter);
            dateEditText.setText(day+"-"+(month+1)+"-"+year);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        TextView instructions = (TextView) findViewById(R.id.filterInstructions);
        instructions.setTextSize(TypedValue.COMPLEX_UNIT_PX, instructions.getTextSize() * 1.5f);
        dateFilter = (EditText) findViewById(R.id.dateFilter);
        titleFilter = (EditText) findViewById(R.id.titleFilter);
        descFilter = (EditText) findViewById(R.id.descFilter);

        Bundle sourceExtras = getIntent().getExtras();
        dateFilter.setText(sourceExtras.getString("DateFilter"));
        titleFilter.setText(sourceExtras.getString("TitleFilter"));
        descFilter.setText(sourceExtras.getString("DescFilter"));

        ImageView dateFilterPicker=(ImageView) findViewById(R.id.dateFilterPicker);
        dateFilterPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerDialogFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
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
    }
}
