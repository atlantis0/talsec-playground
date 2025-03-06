# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -marm")
endif ()

set(STATIC_LIB_DIR ${CMAKE_SOURCE_DIR}/libs)

# add yara lib
add_library(lib_yara STATIC IMPORTED)
set_target_properties(lib_yara PROPERTIES IMPORTED_LOCATION
        ${STATIC_LIB_DIR}/${ANDROID_ABI}/libyara.a)


add_library( # Sets the name of the library.
        scan
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        signature.cpp)

# include headers
target_include_directories(scan PRIVATE
        ${CMAKE_SOURCE_DIR}/include/)


find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        scan
        lib_yara
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})