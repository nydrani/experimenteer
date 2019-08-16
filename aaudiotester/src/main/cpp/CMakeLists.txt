cmake_minimum_required(VERSION 3.4.1)
project(aaudiotestingapp)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

#message(STATUS "project ${PROJECT_BINARY_DIR}")
#message(STATUS "cmake ${CMAKE_BINARY_DIR}")

set(CMAKE_VERBOSE_MAKEFILE on)

add_library(testaaudio
        SHARED
        aaudiolib.cpp)

target_link_libraries(testaaudio
        log
        android
        aaudio)