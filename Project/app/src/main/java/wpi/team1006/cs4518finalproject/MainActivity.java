package wpi.team1006.cs4518finalproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    ViewImageFragment viewImageFragment;
    TakeImageFragment takeImageFragment;
    ViewDBImagesFragment viewDBImagesFragment;

    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume(){
        super.onResume();

        viewImageFragment = new ViewImageFragment();
        takeImageFragment = new TakeImageFragment();
        viewDBImagesFragment = new ViewDBImagesFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        currentFragment = takeImageFragment;

        fragmentTransaction.add(R.id.mainContainer, currentFragment);
        fragmentTransaction.commit();

    }

    //change to the view image fragment
    public void viewImage(){
        changeFragment(viewImageFragment);
    }

    public void viewDBImages(){
        changeFragment(viewDBImagesFragment);
    }


    //change to the fragment to take pictures
    public void takePics(){
        changeFragment(takeImageFragment);
    }

    public void changeFragment(Fragment newFragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(currentFragment != null){
            fragmentTransaction.remove(currentFragment);
        }

        fragmentTransaction.add(R.id.mainContainer, newFragment);
        currentFragment = newFragment;

        fragmentTransaction.commit();
    }

}

