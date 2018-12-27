package com6510.dcs.shef.ac.uk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;

    private boolean scan_started = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        /* set up live data */
        viewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        viewModel.getAllPhotos().observe(this, new Observer<List<Photo>>(){
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                System.out.println("onChanged: size " + photos.size());
                adapter.setPhotos(photos); /* update photos in the adapter */
                if (scan_started == false) {
                    /* update photo db asynchronously */
                    System.out.println("Starting scan async task");
                    new ScanAsyncTask(viewModel, getApplicationContext()).execute(photos);
                    scan_started = true;
                }
            }});

        /* set up grid */
        recycler_view = (RecyclerView) findViewById(R.id.grid_recycler_view);
        int numberOfColumns = 4;
        recycler_view.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        //recycler_view.setHasFixedSize(true);

        /* set recycle view adapter */
        adapter = new BrowseAdapter(this);
        //adapter.setHasStableIds(true);
        recycler_view.setAdapter(adapter);

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
            checkPermissions(this, getApplicationContext());
        }

        System.out.println("Got all permissions, resuming app");

        /* initialize easyimage */
        initEasyImage();
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

    private void initEasyImage() {
        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setCopyPickedImagesToPublicGalleryAppFolder(false)
                .setAllowMultiplePickInGallery(true);
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
                for (File f : imageFiles) {
                    indexFile(f.getAbsolutePath());
                    //viewModel.insertPhoto(new Photo(f.getAbsolutePath()));
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

    void indexFile(String path) {
        System.out.println("Putting into db: " + path);
        File sourceFile = new File(path);
        /* create thumbnail dir */
        File thumbnailDir = new File(getApplicationContext().getCacheDir(), "thumbnails");
        thumbnailDir.mkdir();
        /* generate thumbnail */
        Bitmap original_bitmap = BitmapFactory.decodeFile(path); /* read photo from disk */
        Bitmap thumbnail_bitmap = Bitmap.createScaledBitmap(original_bitmap, 100, 100, true);
        File thumbnail_file = new File(thumbnailDir, sourceFile.getName() + "-" + sourceFile.lastModified());
        try (FileOutputStream out = new FileOutputStream(thumbnail_file.getAbsolutePath())) {
            thumbnail_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* create Photo object to insert in db */
        Photo photo = new Photo(path);
        photo.setImThumbPath(thumbnail_file.getAbsolutePath());
        photo.setImTimestamp(sourceFile.lastModified());
        photo.setImGps("");
        photo.setImTitle("");

        /* delete from db  */
        viewModel.deletePhoto(path);

        /* insert in db */
        viewModel.insertPhoto(photo);
    }

    private class ScanAsyncTask extends AsyncTask<List<Photo>, Void, Void> {
        GalleryViewModel vm;
        private Context context;

        ScanAsyncTask(GalleryViewModel vm, Context context) {
            this.vm = vm;
            this.context = context;
        }

        @Override
        protected Void doInBackground(final List<Photo>... params) {
            /* current db photos */
            List<Photo> db_photos = params[0];
            Map<String, Photo> db_photos_map = new HashMap<String, Photo>();
            for (Photo p: db_photos) {
                db_photos_map.put(p.getImPath(), p);
            }
            System.out.println("Found " + db_photos_map.size() + " photos in db.");

            /* find all photos using mediastore */
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection);
            System.out.println("Indexing " + cursor.getCount() + " photos on phone.");
            Set<String> indexFiles = new HashSet<String>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String path = cursor.getString(cursor.getColumnIndex("_data"));
                indexFiles.add(path);
                System.out.println("Indexing: " + path);
                cursor.moveToNext();
            }

            /* delete photos from db that do not exist anymore */
            for (Photo photo : db_photos) {
                if (indexFiles.contains(photo.getImPath()) == false) {
                    /* delete this photo from db */
                    System.out.println("Db entry does not exist anymore, deleting: " + photo.getImPath());
                    vm.deletePhoto(photo.getImPath());
                }
            }

            /* delete stale thumbnails */
            File thumbnailDirectory = new File(context.getCacheDir(), "thumbnails");
            Map<String, Photo> thumb_photos_map = new HashMap<String, Photo>();
            for (Photo p: db_photos) {
                thumb_photos_map.put(p.getImThumbPath(), p);
            }
            for (File f : thumbnailDirectory.listFiles()) {
                if (!thumb_photos_map.containsKey(f.getAbsolutePath())) {
                    f.delete();
                }
            }

            /* update db */
            for (String path : indexFiles) {
                /* photo already exists in db */
                if (db_photos_map.containsKey(path)) {
                    System.out.println("Photo already exists in db: " + path);
                    long db_ts = db_photos_map.get(path).getImTimestamp();
                    long ix_ts = new File(path).lastModified();
                    /* check modified timestamp in db is old */
                    if (db_ts < ix_ts) {
                        System.out.println("Db entry old, needs updating: " + path);
                        /* need to update photo in db */
                        indexFile(path);
                    }
                } else {
                    indexFile(path);
                }
            }

            /* debug */
            System.out.println("Thumbnail dir: " + thumbnailDirectory.getAbsolutePath());
            for (File f : thumbnailDirectory.listFiles()) {
                System.out.println(f.getAbsolutePath());
            }

            return null;
        }
    }

    /**
     * check permissions are necessary starting from Android 6
     * if you do not set the permissions, the activity will simply not work and you will be probably baffled for some hours
     * until you find a note on StackOverflow
     *
     * @param context the calling context
     */
    public void checkPermissions(final Activity activity, final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            /* Check read permission */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    System.out.println("Requesting read permission");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }
            } else {
                grantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            /* Check write permission */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Writing external storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    System.out.println("Requesting write permission");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                grantedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}
