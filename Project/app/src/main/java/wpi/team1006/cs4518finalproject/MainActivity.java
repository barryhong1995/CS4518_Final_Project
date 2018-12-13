package wpi.team1006.cs4518finalproject;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Initialize variables
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private CollectionReference collectionRef;
    private Query mQuery;
    private int LIMIT = 50;

    //variables to help transfer data between fragments and to database
    private Uri photoURI;
    private Bitmap image;
    private String[] tags;

    //fragment variables
    ViewImageFragment viewImageFragment;
    TakeImageFragment takeImageFragment;
    ViewDBImagesFragment viewDBImagesFragment;
    Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore and Storage
        initFirestore();
        initStorage();

        // Get a reference to the pet collection
        collectionRef = mFirestore.collection("mass-data");

        //fragment stuff
        //get the fragments we will use
        viewImageFragment = new ViewImageFragment();
        takeImageFragment = new TakeImageFragment();
        viewDBImagesFragment = new ViewDBImagesFragment();

        //set the currently displayed fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        currentFragment = takeImageFragment;

        fragmentTransaction.add(R.id.mainContainer, currentFragment);
        fragmentTransaction.commit();
    }

    // Initialize Firestore
    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    // Initialize Storage
    private void initStorage() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    //update activity's image and URI for when updating to database
    public void updateImage(Uri newUri, Bitmap newImage){
        photoURI = newUri;
        image = newImage;
    }
    //updates activity's tags for ease of access when adding to database
    public void updateTags(String[] newTags){
        tags = newTags;
    }

    // Button to add new image information to database and image to storage
    public void onClickAdd(View v) {
        // Obtain current date and time
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(new Date());

        // Add image information to database
        DataImage imgData = new DataImage();
        imgData.setImage(getFileName(photoURI));
        imgData.setTags(arrayToListWithoutDuplicates(tags));
        imgData.setTime(timeStamp);
        collectionRef.add(imgData);

        // Add image  data to storage
        if(photoURI != null)
        {
            final long startTime = SystemClock.uptimeMillis();// **Part 2 Code: start time for DB upload**
            StorageReference ref = storageReference.child("images/"+ getFileName(photoURI));
            ref.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    long endTime = SystemClock.uptimeMillis();// **Part 2 code: end time for DB upload and display difference**
                    Log.d("GPROJ", "Database upload complete: " + (endTime - startTime) + "ms");
                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    // To be used if needed to check progress
                }
            });
        }
    }

    //change to the view image fragment. The image stored in mainActivity is the one that will be displayed.
    //the boolean onDevice indicates if the fragment should try to do onDevice or offDevice processing.
    public void viewImage(boolean onDevice){
        if(image != null) {
            viewImageFragment.setDisplayImage(image);
        }
        viewImageFragment.onDevice = onDevice;
        changeFragment(viewImageFragment);
    }

    //changes fragment displayed to the one to view DB images
    public void viewDBImages(){
        changeFragment(viewDBImagesFragment);
    }


    //change to the fragment to take pictures
    public void takePics(){
        changeFragment(takeImageFragment);
    }

    //exchanges the current fragment for a new one, and updates which fragment is being displayed.
    public void changeFragment(Fragment newFragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(currentFragment != null){
            fragmentTransaction.remove(currentFragment);
        }

        fragmentTransaction.add(R.id.mainContainer, newFragment);
        currentFragment = newFragment;

        fragmentTransaction.commit();
    }

    // Helper function to get file name from URI
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //returns the collection reference for ViewDB images fragment
    public CollectionReference getDBRef(){
        return collectionRef;
    }
    //returns the storage reference for ViewDB images fragment
    public StorageReference getDBStorageRef(){
        return storageReference;
    }

    //helper method to remove repeats in the tags when uploading to database
    private List<String> arrayToListWithoutDuplicates(String[] array){
        List<String> list = new ArrayList<String>();

        for(int i = 0; i < array.length; i++) {
            if (!list.contains(array[i])) {
                list.add(array[i]);
            }
        }

        return list;
    }


}

