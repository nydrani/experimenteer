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

target_link_libraries(testing
        log
        android
        OpenSLES)

target_link_libraries(eaves
        log
        android
        OpenSLES)
