package wpi.team1006.cs4518finalproject;


import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TakeImageFragment extends Fragment {
    public int REQUEST_IMAGE_CAPTURE = 1;
    private File directory;
    private String mCurrentPhotoPath;
    private Bitmap image;



    private View.OnClickListener viewImageListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).viewImage(image);
        }
    };
    private View.OnClickListener imageListener = new View.OnClickListener(){
        public void onClick(View view){
            takePicture();
        }
    };
    private View.OnClickListener addListener = new View.OnClickListener(){
        public void onClick(View view){
            //TODO: function to add to DB
        }
    };

    private View.OnClickListener inferenceListener = new View.OnClickListener(){
        public void onClick(View view){
            ToggleButton onDevice = getActivity().findViewById(R.id.toggleButton);
            if(onDevice.isChecked()){
                //on-device inference function call
                Log.d("GPROJ", "on device");
            }
            else {
                //off device inference call
                Log.d("GPROJ", "off device");
            }
        }
    };


    public TakeImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);//directory to store temp images
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        //get our buttons and attach listeners to them.
        Button viewButton = (Button) getActivity().findViewById(R.id.viewImageButton);
        viewButton.setOnClickListener(viewImageListener);

        Button addButton = (Button) getActivity().findViewById(R.id.dbButton);
        addButton.setOnClickListener(addListener);

        Button imageButton = (Button) getActivity().findViewById(R.id.takePicButton);
        imageButton.setOnClickListener(imageListener);

        Button analyzeButton = (Button) getActivity().findViewById(R.id.analyzeButton);
        analyzeButton.setOnClickListener(inferenceListener);

    }

    public void takePicture(){
        //creates an intent to take a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File tempFile = File.createTempFile("JPG_", ".jpg", directory);
            mCurrentPhotoPath = tempFile.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), "wpi.team1006.cs4518finalproject", tempFile));
        }
        catch(Exception e){
            Log.d("GPROJ", "TempFile failed to be added to extra!");
            Log.d("GPROJ", "Error was: " + e.toString());
        }

        List<ResolveInfo> activities = getActivity().getPackageManager().queryIntentActivities(takePictureIntent, 0);//get all activities that match the intent

        if (activities.size() > 0) {//if there is an app that matches, start the activity
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {

            ImageView mCameraDisplayView = (ImageView) getActivity().findViewById(R.id.imageView);
            Bitmap origImage = BitmapFactory.decodeFile(mCurrentPhotoPath);

            image = Bitmap.createScaledBitmap(origImage, origImage.getWidth()/2, origImage.getHeight()/2, true);

            mCameraDisplayView.setImageBitmap(image);
        }
    }

}
