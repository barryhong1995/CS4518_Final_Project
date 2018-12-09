package wpi.team1006.cs4518finalproject;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewImageFragment extends Fragment {


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

        //TODO: Fill text fields and images!

    }


}
