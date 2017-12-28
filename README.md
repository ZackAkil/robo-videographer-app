# Robo videographer app
Version 2 of rugby recording robot built into a mobile app with better machine learning.

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
- [ ] Calculate and feed delta image on android app
- [ ] Modualirse code on android app 
- [ ] Control arduino with output from tensorflow model
- [ ] Optimise TF model
- [ ] Find a way to use the optimised for inference model on android

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
https://github.com/tensorflow/tensorflow/blob/fca253c282eedbfa4e82071af389bdb75ac13a90/tensorflow/examples/android/src/org/tensorflow/demo/TensorFlowImageClassifier.java

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
