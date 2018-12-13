package wpi.team1006.cs4518finalproject;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewDBImagesFragment extends Fragment {
    private final int SPAN_COUNT = 3;

    private List<DataImage> data;//used to initialize the adapter, and should be changed every time data is fetched
    private RecyclerView rView;
    private ImageRecyclerAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    private CollectionReference collectionRef;//used to query the database, and also to pass to the Adapter so it knows where to load images from

    //listener to return to the image-taking fragment
    private View.OnClickListener returnListener = new View.OnClickListener(){
        public void onClick(View view){
            ((MainActivity)getActivity()).takePics();
        }
    };

    //listener to return to the previous fragment
    private View.OnClickListener viewMoreListener = new View.OnClickListener(){
        public void onClick(View view){
            DataImage selected = adapter.getSelectedInfo();
            if(selected.getTags().size() > 0) {
                //get more specific data filtered based on tags
                filterData(selected.getTags());
            }
        }
    };


    public ViewDBImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        collectionRef = ((MainActivity)getActivity()).getDBRef();
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
        rView = getActivity().findViewById(R.id.imagesRecyclerView);
        //rView.setHasFixedSize(true);

        gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        rView.setLayoutManager(gridLayoutManager);

        //this should happen every time data is gotten
        obtainImageDatabase();

    }

    private void filterData(List<String> tagList){
        List<DataImage> newData = new ArrayList<>();

        //checks each piece of data to see if it belongs in the new data to display
        for(DataImage d : data){
            //checks each provided tag. If the current data image has at least one of them, adds it
            //to the newData list
            boolean sharesTag = false;
            for(String tag : tagList){
                if(d.getTags().contains(tag)){
                    sharesTag = true;
                }
            }
            if(sharesTag){
                newData.add(d);
            }
        }

        //changes the adapter to be one using the filtered data
        adapter = new ImageRecyclerAdapter(newData, ((MainActivity)getActivity()).getDBStorageRef());
        rView.setAdapter(adapter);
    }

    // Obtain a list of DataImage from Firestore Database
    public void obtainImageDatabase() {
        collectionRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            data = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DataImage tempImg = new DataImage();
                                tempImg.setImage((String) document.getData().get("image"));
                                tempImg.setTime((String) document.getData().get("time"));
                                ArrayList tagList = (ArrayList) document.getData().get("tags");
                                tempImg.setTags(tagList);
                                data.add(tempImg);

                            }
                            adapter = new ImageRecyclerAdapter(data, ((MainActivity)getActivity()).getDBStorageRef());
                            rView.setAdapter(adapter);
                        } else {
                            Log.d("MainActivity.java::", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}
