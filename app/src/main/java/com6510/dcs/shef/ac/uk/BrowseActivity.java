package com6510.dcs.shef.ac.uk;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
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
    private ArrayList<Photo> photoDataset;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private BrowseAdapter adapter;

    /* permissions */
    private Set<String> neededPermissions;
    private Set<String> grantedPermissions;
    private final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;

    /* other */
    private FusedLocationProviderClient mFusedLocationClient;
    private Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        activity = this;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        neededPermissions = Util.getDeclaredPermissions(getApplicationContext());
        grantedPermissions = new HashSet<>();

        /* request needed permissions */
        System.out.println("Needed permissions: " + neededPermissions.toString());
        System.out.println("Requesting permissions");
        requestPermissions();
    }

    void requestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        ActivityCompat.requestPermissions(activity, permissions, 0);
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
        System.out.println("Granted permissions so far: " + grantedPermissions.toString());

        /* check if we have all needed permissions now */
        if (grantedPermissions.containsAll(neededPermissions)) {
            /* resume app if we have required permissions */
            System.out.println("Got all permissions, resuming app");
            setup();
        } else {
            /* didn't get all permissions */
            System.out.println("Did not get all required permissions, exiting");
            Toast.makeText(getApplicationContext(), "Did not get all necessary permissions", Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }
    }

    void setup() {
        /* set up grid */
        recyclerView = findViewById(R.id.grid_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        int numberOfColumns = 4;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerView.setHasFixedSize(true);

        /* build viewmodel */
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

        /* set recycle view adapter */
        adapter = new BrowseAdapter(this, viewModel);
        recyclerView.setAdapter(adapter);
        //adapter.setHasStableIds(true);

        /* start async task to scan phone for photos */
        viewModel.refreshDatabase(getApplicationContext());

        /* start observing */
        viewModel.getAllPhotos().observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                photoDataset=(ArrayList<Photo>) photos;
                System.out.println("onChanged: size " + photos.size());

                /* show message if no photos found */
                if (photos.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                /* sort photos by last modified time */
                Collections.sort(photos, (new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return (int)(o1.getImTimestamp() - o2.getImTimestamp());
                    }
                }));

                /* update photos in the adapter */
                adapter.setPhotosDiff(photos);
            }});

        /* floating button to manually add photos from gallery */
        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openGallery(getActivity(), 0);
            }
        });

        /* floating button to open map view */
        FloatingActionButton fabMap = (FloatingActionButton) findViewById(R.id.fab_map);
        fabMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(), MapsActivity.class);
                startActivity(intent);
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
        /* hide if device does not have a camera */
        if (Util.checkCameraHardware(getApplicationContext()) == false) {
            fabCamera.hide();
        }

        /* initialize easyimage */
        Util.initEasyImage(getApplicationContext());
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
                /* create images directory if not created */
                File imagesDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TinyGallery");
                imagesDirectory.mkdir();

                System.out.println("Indexing camera/gallery files:");
                for (File file : imageFiles) {
                    /* copy file to tinygallery images directory */
                    File newFile = new File(imagesDirectory, file.getName());
                    try {
                        Util.copyFile(file, newFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //photoPath = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), "", "");
                    System.out.println("Path: " + newFile.getAbsolutePath());
                    final Photo photo = new Photo(newFile.getAbsolutePath(), Util.getNewThumbnailPath(getApplicationContext()));
                    Util.readPhotoMetadata(photo);
                    Util.makeThumbnail(photo.getImPath(), photo.getImThumbPath());
                    /* store GPS location if photo taken from camera */
                    if (source == EasyImage.ImageSource.CAMERA) {
                        try {
                            mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        System.out.println("Storing GPS data for camera image " + photo.getImPath());
                                        System.out.println("Coordinates: " + location.getLatitude() + ", " + location.getLongitude());
                                        photo.setImLat((float) location.getLatitude());
                                        photo.setImLng((float) location.getLongitude());
                                        photo.setImHasCoordinates(true);
                                        viewModel.insertPhoto(photo);
                                    }
                                }
                            });
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    } else {
                        /* just insert in db */
                        viewModel.insertPhoto(photo);
                    }
                }
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
        if (id == R.id.reset_app) {
            ((ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
