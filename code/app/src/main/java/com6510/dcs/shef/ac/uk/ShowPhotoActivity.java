package com6510.dcs.shef.ac.uk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com6510.dcs.shef.ac.uk.gallery.R;

/**
 * Class to show a single photo. Creates menu options for the editing anf map visualization of the photo
 */
public class ShowPhotoActivity extends AppCompatActivity {

    private Photo photo;
    private Bitmap bitmap;

    private GalleryViewModel viewModel;

    /**
     * On Create
     * @param savedInstanceState State
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Bundle data = getIntent().getExtras();
        photo = (Photo) data.getParcelable("photo");

        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

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

    /**
     * On the Menu being created. Only keep the Map icon if the photo has a location
     * @param menu Menu in question
     * @return True for success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        /* hide location button if not exist */
        if (photo.getImHasCoordinates() == false) {
            menu.removeItem(R.id.show_map_location);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * On selecting an option from the menu
     * @param item Selected item
     * @return True for success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_metadata) {
            Intent editIntent = new Intent(getApplicationContext(), EditActivity.class);
            editIntent.putExtra("Photo", photo);
            startActivityForResult(editIntent, 0);
        } else if (id == R.id.show_metadata) {
            /* build info string */
            String message = "TITLE\n" + photo.getImTitle() + "\n\n"
                    + "DESCRIPTION\n " + photo.getImDescription() + "\n\n"
                    + "DATE TAKEN\n" + photo.getImDateTime() + "\n\n"
                    + "LOCATION\n" + photo.getImLat() + ", " + photo.getImLng() + "\n\n"
                    + "ARTIST\n" + photo.getImArtist() + "\n\n"
                    + "MAKE\n" + photo.getImMake() + "\n\n"
                    + "MODEL\n" + photo.getImModel();
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
        } else if (id == R.id.show_map_location) {
            /*Send an intent to edit the photo, with old data parcelled*/
            Intent mapIntent = new Intent(getApplicationContext(), ShowPhotoLocationActivity.class);
            mapIntent.putExtra("Photo", photo);
            startActivityForResult(mapIntent, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On receiving edited data. Add it back to the DB.
     * @param requestCode Request Code
     * @param resultCode Response Code
     * @param data Data received
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            /* result from edit */
            Bundle extras = data.getExtras();
            photo = (Photo) extras.get("Photo");
            Util.writePhotoMetadata(photo);
            viewModel.insertPhoto(photo);
        }
    }
}
