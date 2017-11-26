package com.example.zackakil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap.Config;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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

    private Bitmap rgbFrameBitmap = null;

    private ImageReader mImageReader;

    private ImageReader.OnImageAvailableListener mImageReaderCallback =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {

                    Image image = null;
                    try {
                        image = imageReader.acquireLatestImage();
                        if(image == null){
                            return;
                        }
                        if (image.getPlanes()[0].getBuffer() == null) {
                            return;
                        }
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] pixels = buffer.array();
//                        Log.d("My App", "sum ="+ buffer.toString());

                        Log.d("My App", "new counts");
                        Log.d("My App", "sum ="+ getAverageFromBuffer(image.getPlanes()[0].getBuffer()));
                        Log.d("My App", "sum ="+ getAverageFromBuffer(image.getPlanes()[1].getBuffer()));
                        Log.d("My App", "sum ="+ getAverageFromBuffer(image.getPlanes()[2].getBuffer()));


                        // use byte buffer for processing
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                        // make sure to close image
                    }

//                    Log.d("My App", "shit the bed! iv'e got a new image");

                }
            };

    private int getAverageFromBuffer(ByteBuffer buffer){
            int sum  = 0;
            int count = buffer.remaining();
            Log.d("My App", "size: "+ count);
            while (buffer.hasRemaining()){
                sum +=  buffer.get();
            }

            int avg = sum/count;
        return avg;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        nTextureView = (TextureView) findViewById(R.id.textureView);

        TextureView colorPick = (TextureView) findViewById(R.id.textureViewColor);
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


                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
                mImageReader.setOnImageAvailableListener(mImageReaderCallback, mHandler);

                return;
            }


            rgbFrameBitmap = Bitmap.createBitmap(mPreviewSize.getWidth(), mPreviewSize.getHeight(), Config.ARGB_8888);

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
            mPreviewCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
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

                        @Override
                        public void onActive(CameraCaptureSession var1){
                            Toast.makeText(getBaseContext(), "Camera started processing",
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
                if(option.getWidth() < width &&
                        option.getHeight() < hieght){
                    collectoreSizes.add(option);
                }
            }else{
                if(option.getWidth() < hieght &&
                        option.getHeight() < width){
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


    //  Byte decoder : ---------------------------------------------------------------------
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        // Pulled directly from:
        // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}
