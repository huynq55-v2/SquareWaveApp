cmake_minimum_required(VERSION 3.18)
project("squarewave")

add_library(
        native-lib
        SHARED
        native-lib.cpp
)

find_library(log-lib log)

# Enable AAudio library (Android 8.0+)
add_library(
        aaudio
        SHARED
        IMPORTED
)
set_target_properties(
        aaudio PROPERTIES
        IMPORTED_LOCATION
        ${ANDROID_NDK}/platforms/android-26/arch-${ANDROID_ARCH}/usr/lib/libaaudio.so
)

target_link_libraries(
        native-lib
        ${log-lib}
        -laaudio
)