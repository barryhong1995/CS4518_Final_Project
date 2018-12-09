package wpi.team1006.cs4518finalproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageRecyclerAdapter extends Adapter {
    private Bitmap[] images;
    private ViewHolder[] viewInfo;

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


    public ImageRecyclerAdapter(Bitmap[] queryResults) {
        images = queryResults;
        viewInfo = new ViewHolder[images.length];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
       ImageView view = new ImageView(viewGroup.getContext());
       view.setPadding(4, 8, 4, 8);
       view.setActivated(false);

       ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ViewHolder)viewHolder).imageView.setImageBitmap(images[position]);
        viewInfo[position] = (ViewHolder)viewHolder;
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public ArrayList<ViewHolder> getSelected(){
        ArrayList<ViewHolder> viewArray = new ArrayList<ViewHolder>();

        for(int i = 0; i < viewInfo.length; i++){
            if(viewInfo[i] != null && viewInfo[i].imageView.isActivated()){
                viewArray.add(viewInfo[i]);
            }
        }

        return viewArray;
    }



}
