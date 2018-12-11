//package com.example.liamkelly.doretours;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.graphics.SurfaceTexture;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.Image;
//import android.media.ImageReader;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.Size;
//import android.view.LayoutInflater;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import java.lang.ref.WeakReference;
//import java.nio.ByteBuffer;
//import java.util.Collections;
//
//import androidx.core.app.ActivityCompat;
//import androidx.fragment.app.Fragment;
//
//public class NavigationImageFragment extends Fragment
//        implements ActivityCompat.OnRequestPermissionsResultCallback {
//
//    private CameraDevice mCameraDevice;
//    private CameraManager mCameraManager;
//    private int mCameraFacing;
//    private String mCameraId;
//    private static int CAMERA_REQUEST_CODE = 1;
//
//    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
//    private Size mPreviewSize;
//    private HandlerThread mBackgroundThread;
//    private Handler mBackgroundHandler;
//    private CameraDevice.StateCallback mStateCallback;
//    private CaptureRequest.Builder mCaptureRequestBuilder;
//    private CaptureRequest mCaptureRequest;
//    private CameraCaptureSession mCameraCaptureSession;
//
//    private ImageReader mImageReader;
//    private ProcessCamera mProcessCamera;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_navigation_image, container, false);
//    }
//
//    @Override
//    public void onViewCreated(final View view, Bundle savedInstanceState) {
//
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        openBackgroundThread();
//        setUpCamera();
//        openCamera();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (mProcessCamera == null) {
//            mProcessCamera = new ProcessCamera(null);
//        }
//        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
//
//        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
//        mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
//
//        mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//                setUpCamera();
//                openCamera();
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//            }
//        };
//
//        mStateCallback = new CameraDevice.StateCallback() {
//            @Override
//            public void onOpened(CameraDevice cameraDevice) {
//                mCameraDevice = cameraDevice;
//                createPreviewSession();
//            }
//
//            @Override
//            public void onDisconnected(CameraDevice cameraDevice) {
//                cameraDevice.close();
//                mCameraDevice = null;
//            }
//
//            @Override
//            public void onError(CameraDevice cameraDevice, int error) {
//                cameraDevice.close();
//                mCameraDevice = null;
//            }
//        };
//
//    }
//
//    private void setUpCamera() {
//        try {
//            for (String cameraId : mCameraManager.getCameraIdList()) {
//                CameraCharacteristics cameraCharacteristics =
//                        mCameraManager.getCameraCharacteristics(cameraId);
//                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
//                        mCameraFacing) {
//                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
//                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                    mPreviewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[];
//                    mCameraId = cameraId;
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openCamera() {
//        try {
//            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
//                    == PackageManager.PERMISSION_GRANTED) {
//                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openBackgroundThread() {
//        mBackgroundThread = new HandlerThread("camera_background_thread");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        closeCamera();
//        closeBackgroundThread();
//    }
//
//    private void closeCamera() {
//        if (mCameraCaptureSession != null) {
//            mCameraCaptureSession.close();
//            mCameraCaptureSession = null;
//        }
//
//        if (mCameraDevice != null) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//    }
//
//    private void closeBackgroundThread() {
//        if (mBackgroundHandler != null) {
//            mBackgroundThread.quitSafely();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        }
//    }
//
//    private void createPreviewSession() {
//        try {
//            mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(),mPreviewSize.getHeight(),
//                    ImageFormat.JPEG, /*maxImages*/2);
//            mImageReader.setOnImageAvailableListener(
//                     mProcessCamera, mBackgroundHandler);
//            Surface receiveSurface = mImageReader.getSurface();
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(receiveSurface);
//
//            mCameraDevice.createCaptureSession(Collections.singletonList(receiveSurface),
//                    new CameraCaptureSession.StateCallback() {
//
//                        @Override
//                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
//                            if (mCameraDevice == null) {
//                                return;
//                            }
//
//                            try {
//                                mCaptureRequest = mCaptureRequestBuilder.build();
//                                mCameraCaptureSession = cameraCaptureSession;
//                                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,
//                                        null, mBackgroundHandler);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
//
//                        }
//                    }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void setNewImageReady(NewImageReady newImageReady) {
//        if (mProcessCamera == null) {
//            mProcessCamera = new ProcessCamera(newImageReady);
//        } else {
//            mProcessCamera.mNewImageReady = newImageReady;
//        }
//    }
//
//    public interface NewImageReady {
//        void onNewImage(Bitmap bitmap);
//    }
//
//    public static class ProcessCamera implements ImageReader.OnImageAvailableListener {
//
//        private NewImageReady mNewImageReady;
//
//        public ProcessCamera(NewImageReady newImageReady) {
//            mNewImageReady = newImageReady;
//        }
//
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            Image img = null;
//            Bitmap bitmap;
//            try {
//                img = reader.acquireNextImage();
//                ByteBuffer buffer = img.getPlanes()[0].getBuffer();
//                buffer.rewind();
//                byte[] data = new byte[buffer.capacity()];
//                buffer.get(data);
//                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                Matrix matrix = new Matrix();
//                matrix.postRotate(90);
//                final Bitmap setBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                if (mNewImageReady != null) {
//                    mNewImageReady.onNewImage(setBitmap);
//                }
//            } catch (Exception e) {
//
//            }
//            if (img != null) {
//                img.close();
//            }
//        }
//    }
//}