cmake_minimum_required(VERSION 3.22.1)

project("photostacker")

set(OpenCV_STATIC ON)
set(OpenCV_DIR $ENV{OPENCV_ANDROID}/sdk/native/jni)
find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})

add_library(${CMAKE_PROJECT_NAME} SHARED
        image-utils.cpp
        native-lib.cpp)

find_library(jnigraphics-lib jnigraphics)
target_link_libraries(${CMAKE_PROJECT_NAME}
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        android
        log)
