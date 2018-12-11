package wpi.team1006.cs4518finalproject;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



/**
 * A simple {@link Fragment} subclass.
 */
public class ViewImageFragment extends Fragment {
    private final int DISPLAY_X = 600;
    private final int DISPLAY_Y = 600;

    private Bitmap image;
    public boolean onDevice = true;//whether to do on-device processing or not
    private OnDeviceInferenceAsync loadingTask;
    protected String tags[];

    //listener to return to the previous fragment
    private View.OnClickListener returnListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).takePics();
        }
    };


    //listener to move to the DB image viewing fragment
    private View.OnClickListener dbImageListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).viewDBImages();
        }
    };

    private View.OnClickListener addListener = new View.OnClickListener(){
        public void onClick(View view){

            //if tags' length is 0, the inference hasn't finished yet and it shouldn't upload
            if(tags.length <=0) {
                Toast.makeText(getContext(), "Please wait for tags to be generated before uploading image to database", Toast.LENGTH_SHORT).show();
            }
            ((MainActivity)getActivity()).updateTags(tags);//set tags here
            ((MainActivity)getActivity()).onClickAdd(view);
        }
    };


    public ViewImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        //Attach all of our listeners
        Button picButton = (Button) getActivity().findViewById(R.id.takePicButton);
        picButton.setOnClickListener(returnListener);

        Button dbImagesButton = (Button) getActivity().findViewById(R.id.dbImagesButton);
        dbImagesButton.setOnClickListener(dbImageListener);

        Button addButton = (Button) getActivity().findViewById(R.id.dbButton);
        addButton.setOnClickListener(addListener);

        if(image != null){
            Bitmap displayImage = Bitmap.createScaledBitmap(image, DISPLAY_X, DISPLAY_Y, true);

            Bitmap topLeft = Bitmap.createBitmap(displayImage, 0, 0, displayImage.getWidth()/2, displayImage.getHeight()/2);
            Bitmap bottomLeft = Bitmap.createBitmap(displayImage, 0, displayImage.getWidth()/2, displayImage.getWidth()/2, displayImage.getHeight()/2);
            Bitmap topRight = Bitmap.createBitmap(displayImage, displayImage.getHeight()/2, 0, displayImage.getWidth()/2, displayImage.getHeight()/2);
            Bitmap bottomRight = Bitmap.createBitmap(displayImage, displayImage.getHeight()/2, displayImage.getWidth()/2, displayImage.getWidth()/2, displayImage.getHeight()/2);

            ImageView tl = getActivity().findViewById(R.id.imageTL);
            ImageView tr = getActivity().findViewById(R.id.imageTR);
            ImageView bl = getActivity().findViewById(R.id.imageBL);
            ImageView br = getActivity().findViewById(R.id.imageBR);
            tl.setImageBitmap(topLeft);
            tr.setImageBitmap(topRight);
            bl.setImageBitmap(bottomLeft);
            br.setImageBitmap(bottomRight);

            //now analyze image--in own thread so UI can finish loading
            if(onDevice){
                //on-device inference function call
                loadingTask = new OnDeviceInferenceAsync();
                loadingTask.execute(topLeft, topRight, bottomLeft, bottomRight);
            }
            else {
                //off device inference call
                String result = ((MainActivity)getActivity()).offDeviceProcessing(image);
                Log.d("GPROJ", "off device: " + result);
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        //the app will crash if the user navigates away from this fragment while its asyncTask is running.
        if(loadingTask != null){
            loadingTask.cancel(true);
        }
    }

    public void setDisplayImage(Bitmap newImage){
        image = newImage;
        tags = new String[0];//reset tags, since they are no longer accurate
    }

    //***************************AsyncTask to help with on-device inference********************************************
    private class OnDeviceInferenceAsync extends AsyncTask<Bitmap, Float, Long> {
        String[] results;
        String[] percentages;
        TextView[] tagDisplays;
        long time = 0;
        final String WORKING_MESSAGE = "Working...";

        protected void onPreExecute(){
            tagDisplays = new TextView[4];

            //our four text views for hte four sections of the image
            tagDisplays[0] = getActivity().findViewById(R.id.topLeftTag);
            tagDisplays[1] = getActivity().findViewById(R.id.topRightTag);
            tagDisplays[2] = getActivity().findViewById(R.id.bottomLeftTag);
            tagDisplays[3] = getActivity().findViewById(R.id.bottomRightTag);

            for(TextView t : tagDisplays) {
                t.setText(WORKING_MESSAGE);
            }
        }

        protected Long doInBackground(Bitmap... img_files) {
            results = new String[img_files.length];
            percentages = new String[img_files.length];

            for(int i = 0; i < img_files.length; i++){
                String[] res = ((MainActivity)getActivity()).onDeviceProcessing(img_files[i]);
                results[i] = res[0];//res[0] is the actual tag
                percentages[i] = res[1];//res[1] is the tag's likelihood in the inference
            }
            return time;
        }

        protected void onPostExecute(Long res){
            //get the tags we want to fill with this information and fill them
            for(int i = 0; i < tagDisplays.length; i++) {
                tagDisplays[i].setText(results[i] + ": "+ percentages[i] + "%");
            }

            tags = results;
        }

    }


}
