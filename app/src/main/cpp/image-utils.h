//
// Created by Aidan on 2/19/2025.
//

#ifndef PHOTOSTACKER_IMAGE_UTILS_H
#define PHOTOSTACKER_IMAGE_UTILS_H

#include <jni.h>
#include <opencv2/core.hpp>

namespace ImageUtils {
    // Converts a bitmap to a Mat (passing images from managed to native code).
    void bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &dst);

    // Converts a Mat to a bitmap (passing images from native to managed code).
    void matToBitmap(JNIEnv *env, const cv::Mat &src, jobject bitmap);
}

#endif // PHOTOSTACKER_IMAGE_UTILS_H
