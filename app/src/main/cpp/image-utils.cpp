//
// Created by Aidan on 2/19/2025.
//

#include "image-utils.h"

#include <android/bitmap.h>
#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

// Bitmap data used for conversions.
struct BitmapData {
    AndroidBitmapInfo info{};
    void* pixels = nullptr;
    int width = 0;
    int height = 0;
};

// Locks pixels in a bitmap and returns relevant data.
BitmapData lockPixels(JNIEnv* env, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels = nullptr;

    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);

    auto height = static_cast<int>(info.height);
    auto width = static_cast<int>(info.width);

    return { info, pixels, width, height };
}

void ImageUtils::bitmapToMat(JNIEnv* env, jobject bitmap, cv::Mat& dst) {
    auto [info, pixels, width, height] = lockPixels(env, bitmap);

    dst.create(height, width, CV_8UC3);
    cv::Mat tmp(height, width, CV_8UC4, pixels);
    cvtColor(tmp, dst, cv::COLOR_RGBA2RGB);
    AndroidBitmap_unlockPixels(env, bitmap);
}

void ImageUtils::matToBitmap(JNIEnv* env, const cv::Mat& src, jobject bitmap) {
    auto [info, pixels, width, height] = lockPixels(env, bitmap);

    cv::Mat tmp(height, width, CV_8UC4, pixels);
    cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
    AndroidBitmap_unlockPixels(env, bitmap);
}
