package com.example.zackakil.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
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

    private TextView mYDisplay;
    private TextView mUDisplay;
    private TextView mVDisplay;


    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;

    private ProgressBar mProgressBar;
    private ImageView mImageView;
    private TextView mPredictionTextView;
    private TextView mSumTextView;

    private Bitmap syntheticImage = null;
    private Bitmap syntheticImage2 = null;
    private boolean useSyntheticImage = false;
    private int syntheticImageToUse = 0;

    private SeekBar mSeekBarWidth;
    private SeekBar mSeekBarHeight;
    private int mWidthCropVal;
    private int mHeightCropVal;

    private long lastEpoch = 0;
    private int delay = 500;

    private float[] oldFramePixels;

    private Switch mCropViewSwitch;
    private float prediction = 0;

    private BluetoothTripod mBluetoothTripod;
    private SignalFeedDampener mSignalDampener;

    private SignalScaler mSignalScaler;

    private Spinner fpsSelector;

    private EditText mEditTextSignalDamp;


    private TfPredictor predictor;
    private ImagePreProcessor imagePreProcessor;

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

                        if (rgbBytes == null) {
                            rgbBytes = new int[mPreviewSize.getWidth() * mPreviewSize.getHeight()];
                        }

                        final Image.Plane[] planes = image.getPlanes();
                        yRowStride = planes[0].getRowStride();
                        final int uvRowStride = planes[1].getRowStride();
                        final int uvPixelStride = planes[1].getPixelStride();
                        fillBytes(planes, yuvBytes);

                        ImageUtils.convertYUV420ToARGB8888(
                                yuvBytes[0],
                                yuvBytes[1],
                                yuvBytes[2],
                                mPreviewSize.getWidth(),
                                mPreviewSize.getHeight(),
                                yRowStride,
                                uvRowStride,
                                uvPixelStride,
                                rgbBytes);

                        if (rgbFrameBitmap != null) {
                            rgbFrameBitmap.setPixels(rgbBytes, 0, mPreviewSize.getWidth(), 0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
                            final int r = Color.red(rgbFrameBitmap.getPixel(0, 0));
                            final int g = Color.green(rgbFrameBitmap.getPixel(0, 0));
                            final int b = Color.blue(rgbFrameBitmap.getPixel(0, 0));





                            if (System.currentTimeMillis() > + (lastEpoch + delay) ){
                                lastEpoch = System.currentTimeMillis();
                                prediction = getPredictionFromTf();
                            }

                            Log.d("My App", "prediction ="+ prediction);

                            Log.d("My App", "height crop ="+ mHeightCropVal);
                            Log.d("My App", "width crop ="+ mWidthCropVal);

                            mBluetoothTripod.feedPrediction( mSignalScaler.scaleVal(prediction) );
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mYDisplay.setText(String.valueOf(r));
                                    mUDisplay.setText(String.valueOf(g));
                                    mVDisplay.setText(String.valueOf(b));

                                }
                            });
                        }else{
                            rgbFrameBitmap = Bitmap.createBitmap(mPreviewSize.getWidth(), mPreviewSize.getHeight(), Config.ARGB_8888);
                        }


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


    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
