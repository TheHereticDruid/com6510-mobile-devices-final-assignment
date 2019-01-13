package com6510.dcs.shef.ac.uk;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com6510.dcs.shef.ac.uk.gallery.R;
import pl.aprilapps.easyphotopicker.Constants;
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

    /* other */
    private FusedLocationProviderClient mFusedLocationClient;
    private Activity activity;

    /* intents */
    private final int INTENT_EASYIMAGE = 8665;
    private final int INTENT_FILTER = 3543;
    private final int INTENT_EDIT = 3838;

    /* filter values */
    private String filter_title;
    private String filter_description;
    private String filter_date;
    private String filter_artist;
    private String filter_make;
    private String filter_model;

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
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.browse_columns_port)));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.browse_columns_land)));
        }
        recyclerView.setHasFixedSize(true);

        /* no filters initially */
        filter_title = "";
        filter_date = "";
        filter_description = "";
        filter_artist = "";
        filter_make = "";
        filter_model = "";

        /* build viewmodel */
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);

        /* set recycle view adapter */
        adapter = new BrowseAdapter(this, viewModel);
        recyclerView.setAdapter(adapter);
        //adapter.setHasStableIds(true);

        /* start async task to scan phone for photos */
        viewModel.refreshDatabase(getApplicationContext());

        /* set up observers */
        setFilteredObserver("", "", "", "", "", "");

        /* floating button to manually add photos from gallery */
        final FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openGallery(getActivity(), INTENT_EASYIMAGE);
            }
        });

        /* floating button to take photos from camera */
        final FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(getActivity(), INTENT_EASYIMAGE);
            }
        });
        /* hide if device does not have a camera */
        if (Util.checkCameraHardware(getApplicationContext()) == false) {
            fabCamera.hide();
        }

        /* floating buttion to filter photos */
        final FloatingActionButton fabFilter = (FloatingActionButton) findViewById(R.id.fab_filter);
        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FilterActivity.class);
                filterIntent.putExtra("TitleFilter", filter_title);
                filterIntent.putExtra("DateFilter", filter_date);
                filterIntent.putExtra("DescFilter", filter_description);
                filterIntent.putExtra("ArtistFilter", filter_artist);
                filterIntent.putExtra("MakeFilter", filter_make);
                filterIntent.putExtra("ModelFilter", filter_model);
                startActivityForResult(filterIntent, INTENT_FILTER);
            }
        });

        /* auto hide floating buttons */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    fabCamera.hide();
                    fabFilter.hide();
                    fabGallery.hide();
                } else if (dy < 0) {
                    fabCamera.show();
                    fabFilter.show();
                    fabGallery.show();
                }
            }
        });

        /* initialize easyimage */
        Util.initEasyImage(getApplicationContext());
    }

    protected void handleEasyImageResult(int requestCode, int resultCode, Intent data) {
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
                    /* notify mediascanner */
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{newFile.getAbsolutePath()}, null, null);

                    System.out.println("Path: " + newFile.getAbsolutePath());
                    final Photo photo = new Photo(
                            newFile.getAbsolutePath(),
                            Util.getNewThumbnailPath(getApplicationContext()),
                            file.lastModified(),
                            file.getName(),
                            "", 0, 0, false, "", "", "", "");
                    Util.readPhotoMetadata(photo);
                    Util.makeThumbnail(photo.getImPath(), photo.getImThumbPath());
                    /* store GPS location if photo taken from camera */
                    if (source == EasyImage.ImageSource.CAMERA) {
                        try {
                            if (Util.isEmulator()) {
                                /* UOS DCS */
                                photo.setImLat(53.381028f);
                                photo.setImLng(-1.480318f);
                                photo.setImHasCoordinates(true);
                            } else {
                                mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            System.out.println("Storing GPS data for camera image " + photo.getImPath());
                                            photo.setImLat((float) location.getLatitude());
                                            photo.setImLng((float) location.getLongitude());
                                            photo.setImHasCoordinates(true);
                                            System.out.println("Coordinates: " + photo.getImLat() + ", " + photo.getImLng());
                                        }
                                        /* edit metadata before saving image */
                                        Intent editIntent = new Intent(getApplicationContext(), EditActivity.class);
                                        editIntent.putExtra("Photo", photo);
                                        startActivityForResult(editIntent, INTENT_EDIT);
                                    }
                                });
                            }
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    } else {
                        /* just insert in db */
                        viewModel.insertPhoto(photo);
                    }
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(BrowseActivity.this);
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            }
        });
    }

    void handleFilterResult(int requestCode, int resultCode, Intent data) {
        System.out.println("handleFilterResult called");
        if(resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            filter_title = extras.getString("TitleFilter", "");
            filter_description = extras.getString("DescFilter", "");
            filter_date = extras.getString("DateFilter", "");
            filter_artist = extras.getString("ArtistFilter", "");
            filter_make = extras.getString("MakeFilter", "");
            filter_model = extras.getString("ModelFilter", "");
            /* update filter observer */
            setFilteredObserver(filter_title, filter_description, filter_date, filter_artist, filter_make, filter_model);
        }
    }

    void handleEditResult(int requestCode, int resultCode, Intent data) {
        System.out.println("handleEditResult called");
        if(resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Photo newPhoto = (Photo) extras.get("Photo");
            Util.writePhotoMetadata(newPhoto);
            viewModel.insertPhoto(newPhoto);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RequestCodes.PICK_PICTURE_FROM_GALLERY || requestCode == Constants.RequestCodes.TAKE_PICTURE) {
            handleEasyImageResult(requestCode, resultCode, data);
        } else if(requestCode == INTENT_FILTER) {
            handleFilterResult(requestCode, resultCode, data);
        } else if(requestCode == INTENT_EDIT) {
            handleEditResult(requestCode, resultCode, data);
        }
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.reset_app) {
            ((ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
            return true;
        } else if (id == R.id.map_view) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFilteredObserver(String title, String description, String date, String artist, String make, String model) {
        System.out.println("Setting observer with filters title=" + title
                + ", description=" + description
                + ", date=" + date
                + ", artist=" + artist
                + ", make=" + make
                + ", model=" + model);
        viewModel.getFilteredPhotos("%"+title+"%",
                "%"+description+"%",
                "%"+date+"%",
                "%"+artist+"%",
                "%"+make+"%",
                "%"+model+"%")
                .observe(this, new Observer<List<Photo>>(){
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
                        if (o1.getImTimestamp() == o2.getImTimestamp()) {
                            return 0;
                        }
                        return o2.getImTimestamp() < o1.getImTimestamp() ? -1 : 1;
                    }
                }));

                /* update photos in the adapter */
                adapter.setPhotosDiff(photos);
            }});
    }
}
