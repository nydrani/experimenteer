cmake_minimum_required(VERSION 3.4.1)
project(testapp)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

#message(STATUS "project ${PROJECT_BINARY_DIR}")
#message(STATUS "cmake ${CMAKE_BINARY_DIR}")

set(CMAKE_VERBOSE_MAKEFILE on)

add_library(testing
            SHARED
            testlib.cpp)

add_library(eaves
        SHARED
        eaveslib.cpp)

add_library(attestation
        SHARED
        attestationlib.cpp)

add_library(signal
        SHARED
        signallib.cpp)

add_library(antidebugging
        SHARED
        antidebugginglib.cpp)

#add_library(testaaudio
#        SHARED
#        aaudiolib.cpp)

target_link_libraries(testing
        log
        android)

target_link_libraries(eaves
        log
        android
        OpenSLES)

target_link_libraries(attestation
        log
        android)

target_link_libraries(signal
        log
        android)

target_link_libraries(antidebugging
        log
        android)

#target_link_libraries(testaaudio
#        log
#        android
#        aaudio)