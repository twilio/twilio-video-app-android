cmake_minimum_required(VERSION 3.4.1)

# Set the library name
set(VIDEO_ANDROID_LIBRARY jingle_peerconnection_so)

# Set the target arch
string(REPLACE "-" "" TARGET_ARCH ${ANDROID_ABI})

# Set compiler name and version
string(REGEX MATCH "^([0-9]+\\.[0-9]+)" COMPILER_VERSION ${CMAKE_CXX_COMPILER_VERSION})
string(TOLOWER ${CMAKE_CXX_COMPILER_ID}-${COMPILER_VERSION} COMPILER)

# Set build type
string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE)

# Set core paths
set(CORE_HOME_DIR ${VIDEO_DEPS_PREFIX}/${TARGET_ARCH}/${COMPILER}/${BUILD_TYPE})
set(CORE_INCLUDE_DIR ${CORE_HOME_DIR}/include)
set(CORE_STATIC_LIB_DIR ${CORE_HOME_DIR}/lib)

# Set WebRTC paths
set(WEBRTC_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party/webrtc)
set(LIBYUV_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party/webrtc/third_party/libyuv/include)

# Include headers
include_directories(${CORE_INCLUDE_DIR})
include_directories(${WEBRTC_INCLUDE_DIR})
include_directories(${LIBYUV_INCLUDE_DIR})

# Set static library properties
add_library(webrtc STATIC IMPORTED GLOBAL)
set_property(TARGET webrtc PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libwebrtc.a)

add_library(webrtc-jni STATIC IMPORTED GLOBAL)
set_property(TARGET webrtc-jni PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libwebrtc-jni.a)

add_library(jni-helpers STATIC IMPORTED GLOBAL)
set_property(TARGET jni-helpers PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libjni-helpers.a)

include_directories(${RESIPROCATE_INCLUDE_DIR})
add_library(resiprocate STATIC IMPORTED GLOBAL)
set_property(TARGET resiprocate PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libresiprocate.a)

include_directories(${POCO_INCLUDE_DIR})
add_library(poco-net STATIC IMPORTED GLOBAL)
set_property(TARGET poco-net PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoNet.a)

add_library(poco-netssl STATIC IMPORTED GLOBAL)
set_property(TARGET poco-netssl PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoNetSSL.a)

add_library(poco-util STATIC IMPORTED GLOBAL)
set_property(TARGET poco-util PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoUtil.a)

add_library(poco-crypto STATIC IMPORTED GLOBAL)
set_property(TARGET poco-crypto PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoCrypto.a)

add_library(poco-json STATIC IMPORTED GLOBAL)
set_property(TARGET poco-json PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoJSON.a)

add_library(poco-foundation STATIC IMPORTED GLOBAL)
set_property(TARGET poco-foundation PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libPocoFoundation.a)

add_library(boringssl STATIC IMPORTED GLOBAL)
set_property(TARGET boringssl PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libboringssl.a)

include_directories(${CORE_INCLUDE_DIR})
add_library(video-core STATIC IMPORTED GLOBAL)
set_property(TARGET video-core PROPERTY IMPORTED_LOCATION ${CORE_STATIC_LIB_DIR}/libtwilio-video.a)

# Set source files
file(GLOB_RECURSE SOURCE_FILES
    "src/main/jni/*.h"
    "src/main/jni/*.cpp"
)

# Create the library
add_library(${VIDEO_ANDROID_LIBRARY} SHARED ${SOURCE_FILES})

# Link framework libraries
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-llog")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lz")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lm")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-ldl")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lGLESv2")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-ljnigraphics")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lOpenSLES")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lEGL")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-lGLESv1_CM")
target_link_libraries(${VIDEO_ANDROID_LIBRARY} "-landroid")

# Link static library dependencies
set(WEBRTC_JNI_LIB "-Wl,--whole-archive ${CORE_STATIC_LIB_DIR}/libwebrtc-jni.a -Wl,--no-whole-archive")
set(VIDEO_ANDROID_STATIC_LIB_DEPS ${CORE_STATIC_LIB_DIR}/libtwilio-video.a
        ${CORE_STATIC_LIB_DIR}/libwebrtc.a
        ${WEBRTC_JNI_LIB}
        ${CORE_STATIC_LIB_DIR}/libjni-helpers.a
        ${CORE_STATIC_LIB_DIR}/libresiprocate.a
        ${CORE_STATIC_LIB_DIR}/libPocoNet.a
        ${CORE_STATIC_LIB_DIR}/libPocoNetSSL.a
        ${CORE_STATIC_LIB_DIR}/libPocoUtil.a
        ${CORE_STATIC_LIB_DIR}/libPocoCrypto.a
        ${CORE_STATIC_LIB_DIR}/libPocoJSON.a
        ${CORE_STATIC_LIB_DIR}/libPocoFoundation.a
        ${CORE_STATIC_LIB_DIR}/libboringssl.a)
target_link_libraries(${VIDEO_ANDROID_LIBRARY} ${VIDEO_ANDROID_STATIC_LIB_DEPS})
