cmake_minimum_required(VERSION 3.4.1)
project(testingapp)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

#message(STATUS "project ${PROJECT_BINARY_DIR}")
#message(STATUS "cmake ${CMAKE_BINARY_DIR}")

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -u ANativeActivity_onCreate")

add_library(native_app_glue STATIC
        ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c)

target_include_directories(native_app_glue PUBLIC
        ${ANDROID_NDK}/sources/android/native_app_glue)

add_library(testvulkan
            SHARED
            vulkanlib.cpp)

add_library(vulkancore
        SHARED
        vulkancore.cpp)

target_link_libraries(testvulkan
        log
        android
        vulkan)

target_link_libraries(vulkancore
        log
        android
        native_app_glue
        vulkan)
