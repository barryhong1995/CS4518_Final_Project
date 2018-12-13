package wpi.team1006.cs4518finalproject;


import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewImageFragment extends Fragment {
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


    private final int DISPLAY_X = 600;
    private final int DISPLAY_Y = 600;

    private Bitmap image;
    public boolean onDevice = true;//whether to do on-device processing or not
    private InferenceAsync loadingTask;
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
            if(image == null){
                Toast.makeText(getContext(), "You must take an image to upload it to the database", Toast.LENGTH_SHORT).show();

            }
            else if(tags.length <=0) {
                Toast.makeText(getContext(), "Please wait for tags to be generated before uploading image to database", Toast.LENGTH_SHORT).show();
            }
            else {
                ((MainActivity) getActivity()).updateTags(tags);//set tags here
                ((MainActivity) getActivity()).onClickAdd(view);
            }
        }
    };


    public ViewImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);

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
            BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("model/labels.txt")));
            for (int i = 0; i < labels.length; i++) {
                labels[i] = reader.readLine();
            }
        }
        catch(IOException e){
            Log.d("GPROJ", "Exception when filling model's label array: " + e);
        }

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
            loadingTask = new InferenceAsync();
            loadingTask.execute(topLeft, topRight, bottomLeft, bottomRight);
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


    //gets a MappedByteBuffer formatted correctly to set the tflite object to.
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getActivity().getAssets().openFd(getModelPath());
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



    public void setDisplayImage(Bitmap newImage){
        image = newImage;
        tags = new String[0];//reset tags, since they are no longer accurate
    }

    //***************************AsyncTask to help with on- and off-device inference********************************************
    private class InferenceAsync extends AsyncTask<Bitmap, Float, Long> {
        String[] results;//the actual tags
        String[] percentages;//the likelihood of the corresponding tag in results being accurate
        TextView[] tagDisplays;//the TextViews the above String arrays' text will go into
        long time = 0;
        final String WORKING_MESSAGE = "Working...";

        //Initializes the array of tags to fill in once the inference is completed
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

        //use the provided bitmaps to perform inference, using different methods mased on if it is on-
        //or off-device inference.
        protected Long doInBackground(Bitmap... img_files) {
            //initialize the arrays used to store the results
            results = new String[img_files.length];
            percentages = new String[img_files.length];

            //************Part 2 variables to measure time*******************
            long startTime;
            long endTime;

            if(onDevice) {
                //perform on-device processing for each provided bitmap
                startTime = SystemClock.uptimeMillis();
                for (int i = 0; i < img_files.length; i++) {
                    String[] res = onDeviceProcessing(img_files[i]);
                    results[i] = res[0];//res[0] is the actual tag
                    percentages[i] = res[1];//res[1] is the tag's likelihood in the inference
                }
                endTime = SystemClock.uptimeMillis();
                time = endTime - startTime;
                return time;
            }
            else{
                //perform off-device processing for each provided bitmap
                startTime = SystemClock.uptimeMillis();
                for (int i = 0; i < img_files.length; i++) {
                    String[] res = offDeviceProcessing(img_files[i]);
                    results[i] = res[0];//res[0] is the actual tag
                    percentages[i] = res[1];//res[1] is the tag's likelihood in the inference
                }
                endTime = SystemClock.uptimeMillis();
                time = endTime - startTime;
                return time;
            }
        }

        //sets the tags variable of the fragment class to the results (for adding to the database),
        //and updates the display on the app to show results.
        protected void onPostExecute(Long res){
            //get the tags we want to fill with this information and fill them
            //***********Part 2 code: print the time taken in the debugger for later reference*******************
            Log.d("GPROJ", "Inference time taken: " + time + "ms");
            for(int i = 0; i < tagDisplays.length; i++) {
                tagDisplays[i].setText(results[i] + ": "+ percentages[i] + "%");
            }
            tags = results;
        }

        //Performs inference on the passed in image
        private String[] onDeviceProcessing(Bitmap image){
            Bitmap sizedBMP = Bitmap.createScaledBitmap(image, SIZE_X, SIZE_Y, true);

            //put the bitmap into imageData as a ByteBuffer
            convertBitmapToByteBuffer(sizedBMP);

            float[][] labelProbArray = new float[DIM_BATCH_SIZE][labels.length];//label weight array for response

            tflite.run(imageData, labelProbArray);//runs the model

            //get and print the results
            float highest = -1;
            String guessLabel = "";
            for(int i = 0; i < labelProbArray[0].length; i++){
                if(labelProbArray[0][i] > highest){
                    highest = labelProbArray[0][i];
                    guessLabel = labels[i];
                }
            }
            return new String[] {guessLabel, (highest*100 + "")};
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

        //performs inference off-device on the passed-in image. Since it requires a network connection,
        //the processing runs in a separate thread.
        private String[] offDeviceProcessing(Bitmap image){

            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);//directory to store temp images

            try {
                //creates a temporary file for the Bitmap to process
                File tempFile = File.createTempFile("JPG_", ".jpg", directory);
                OutputStream out = new FileOutputStream(tempFile);

                //writes the bitmap to the file
                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                //create and send request
                String hostURL = "http://35.243.243.163:54321/inception";
                String[] response = sendRequest(tempFile, hostURL);

                tempFile.delete();//we don't need this file anymore, and don't want to take up unecessary space

                return response;
            }catch(Exception e) {
                Log.d("GPROJ", "Error making off-device inference temp file: " + e);
                return new String[]{"Off-device inference failed", "0.0"};
            }
        }

        //helper function to send the off-device request. If it gets back an odd response, it attempts another call
        //otherwise, returns a string array indicating the inference failed.
        private String[] sendRequest(File tempFile, String hostURL){
            MediaType mediaType = MediaType.parse("image/JPEG");

            String resp = "";

            try{
                //build the request
                RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                        .addFormDataPart("file", tempFile.getName(), RequestBody.create(mediaType, tempFile))
                        .build();
                Request request = new Request.Builder().url(hostURL).post(requestBody).build();
                OkHttpClient client = new OkHttpClient();

                //get the response
                Response response = client.newCall(request).execute();

                //since response errors can be fixed by retrying the request, this is in a
                //separate try block because it re-calls this method instead of returning a failure
                try {
                    resp = response.body().string();
                    String[] result = new String[2];
                    result[0] = resp.substring(0, resp.lastIndexOf(" "));//gets the space between the percentage and tag
                    result[1] = resp.substring(resp.lastIndexOf(" "), resp.indexOf("%"));

                    return result;
                } catch (Exception e){
                    Log.d("GPROJ", "Error with response: " + resp);
                    Log.d("GPROJ", "Error: " + e);
                    e.printStackTrace();

                    return sendRequest(tempFile, hostURL);//try again
                }
            } catch(Exception e){
                Log.d("GPROJ", "Failed to make request: " + e);
                e.printStackTrace();

                return new String[]{"Off-device inference failed", "0.0"};
            }
        }


    }


}
