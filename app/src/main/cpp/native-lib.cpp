#include <algorithm>
#include <array>
#include <cstddef>
#include <string>
#include <vector>

#include <jni.h>
#include <omp.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/opencv.hpp>

#include "image-utils.h"

using namespace cv;

cv::Mat alignImage(const Mat &img1, const Mat &img2, jint numFeatures, jdouble retainPercent) {
    // Convert images to grayscale
    Mat img1_gray, img2_gray;
    cvtColor(img1, img1_gray, COLOR_RGBA2GRAY);
    cvtColor(img2, img2_gray, COLOR_RGBA2GRAY);

    // Apply ORB feature detection algorithm
    auto orb = ORB::create(numFeatures);
    std::vector<KeyPoint> kp1, kp2;
    Mat des1, des2;

    orb->detectAndCompute(img1_gray, noArray(), kp1, des1);
    orb->detectAndCompute(img2_gray, noArray(), kp2, des2);

    // Use Hamming distance matcher
    auto matcher = DescriptorMatcher::create(DescriptorMatcher::BRUTEFORCE_HAMMING);
    std::vector<DMatch> matches;
    matcher->match(des1, des2, matches);

    // Sort matches based on distance
    std::sort(matches.begin(), matches.end(), [](const DMatch &a, const DMatch &b) {
        return a.distance < b.distance;
    });

    // Retain top matches
    auto num_matches_keep = static_cast<int>(static_cast<double>(matches.size()) * retainPercent);
    matches.resize(num_matches_keep);

    // Extract location of good matches
    std::vector<Point2f> points1(num_matches_keep);
    std::vector<Point2f> points2(num_matches_keep);

    for (size_t i = 0; i < matches.size(); i++) {
        points1[i] = kp1[matches[i].queryIdx].pt;
        points2[i] = kp2[matches[i].trainIdx].pt;
    }

    // Compute homography matrix
    Mat h = findHomography(points2, points1, RANSAC);

    // Warp image with homography matrix
    Mat aligned_img;
    warpPerspective(img2, aligned_img, h, img1.size(), INTER_LINEAR, BORDER_REPLICATE);
    return aligned_img;
}

extern "C" JNIEXPORT void JNICALL
Java_com_aidansun_photostacker_ImageDisplayActivity_processImage(
        JNIEnv *env, jobject, jobjectArray bitmaps, jobject out, jboolean enableAlignment,
        jint numFeatures, jdouble retainPercent) {
    jsize length = env->GetArrayLength(bitmaps);
    std::vector<Mat> images;

    // Convert images from managed bitmaps
    for (int i = 0; i < length; i++) {
        auto bitmap = (jobject) env->GetObjectArrayElement(bitmaps, i);
        Mat mat;
        ImageUtils::bitmapToMat(env, bitmap, mat);

        // Align images using image #1 as a reference
        if (i > 0 && enableAlignment) mat = alignImage(images[0], mat, numFeatures, retainPercent);

        images.push_back(mat);
        env->DeleteLocalRef(bitmap);
    }

    int rows = images[0].rows;
    int cols = images[0].cols;
    auto numImages = static_cast<int>(images.size());

    Mat medianImage(rows, cols, CV_8UC3); // Output RGB image

    // Parallel processing using OpenMP
#pragma omp parallel for collapse(2)
    for (int y = 0; y < rows; y++) {
        for (int x = 0; x < cols; x++) {
            Vec3b medianPixel; // Store median R, G, B values

            // Process each color channel
            for (int c = 0; c < 3; c++) {
                std::vector<uchar> pixelValues(numImages);

                for (int i = 0; i < numImages; i++) {
                    pixelValues[i] = images[i].at<Vec3b>(y, x)[c]; // Extract channel value
                }

                // Compute median (efficient sorting)
                nth_element(pixelValues.begin(), pixelValues.begin() + numImages / 2,
                            pixelValues.end());
                medianPixel[c] = pixelValues[numImages / 2]; // Assign median value
            }

            medianImage.at<Vec3b>(y, x) = medianPixel;
        }
    }

    // Convert back to original type
    ImageUtils::matToBitmap(env, medianImage, out);
}
