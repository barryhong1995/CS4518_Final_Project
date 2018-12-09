package wpi.team1006.cs4518finalproject;

<<<<<<< HEAD
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
=======
import android.graphics.Bitmap;
>>>>>>> 318811b019d644307fa09bf940a782c282ad6a32
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    // Initialize variables
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private CollectionReference collectionRef;
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

        viewImageFragment = new ViewImageFragment();
        takeImageFragment = new TakeImageFragment();
        viewDBImagesFragment = new ViewDBImagesFragment();

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

    //change to the view image fragment
    public void viewImage(Bitmap image){
        viewImageFragment.setDisplayImage(image);

        changeFragment(viewImageFragment);



    }

    public void viewDBImages(){
        changeFragment(viewDBImagesFragment);
    }


    //change to the fragment to take pictures
    public void takePics(){
        changeFragment(takeImageFragment);
    }

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
}

