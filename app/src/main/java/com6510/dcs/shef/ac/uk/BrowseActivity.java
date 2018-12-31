package com6510.dcs.shef.ac.uk;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com6510.dcs.shef.ac.uk.gallery.R;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class BrowseActivity extends AppCompatActivity {
    
    /* ViewModel */
    private GalleryViewModel viewModel;

    private RecyclerView recycler_view;
    private BrowseAdapter adapter;

    /* permissions */
    private Set<String> requestedPermissions;
    private Set<String> grantedPermissions;

    List<Photo> oldPhotos = new ArrayList<Photo>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        /* set up grid */
        recycler_view = findViewById(R.id.grid_recycler_view);
        int numberOfColumns = 4;
        recycler_view.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recycler_view.setHasFixedSize(true);

        /* set recycle view adapter */
        adapter = new BrowseAdapter(this);
        recycler_view.setAdapter(adapter);
        //adapter.setHasStableIds(true);

        /* build viewmodel */
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

        /* start async task to scan phone for photos */
        viewModel.refreshDatabase(getApplicationContext());

        /* start observing */
        viewModel.getAllPhotos().observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                System.out.println("onChanged: size " + photos.size());
                Collections.sort(photos, (new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return (int)(o1.getImTimestamp() - o2.getImTimestamp());
                    }
                }));
                /* update photos in the adapter */
                adapter.setPhotosDiff(photos);
                //adapter.setPhotos(photos);
            }});

        /* floating button to manually add photos from gallery */
        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openGallery(getActivity(), 0);
            }
        });

        /* floating button to take photos from camera */
        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(getActivity(), 0);
            }
        });

        requestedPermissions = Util.getDeclaredPermissions(getApplicationContext());
        grantedPermissions = new HashSet<>();

        /* required by Android 6.0+ */
        /* check if we have all needed permissions now */
        while (!grantedPermissions.containsAll(requestedPermissions)) {
            System.out.println("Requesting permissions");
            Util.checkPermissions(this, getApplicationContext(), grantedPermissions);
        }

        System.out.println("Got all permissions, resuming app");

        /* initialize easyimage */
        Util.initEasyImage(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /* update set of granted permissions */
        for (int i=0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            }
        }

        System.out.println("requestedPermissions: " + requestedPermissions.toString());
        System.out.println("grantedPermissions: " + grantedPermissions.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                System.out.println("Inserting manually picked files:");
                for (File f : imageFiles) {
                    System.out.println(f.getAbsolutePath());
                    //indexFile(f.getAbsolutePath());
                    viewModel.insertPhoto(new Photo(f.getAbsolutePath()));
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
            }
        });
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear_cache) {
            viewModel.deleteAll();
            viewModel.refreshDatabase(getApplicationContext());
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
