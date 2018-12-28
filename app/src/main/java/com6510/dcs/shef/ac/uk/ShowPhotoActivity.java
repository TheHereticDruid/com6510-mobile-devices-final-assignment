package com6510.dcs.shef.ac.uk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com6510.dcs.shef.ac.uk.gallery.R;

public class ShowPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Bundle data = getIntent().getExtras();
        Photo photo = (Photo) data.getParcelable("photo");

        ImageView image = findViewById(R.id.image_preview);
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getImPath()); /* read photo from disk */
        image.setImageBitmap(bitmap);

    }
}
