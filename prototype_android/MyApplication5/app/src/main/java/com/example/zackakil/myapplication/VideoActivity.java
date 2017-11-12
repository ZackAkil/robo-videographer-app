package com.example.zackakil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class VideoActivity extends AppCompatActivity {

    private TextureView nTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    setupCamera(i, i1);
                    openCamera();

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            };

    private Size mPreviewSize;
    private String mCameraId;

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    Toast.makeText(getBaseContext(), "Camera open", Toast.LENGTH_SHORT).show();
                    mCameraDevice = cameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    cameraDevice.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    cameraDevice.close();
                    mCameraDevice = null;
                }
            };

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;

    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback=
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }
            };

    private HandlerThread mHandelerThread;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        nTextureView = (TextureView) findViewById(R.id.textureView);
    }

    @Override
    public void onPause(){
        closeCamera();

        closeBackgroundThread();

        super.onPause();
    }


    @Override
    public void onResume(){
        super.onResume();

        openBackgroundThread();

        if(nTextureView.isAvailable()){
            setupCamera(nTextureView.getWidth(), nTextureView.getHeight());
        }else{
            nTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void setupCamera(int width, int height){

        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {

            for (String id : cm.getCameraIdList()) {

                CameraCharacteristics camCars = cm.getCameraCharacteristics(id);

                Log.d("My App", id);
                if (cm.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                }
                StreamConfigurationMap map = camCars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = getPreferedPreviewSize(map.getOutputSizes(SurfaceTexture.class), height, width);
                mCameraId = id;
                return;
            }

        }catch(CameraAccessException ex){
            Toast.makeText(getBaseContext(), "Camera exception", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }


        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cm.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);

        }catch(SecurityException ex){

            Toast.makeText(getBaseContext(), "Permission exception", Toast.LENGTH_SHORT).show();
        }
        catch(CameraAccessException ex){
            Toast.makeText(getBaseContext(), "Camera exception", Toast.LENGTH_SHORT).show();
        }

    }

    private void closeCamera(){

        if (mCameraCaptureSession != null){
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void createCameraPreviewSession(){

        try {

            SurfaceTexture surfaceTexture = nTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback(){

                        @Override
                        public void onConfigured(CameraCaptureSession session){
                            if(mCameraDevice == null){
                                return;
                            }
                            try{
                                mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(
                                        mPreviewCaptureRequest,
                                        mSessionCaptureCallback,
                                        mHandler
                                );

                            }catch (CameraAccessException ex){
                                ex.printStackTrace();
                            }


                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession var1){
                            Toast.makeText(getBaseContext(), "Camera configed wrong",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }, null);


        }catch(CameraAccessException ex){
            Toast.makeText(getBaseContext(), "Camera exception", Toast.LENGTH_SHORT).show();
        }
    }

    private Size getPreferedPreviewSize(Size[] mapSizes, int width, int hieght){

        List<Size> collectoreSizes = new ArrayList<>();

        for(Size option : mapSizes){
            if(width > hieght){
                if(option.getWidth() > width &&
                        option.getHeight() > hieght){
                    collectoreSizes.add(option);
                }
            }else{
                if(option.getWidth() > hieght &&
                        option.getHeight() > width){
                    collectoreSizes.add(option);
                }
            }
        }

        if(collectoreSizes.size() > 0){
            return Collections.min(collectoreSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getHeight() * lhs.getHeight() -
                            rhs.getHeight() * rhs.getWidth());
                }
            });
        }
        return mapSizes[0];

    }

    private void openBackgroundThread(){
        mHandelerThread = new HandlerThread("Camera2 backgroud thread");
        mHandelerThread.start();
        mHandler = new Handler(mHandelerThread.getLooper());
    }

    private void closeBackgroundThread(){
        mHandelerThread.quitSafely();
        try{
            mHandelerThread.join();
            mHandelerThread = null;
            mHandler = null;
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }
    }
}
