package com.example.liamkelly.doretours;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liamkelly.doretours.data.location.Building;
import com.example.liamkelly.doretours.data.location.Campus;
import com.example.liamkelly.doretours.data.location.CampusManager;
import com.example.liamkelly.doretours.data.location.HardcodedBuilding;
import com.example.liamkelly.doretours.data.location.GPSFragment;
import com.example.liamkelly.doretours.data.location.GPSManager;
import com.example.liamkelly.doretours.data.pose.PoseManager;
import com.example.liamkelly.doretours.upload.LabeledData;
import com.example.liamkelly.doretours.upload.UploadManager;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class TrainingActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener, GPSFragment.OnFragmentInteractionListener {

    private static final String TAG = "TrainingActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String NOTHING_IDENTIFIED = "Nothing Identified";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;

    private String mBuildingSelection;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    private TextView mAngleTextView;

    private boolean mInsideBuilding = true;

    private double mLastLatitude = -1;

    private double mLastLongitude = -1;

    private boolean mInCampus = false;

    private Campus mCampus;

    private Building mLookingAt;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                    break;
            }
        }
    };

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCampus = CampusManager.getInstance().getActiveCampus();

        GPSFragment gpsFragment = new GPSFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(gpsFragment, "gpsFrag")
                .commit();

        mCameraView = (CameraView) findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        String[] buildingNames = new String[mCampus.getBuildings().size() + 1];
        for (int i = 0; i < mCampus.getBuildings().size(); i++) {
            buildingNames[i] = mCampus.getBuildings().get(i).getName();
        }

        buildingNames[mCampus.getBuildings().size()] = NOTHING_IDENTIFIED;

        final Spinner spinner = (Spinner) findViewById(R.id.buildings);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                buildingNames);
        spinner.setAdapter(adapter);

        spinner.setSelection(mCampus.getBuildings().size());

        mBuildingSelection = (String)spinner.getItemAtPosition(mCampus.getBuildings().size());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBuildingSelection = (String)parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        GPSManager.getInstance(this).addCallback(new GPSManager.Callback() {
            @Override
            public void onData(double latitude, double longitude) {
                if (CampusManager.getInstance().getActiveCampus().inCampus(latitude, longitude)) {
                    mInCampus = true;
                    mLastLatitude = latitude;
                    mLastLongitude = longitude;
                    int cnt = 0;
                    for (Building building : mCampus.getBuildings()) {
                        if (building.inBuilding(latitude, longitude)) {
                            mBuildingSelection = building.getName();
                            mInsideBuilding = true;
                            break;
                        }
                        cnt++;
                    }
                    if (cnt == mCampus.getBuildings().size()) {
                        mBuildingSelection = NOTHING_IDENTIFIED;
                        mInsideBuilding = false;
                    }
                    spinner.setSelection(cnt); // The last entry, if we don't hit anything, is "Nothing Identified"
                } else {
                    mInCampus = false;
                }
            }
        });

        mAngleTextView = (TextView) findViewById(R.id.angles);

        PoseManager.getInstance(this).addCallback(new PoseManager.PoseCallback() {
            @Override
            public void callback(double northMagnitude, double eastMagnitude, double zAngle) {
                if (mInCampus) {
                    if (!mInsideBuilding && zAngle >= 45.0 && zAngle <= 135) {
                        Building lookingAt = mCampus.getBuildingInView(northMagnitude, eastMagnitude, mLastLatitude, mLastLongitude);
                        if (lookingAt != null) {
                            mLookingAt = lookingAt;
                            mAngleTextView.setText("Looking at: " + lookingAt.getName());
                        } else {
                            mAngleTextView.setText("Not looking at anything");
                        }
                    } else {
                        mAngleTextView.setText("Inside building or looking too far up or down");
                    }
                } else {
                    mAngleTextView.setText("Outside " + CampusManager.getInstance().getActiveCampus().getName());
                }
            }

            @Override
            public void callback(float roll, float pitch, float yaw) {
                // mAngleTextView.setText("roll: " + roll + ", pitch: " + pitch + ", yaw: " + yaw);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && mCameraView != null) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        if (mCameraView != null) {
            mCameraView.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aspect_ratio:
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (mCameraView != null
                        && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                    final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                    final AspectRatio currentRatio = mCameraView.getAspectRatio();
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                            .show(fragmentManager, FRAGMENT_DIALOG);
                }
                return true;
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show();
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    // upload image here
                    // Compute which building
                    if (mLookingAt != null) {
                        LabeledData dataPoint = new LabeledData(mLookingAt.getName(), data);
                        UploadManager.getInstance().uploadDataPoint(dataPoint, TrainingActivity.this);
                    }
                }
            });
        }

    };

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }


}