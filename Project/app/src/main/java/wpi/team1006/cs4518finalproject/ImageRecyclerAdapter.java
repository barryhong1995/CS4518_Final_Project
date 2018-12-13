package wpi.team1006.cs4518finalproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageRecyclerAdapter extends Adapter {
    private final int DISPLAY_X = 300;
    private final int DISPLAY_Y = 300;
    private List<DataImage> images;
    private ViewHolder[] viewInfo;
    private StorageReference storageRef;//reference for where to load images from

    public /*static*/ class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        private View.OnClickListener listener = new View.OnClickListener(){
            public void onClick(View view){
                if(view.isActivated()){
                    markDeselected(view);
                }
                else{
                    ViewHolder[] selected = new ImageRecyclerAdapter.ViewHolder[0];
                    selected = getSelected().toArray(selected);

                    for(int i = 0; i < selected.length; i++){
                        markDeselected(selected[i].imageView);
                    }

                    markSelected(view);
                }
            }
        };

        private void markSelected(View view){
            view.setBackgroundColor(Color.GREEN);
            view.setActivated(true);
        }

        private void markDeselected(View view){
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setActivated(false);
        }

        public ViewHolder(ImageView view) {
            super(view);
            imageView = view;
            imageView.setOnClickListener(listener);
        }
    }


    public ImageRecyclerAdapter(List<DataImage> queryResults, StorageReference dbRef) {
        images = queryResults;
        viewInfo = new ViewHolder[images.size()];
        storageRef = dbRef;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
       ImageView view = new ImageView(viewGroup.getContext());
       view.setPadding(0, 8, 0, 8);
       view.setActivated(false);

       ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        viewInfo[position] = (ViewHolder)viewHolder;
        getImgBitmap(position, images.get(position).getImage());

    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    //gets the information of the selected element
    public DataImage getSelectedInfo(){

        for(int i = 0; i < viewInfo.length; i++){
            if(viewInfo[i] != null && viewInfo[i].imageView.isActivated()){
                return images.get(i);
            }
        }

        return new DataImage();//nothing was selected
    }

    //to get which element, if any, are currently selected
    public ArrayList<ViewHolder> getSelected(){
        ArrayList<ViewHolder> viewArray = new ArrayList<ViewHolder>();

        for(int i = 0; i < viewInfo.length; i++){
            if(viewInfo[i] != null && viewInfo[i].imageView.isActivated()){
                viewArray.add(viewInfo[i]);
            }
        }

        return viewArray;
    }

    private void getImgBitmap(final int position, String imgName) {
        StorageReference ref = storageRef.child("images/"+imgName);
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap imgBmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    Bitmap sizedBMP = Bitmap.createScaledBitmap(imgBmp, DISPLAY_X, DISPLAY_Y, true);
                    viewInfo[position].imageView.setImageBitmap(sizedBMP);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("GRPOJ", "couldn't download image!!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
