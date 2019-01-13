package com6510.dcs.shef.ac.uk;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com6510.dcs.shef.ac.uk.gallery.R;

public class ShowPhotoActivity extends AppCompatActivity {

    private Photo photo;
    private Bitmap bitmap;

    private final int INTENT_EDIT = 3838;

    private GalleryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Bundle data = getIntent().getExtras();
        photo = (Photo) data.getParcelable("photo");

        viewModel= ViewModelProviders.of(this).get(GalleryViewModel.class);

        ImageView image = findViewById(R.id.image_preview);
        bitmap = BitmapFactory.decodeFile(photo.getImPath());
        image.setImageBitmap(bitmap);

        /* hide action bar on click to look nice */
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("RestrictedApi") // supress stupid warning (bug)
            public void onClick(View v) {
                android.support.v7.app.ActionBar actionBar = getSupportActionBar();
                actionBar.setShowHideAnimationEnabled(false);
                if (actionBar.isShowing()) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_metadata) {
            Intent editIntent=new Intent(getApplicationContext(), EditActivity.class);
            editIntent.putExtra("Photo", photo);
            startActivityForResult(editIntent, INTENT_EDIT);
        } else if (id == R.id.show_metadata) {
            /* build info string */
            String message = "Title: " + photo.getImTitle() + "\n"
                    + "Description: " + photo.getImDescription() + "\n"
                    + "Date taken: " + photo.getImDateTime() + "\n"
                    + "Location: " + photo.getImLat() + ", " + photo.getImLng() + "\n";
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Photo metadata")
                    .setMessage(message)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Photo newPhoto=(Photo) extras.get("Photo");
            viewModel.insertPhoto(newPhoto);
        }
    }
}
