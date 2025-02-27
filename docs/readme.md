# PhotoStacker

**PhotoStacker** is an Android image processing application that can capture a series of photos and combine them into a single output image. It uses a median stacking procedure to achieve use cases such as noise reduction and removal of moving objects, and photos can optionally be aligned before processing to account for camera movements.

## Features

- Efficient median stacking procedure
  - Reduce noise or grain effects compared to a single photo
  - Erase objects that move and obstruct a scene
- Configurable timer to automatically take photos at regular time intervals
- Auto-align/stabilize photos to correct shaky input
- Save both individual input photos and the output photo to the Gallery
- Full HD image support

## System Requirements

This app supports Android 10 (API level 29) and above. Many modern mobile devices have sufficient computing power to run the image processing operations this app uses.

## Build in Android Studio

1. Download and extract the [OpenCV Android SDK](https://github.com/opencv/opencv/releases) into a directory of your choice.
2. Create the `OPENCV_ANDROID` environment variable pointing to the SDK. (`OPENCV_ANDROID`/sdk/native/jni should be a valid directory.)
3. Open and build the project in Android Studio.

## Image Processing

PhotoStacker uses OpenCV in C++ to process photos and OpenMP for efficient multicore processing. For a Python and NumPy implementation of alignment and median stacking similar to the procedure used in this app, see [Moving Object Removal](https://github.com/AidanSun05/moving-object-removal).

## License

PhotoStacker is licensed under the Apache License version 2.0.

Copyright 2025 Aidan Sun.
