package wpi.team1006.cs4518finalproject;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    // Initialize variables
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private CollectionReference collectionRef;

    private Uri photoURI;

    //fragment variables
    ViewImageFragment viewImageFragment;
    TakeImageFragment takeImageFragment;
    ViewDBImagesFragment viewDBImagesFragment;
    Fragment currentFragment;

    //Variables for inference
    MappedByteBuffer tfliteModel;
    Interpreter tflite;
    ByteBuffer imageData;
    String[] labels;
    private final int DIM_BATCH_SIZE = 1;//dimensions of the batch size
    private final int SIZE_X = 299;//x dimension of input
    private final int SIZE_Y = 299;
    private final int DIM_PIXEL_SIZE = 3;//number of color channels per pixel
    private final int NUM_BYTES_PER_CHANNEL = 4;
    private final int IMAGE_MEAN = 128;//used in standardizing the images' brightnesses
    private final float IMAGE_STD = 128.0f;//used in standardizing the images' brightnesses
    private final int LABEL_ARRAY_SIZE = 1001;//number of entries in the labels.txt file


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

        //initialize tensorflow things
        //get our model
        try {
            tfliteModel = loadModelFile();
            tflite = new Interpreter(tfliteModel);
        }
        catch(IOException e){
            Log.d("GPROJ", "Exception when making model: " + e);
        }
        //allocate the ByteBuffer
        imageData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * SIZE_X * SIZE_Y * DIM_PIXEL_SIZE * NUM_BYTES_PER_CHANNEL);
        imageData.order(ByteOrder.nativeOrder());

        //initialize the array of labels
        labels = new String[LABEL_ARRAY_SIZE];
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("model/labels.txt")));
            for (int i = 0; i < labels.length; i++) {
                labels[i] = reader.readLine();
            }
        }
        catch(IOException e){
            Log.d("GPROJ", "Exception when filling model's label array: " + e);
        }


    }

    //gets a MappedByteBuffer formatted correctly to set the tflite object to.
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        long startingOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return channel.map(FileChannel.MapMode.READ_ONLY, startingOffset, declaredLength);
    }
    //returns the model path for the inference model
    private String getModelPath(){
        return "model/inception_v3.tflite";
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

    public void setUri(Uri newUri){
        photoURI = newUri;
    }

    // Button to add new image information to database and image to storage
    public void onClickAdd(View v) {
        // Add image information to database
        DataImage imgData = new DataImage();
        imgData.setImage(getFileName(photoURI));
      //  imgData.setTags();
        collectionRef.add(imgData);

        // Add image  data to storage
        if(photoURI != null)
        {
            StorageReference ref = storageReference.child("images/"+ getFileName(photoURI));
            ref.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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

    //change to the view image fragment. The image passed in is the one which will be displayed.
    public void viewImage(Bitmap image){
        viewImageFragment.setDisplayImage(image);

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


    //Performs inference on the passed in image
    public String onDeviceProcessing(Bitmap image){
        Bitmap sizedBMP = Bitmap.createScaledBitmap(image, SIZE_X, SIZE_Y, true);

        //put the bitmap into imageData as a ByteBuffer
        convertBitmapToByteBuffer(sizedBMP);

        float[][] labelProbArray = new float[DIM_BATCH_SIZE][labels.length];//label weight array for response

        //variables to get the time it took to perform the inference
        //long startTime;
        //long endTime;

        //startTime = SystemClock.uptimeMillis();
        tflite.run(imageData, labelProbArray);//runs the model
        //endTime = SystemClock.uptimeMillis();

        //display the time
        //TextView latencyText = findViewById(R.id.latencyText);
        //latencyText.setText((endTime - startTime) + "ms");


        //get and print the results
        float highest = -1;
        String guessLabel = "";
        for(int i = 0; i < labelProbArray[0].length; i++){
            if(labelProbArray[0][i] > highest){
                highest = labelProbArray[0][i];
                guessLabel = labels[i];
            }
        }
        return (guessLabel + ": " + (highest*100) + "%");
    }

    //Helper function for Tensorflow model to convert the Bitmap into the proper format for the model
    private void convertBitmapToByteBuffer(Bitmap bitmap){
        if(imageData == null){
            return;
        }
        int[] intVals = new int[SIZE_X*SIZE_Y];

        imageData.rewind();
        bitmap.getPixels(intVals, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        //converts the image to floating point
        int pixel = 0;
        for(int i = 0; i < SIZE_X; ++i){
            for(int j = 0; j < SIZE_Y; ++j){
                final int val = intVals[pixel++];
                addPixelValue(val);
            }
        }
    }
    //adjusts the pixels to account for brightness for the TensorFlow Model
    private void addPixelValue(int pixelValue){
        imageData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imageData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imageData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }


    public String offDeviceProcessing(Bitmap image){
        return "Working...";

    }





}

