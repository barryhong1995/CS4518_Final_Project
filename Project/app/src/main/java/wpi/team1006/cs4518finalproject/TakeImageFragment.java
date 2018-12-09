package wpi.team1006.cs4518finalproject;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class TakeImageFragment extends Fragment {


    private View.OnClickListener viewImageListener = new View.OnClickListener(){
        public void onClick(View view){
            Log.d("GPROJ", "In onclick for view image");
            ((MainActivity)getActivity()).viewImage();
        }
    };
    private View.OnClickListener imageListener = new View.OnClickListener(){
        public void onClick(View view){
            //TODO: function to take a picture
        }
    };
    private View.OnClickListener addListener = new View.OnClickListener(){
        public void onClick(View view){
            //TODO: function to add to DB
        }
    };

    private View.OnClickListener inferenceListener = new View.OnClickListener(){
        public void onClick(View view){
            Log.d("GPROJ", "in listener");

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

}
