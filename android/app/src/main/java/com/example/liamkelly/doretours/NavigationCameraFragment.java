package com.example.liamkelly.doretours;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NavigationCameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NavigationCameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NavigationCameraFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageReady mImageReady;

    public NavigationCameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NavigationCameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NavigationCameraFragment newInstance(String param1, String param2) {
        NavigationCameraFragment fragment = new NavigationCameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Camera camera = getCameraInstance();
        SurfaceTexture surfaceTexture = new SurfaceTexture(MODE_PRIVATE);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (Exception e) {

        }
        camera.startPreview();
        final Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                camera.takePicture(null, null, mPicture);
                h.postDelayed(this, 20);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation_camera2, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options scalingOptions = new BitmapFactory.Options();
            final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, scalingOptions);
            if (mImageReady != null) {
                mImageReady.onImageReady(bmp);
            }
        }
    };

    public void setImageReadyCallback(ImageReady imageReady) {
        mImageReady = imageReady;
    }

    public interface ImageReady {
        void onImageReady(Bitmap bitmap);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
