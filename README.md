# Robo videographer app
Version 2 of rugby recording robot built into a mobile app with better machine learning.

## Plan

- [x] Connect bluetooth android to arduino
- [x] Control arduino servo with android
- [x] Access camera
- [x] Display camera preview
- [ ] Access individual frame of camera
- [ ] Access some pixel value from individual frame from camera
- [ ] Run simple TensorFlow model
- [ ] Pass image to simple TensorFlow model
- [ ] Control arduino with output from tensorflow model

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