//                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePreProcessor = new ImagePreProcessor(50, 16);

        predictor = new TfPredictor(getString(R.string.cnnModelName),
                                    getAssets(),
                                    "input_input",
                                    "output/BiasAdd",
                                    new int[]{2, 16, 50, 1});

        setContentView(R.layout.activity_video);
        nTextureView = (TextureView) findViewById(R.id.textureView);

        mYDisplay = (TextView) findViewById(R.id.textViewY);
        mUDisplay = (TextView) findViewById(R.id.textViewU);
        mVDisplay = (TextView) findViewById(R.id.textViewV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mPredictionTextView = (TextView) findViewById(R.id.textViewPrediction);
        mSumTextView = (TextView) findViewById(R.id.textViewSum);

        mSeekBarWidth = (SeekBar) findViewById(R.id.seekBarWidth);
        mSeekBarWidth.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser) {

                       mWidthCropVal = progressValue;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


        mSeekBarHeight = (SeekBar) findViewById(R.id.seekBarHeight);
        mSeekBarHeight.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progressValue, boolean fromUser) {

                        mHeightCropVal = progressValue;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

        mCropViewSwitch = (Switch) findViewById(R.id.switchCropView);

        mBluetoothTripod = new BluetoothTripod("HC-06");

        mImageView.setKeepScreenOn(true);

        mSignalDampener = new SignalFeedDampener();

        mSignalScaler = new SignalScaler(VideoActivity.this);

        mEditTextSignalDamp = (EditText) findViewById(R.id.editTextSignalDampen);



        Spinner spinner = (Spinner) findViewById(R.id.spinnerFPS);
// Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fps_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                delay = Integer.parseInt((String) adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


    }

    @Override
    public void onPause(){
        closeCamera();

        closeBackgroundThread();

        mBluetoothTripod.close();

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


    private float[] getSyntheticPixels(){

        final Bitmap synthBitmap = syntheticImage;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(synthBitmap);
            }
        });


        final float[] synthFloatPixels_1 = bitmapToPixels(syntheticImage, 50, 16);

        final float[] synthFloatPixels_2 = bitmapToPixels(syntheticImage2, 50, 16);

        float[] c = new float[synthFloatPixels_1.length + synthFloatPixels_2.length];
        System.arraycopy(synthFloatPixels_1, 0, c, 0, synthFloatPixels_1.length);
        System.arraycopy(synthFloatPixels_2, 0, c, synthFloatPixels_1.length, synthFloatPixels_2.length);



        return c;
    }

    private float[] bitmapToPixels(Bitmap frame, int width, int height){

        int size = width * height;

        final int[] pixels = new int[size];

        final float[] synthFloatPixels = new float[size];

        frame.getPixels (pixels, 0, frame.getWidth(), 0, 0, width, height);

        for(int i = 0; i<size; i++){
            synthFloatPixels[i] =  ((float) Color.red(pixels[i])) / 255.f ;
        }

        return synthFloatPixels;

    }



    private float getPredictionFromTf(){

        float[] floatPixels;

        if (! useSyntheticImage) {
//            imagePreProcessor.feedFrame(rotateImage(rgbFrameBitmap, 90), mWidthCropVal, mHeightCropVal);
            imagePreProcessor.feedFrame(rgbFrameBitmap, mWidthCropVal, mHeightCropVal);

            floatPixels = imagePreProcessor.getLatestDeltaFrame();
        }else{
            floatPixels = getSyntheticPixels();


        }


        if (floatPixels != null) {

            if (mCropViewSwitch.isChecked()){

                final Bitmap bitmap = imagePreProcessor.getCroppedFrame();

                if(!useSyntheticImage)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(bitmap);
                        }
                    });

            }else{

                final Bitmap bitmap = imagePreProcessor.getLatestDeltaFrameAsBitmap();

                if(!useSyntheticImage)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(bitmap);
                        }
                    });

            }

            if (oldFramePixels != null) {

                float[] c;
                if (! useSyntheticImage) {
                    c = new float[oldFramePixels.length + floatPixels.length];
                    System.arraycopy(oldFramePixels, 0, c, 0, oldFramePixels.length);
                    System.arraycopy(floatPixels, 0, c, oldFramePixels.length, floatPixels.length);
                }else{
                    c = floatPixels;
                }


                float prediction = 1 - (float) mSignalDampener.getNextVal( predictor.predict(c), Float.parseFloat( mEditTextSignalDamp.getText().toString()));

                mProgressBar.setProgress(100 - (int) (prediction * 100));

                final String out = String.valueOf(prediction);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPredictionTextView.setText(out);
                    }
                });

                return prediction;

            }else{
                oldFramePixels = floatPixels;
                return 0;
            }

        }else{
            return 0;
        }
    }

    public void runSyntheticData(View view){
//        toggle sythn trigger
        useSyntheticImage = true;
//        loop image id
        syntheticImageToUse = (syntheticImageToUse + 1) % 5;

//        set image to next bitmap from assets
        try {
            InputStream open = getAssets().open(syntheticImageToUse+"_0.png");
            syntheticImage = BitmapFactory.decodeStream(open);

            open = getAssets().open(syntheticImageToUse+"_1.png");
            syntheticImage2 = BitmapFactory.decodeStream(open);
        }catch(IOException e){
            Toast.makeText(getBaseContext(), "Loading image error", Toast.LENGTH_SHORT).show();
            Log.d("My App", "NO IMAGE "+syntheticImageToUse);
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

}
