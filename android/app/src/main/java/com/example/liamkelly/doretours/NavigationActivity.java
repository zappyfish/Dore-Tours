package com.example.liamkelly.doretours;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.liamkelly.doretours.data.location.GPSFragment;


public class NavigationActivity extends AppCompatActivity implements NavigationDestinationFragment.OnFragmentInteractionListener,
        GPSFragment.OnFragmentInteractionListener, NavigationCameraFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        final ImageView imageView = findViewById(R.id.image_view);

        GPSFragment gpsFragment = new GPSFragment();
        NavigationDestinationFragment navDestFragment = new NavigationDestinationFragment();
        NavigationCameraFragment navCameraFragment = new NavigationCameraFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(gpsFragment, "gpsFrag")
                .add(navDestFragment, "navDestFrag")
                .add(navCameraFragment, "navImgFrag")
                .commit();
        navCameraFragment.setImageReadyCallback(new NavigationCameraFragment.ImageReady() {
            @Override
            public void onImageReady(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: Complete me
    }
}
