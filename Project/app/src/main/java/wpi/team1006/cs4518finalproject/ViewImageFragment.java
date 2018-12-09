package wpi.team1006.cs4518finalproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewImageFragment extends Fragment {
    private Bitmap image;

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

        if(image != null){
            Bitmap displayImage = Bitmap.createScaledBitmap(image, 500, 500, true);

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
        }

    }

    public void setDisplayImage(Bitmap newImage){
        image = newImage;

    }


}
