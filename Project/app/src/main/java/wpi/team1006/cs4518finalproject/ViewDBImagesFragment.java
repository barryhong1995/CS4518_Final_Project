package wpi.team1006.cs4518finalproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewDBImagesFragment extends Fragment {
    private RecyclerView rView;
    private ImageRecyclerAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    //listener to return to the image-taking fragment
    private View.OnClickListener returnListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).takePics();
        }
    };

    //listener to return to the previous fragment
    private View.OnClickListener viewMoreListener = new View.OnClickListener(){
        public void onClick(View view){
            ImageRecyclerAdapter.ViewHolder[] selected = new ImageRecyclerAdapter.ViewHolder[0];
            selected = adapter.getSelected().toArray(selected);

            Log.d("GPROJ", "size of selected: "+selected.length);

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

        //recycler view initialization
        rView = (RecyclerView) getActivity().findViewById(R.id.imagesRecyclerView);
        //rView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rView.setLayoutManager(gridLayoutManager);

        //this should happen every time data is gotten
        Bitmap[] data = new Bitmap[25];

        try{
            InputStream image_stream = getActivity().getAssets().open("imgs/cannon.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(image_stream);
            Bitmap sizedBMP = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            for(int i = 0; i < data.length; i++) {
                data[i] = sizedBMP;
            }
        }
        catch (Exception e){
            Log.d("GPROJ", "Exception occurred: "+e.toString());
        }


        adapter = new ImageRecyclerAdapter(data);
        rView.setAdapter(adapter);



    }

}
