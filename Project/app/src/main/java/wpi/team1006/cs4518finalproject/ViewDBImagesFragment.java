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
public class ViewDBImagesFragment extends Fragment {

    //listener to return to the image-taking fragment
    private View.OnClickListener returnListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).takePics();
        }
    };

    //listener to return to the previous fragment
    private View.OnClickListener viewMoreListener = new View.OnClickListener(){
        public void onClick(View view){
            //TODO: call to database, asking for more specific images
        }
    };


    public ViewDBImagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_dbimages, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        //Attach all of our listeners
        Button picButton = (Button) getActivity().findViewById(R.id.takePicButton);
        picButton.setOnClickListener(returnListener);

        Button viewMoreButton = (Button) getActivity().findViewById(R.id.viewMoreButton);
        viewMoreButton.setOnClickListener(viewMoreListener);

    }

}
