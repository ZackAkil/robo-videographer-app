# Robo videographer app
Version 2 of rugby recording robot built into a mobile app with better machine learning.

![logo](/images/logo_v1.png)

## Plan

- [x] Connect bluetooth android to arduino
- [x] Control arduino servo with android
- [x] Access camera
- [x] Display camera preview
- [x] Access individual frame of camera
- [x] Access some pixel value from individual frame from camera
- [x] Get frame in RGB
- [x] Run simple TensorFlow model
- [x] Export keras as tf graph for android
- [x] Embed simple Tf model in android app
- [x] Calculate and feed delta image on android app
- [x] Create some way of validating the data being fed into tf model (visualisation or input hist)
- [x] Add test frames into android and python to validate data being fed in is correct shapes
- [ ] Modualirse code on android app 
- [ ] Save data (recordings)
- [x] *Control arduino with output from tensorflow model
- [x] *Optimise TF model
- [x] *Find a way to use the optimised for inference model on android
- [x] *Adjustable crop
- [x] FPS limiter
- [x] FPS tracker
- [ ] Refactor into threads
- [x] *Add recurent layer
- [x] *Prevent from sleeping

---

- [x] *Add smoothing into arduino
- [x] ~Add bluetooth connection fixer~ figure out why bluetooth drops (was limited power)
- [x] *Add frame rate adjuster
- [ ] refactore frame size to be easier to configure
- [x] Add camera pivot range setter
- [x] Add configure option for signal dampaning

*MVP





## Notes
Use native android (work thorugh this https://developer.android.com/training/index.html)
- View/save video
- Compute on video
- Run tensor flow model
- Comunicate with bluetooth https://developer.android.com/guide/topics/connectivity/bluetooth.html#TheBasics
- Store data
- Communicate with web (private APIs)
- Browse stored files

Good resource for final cloud based data processing - https://www.coursera.org/specializations/gcp-data-machine-learning

Actviity lifecycle - https://developer.android.com/guide/components/activities/activity-lifecycle.html

bluetooth example - https://github.com/bauerjj/Android-Simple-Bluetooth-Example/blob/master/app/src/main/java/com/mcuhq/simplebluetooth/MainActivity.java

using camera on android - https://www.youtube.com/playlist?list=PL9jCwTXYWjDJUJATHM0Lrjk9N5n6QZqBg

using tf with android camera - https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/android/src/org/tensorflow/demo/DetectorActivity.java

anroid camera ref - https://developer.android.com/reference/android/hardware/camera2/package-summary.html

get rgb from camera - https://stackoverflow.com/questions/15918180/get-rgb-from-a-surfaceview-displaying-live-camera

seems the key to getting images is using image reader - https://developer.android.com/reference/android/media/ImageReader.html

---
these held the key to using image reader - 
- https://stackoverflow.com/questions/28440599/camera-previewcallback-equivalent-in-camera2-api

- https://stackoverflow.com/questions/32516539/android-camera2-bad-argument-passed-to-camera-service
---
how to use the image utils 
- https://github.com/tensorflow/tensorflow/blob/15b1cf025da5c6ac2bcf4d4878ee222fca3aec4a/tensorflow/examples/android/src/org/tensorflow/demo/CameraActivity.java

---
how to use tensor flow on android 

- https://www.youtube.com/watch?v=kFWKdLOxykE
- https://github.com/llSourcell/A_Guide_to_Running_Tensorflow_Models_on_Android

- Android tensorflow utility - https://github.com/tensorflow/tensorflow/blob/fc49f43817e363e50df3ff2fd7a4870ace13ea13/tensorflow/contrib/android/java/org/tensorflow/contrib/android/TensorFlowInferenceInterface.java
---
LSTM CNN
- https://github.com/keras-team/keras/blob/master/examples/conv_lstm.py

Googles usage -
- https://github.com/tensorflow/tensorflow/blob/fca253c282eedbfa4e82071af389bdb75ac13a90/tensorflow/examples/android/src/org/tensorflow/demo/TensorFlowImageClassifier.java

specifically intrested in how they feed the bitmap image into the model:
```java
  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    for (int i = 0; i < intValues.length; ++i) {
      final int val = intValues[i];
      floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
      floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
      floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
    }
    Trace.endSection();

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
    Trace.endSection();
```

---
## Nan in Keras loss problem
The cause of this was that I had nan's / nulls in my y data. Removing them resolved this issue.

---
## [node_name] not in graph when saving keras model as tf graph
Fixed this issue by programatically getting the tensor names with the following code:
```
[x.op.name for x in model.outputs]
```
from _chris-smith-zocdoc_ at https://github.com/keras-team/keras/issues/6552

Updated the saving code to fetch these names automatically.

---
## Adding tensorflow dependancy to android project
Using the info at - https://www.tensorflow.org/mobile/android_build
Added dependancy by adding the following code to the `build.gradle (Module: app)` file :
```
dependencies {
    compile 'org.tensorflow:tensorflow-android:+'
}
```
---
## java.io.IOException: Not a valid TensorFlow Graph serialization: NodeDef expected inputs ''
Just use the `frozen*.pb` saved model and not the `opt*.pb` one.

---
## Tensorflow model on android not predicting because of dropout layer in wrong state
Solution: remove dropout layer from model [not a true fix]

---
## Image capture from camer is rotated 90 problem
After creating a visualisation for the processed data it was apparent that the image data being fed to the CNN was in the wrong rotation. Apparently this is due to the default shooting orientation of android cameras, still curious why the preview render is the correct orientation???
Fixed by rotating the bitmaps programatically
- https://stackoverflow.com/a/14066265
```java
public static Bitmap rotateImage(Bitmap source, float angle) {
    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                               matrix, true);
}
```

---
Android arbitrary storage: 
- https://developer.android.com/training/data-storage/index.html
Room
- https://developer.android.com/topic/libraries/architecture/room.html
- https://developer.android.com/training/data-storage/room/index.html

---
git deployment stuff for the platform
- https://blog.pythonanywhere.com/87/

---
list of recordings
- https://developer.android.com/guide/topics/ui/layout/listview.html

create simple list item views
- https://www.youtube.com/watch?v=A-_hKWMA7mk

create custom list item views
- https://www.youtube.com/watch?v=_sStCBdJkQg
- https://www.youtube.com/watch?v=nOdSARCVYic

---
Read/write files
- https://www.youtube.com/watch?v=EcfUkjlL9RI

---
Crop bitmap image
- https://stackoverflow.com/questions/3846338/how-to-crop-an-image-in-android

---
Prevent from sleeping 
- https://developer.android.com/reference/android/view/View.html#setKeepScreenOn%28boolean%29

---
Keras stateful lstm
- https://github.com/keras-team/keras/blob/master/examples/lstm_stateful.py
- https://github.com/keras-team/keras/issues/4208
- https://github.com/keras-team/keras/issues/2045

---
Arduino smoothing servo
- https://learn.adafruit.com/multi-tasking-the-arduino-part-1/overview
- https://learn.adafruit.com/multi-tasking-the-arduino-part-1/all-together-now

---
Android spinner (drop down) for FPS selector
- https://developer.android.com/guide/topics/ui/controls/spinner.html

---
Optimising model for inference 
```bash
python keras_to_tensorflow.py -input_model_file rcnn.h5 -output_model_file model.pb
```
