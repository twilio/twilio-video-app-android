cmake_minimum_required(VERSION 3.4.1)

# Set the library name
set(VIDEO_ANDROID_LIBRARY jingle_peerconnection_so)

# Set compiler name and version
string(REGEX MATCH "^([0-9]+\\.[0-9]+)" COMPILER_VERSION ${CMAKE_CXX_COMPILER_VERSION})
string(TOLOWER ${CMAKE_CXX_COMPILER_ID}-${COMPILER_VERSION} COMPILER)

# Set build type
string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE)

# Set core paths
set(CORE_HOME_DIR ${VIDEO_DEPS_PREFIX}/${ANDROID_ABI}/${COMPILER}/${BUILD_TYPE})
set(CORE_INCLUDE_DIR ${CORE_HOME_DIR}/include)
set(CORE_STATIC_LIB_DIR ${CORE_HOME_DIR}/lib)

# Set WebRTC paths
set(WEBRTC_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party/webrtc/include)
set(THIRD_PARTY_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party/webrtc/include/webrtc/third_party)

# Include headers
include_directories(${CORE_INCLUDE_DIR})
include_directories(${WEBRTC_INCLUDE_DIR})
include_directories(${WEBRTC_INCLUDE_DIR}/webrtc)
include_directories(${THIRD_PARTY_INCLUDE_DIR}/libyuv/include)

# Set source files
file(GLOB_RECURSE SOURCE_FILES
    "src/main/jni/*.h"
    "src/main/jni/*.cpp"
)

# Video C++ SDK uses std::dynamic_pointer_cast to determine the concrete implementations
# of twilio::media::AudioCodec and twilio::media::VideoCodec passed in ConnectOptions.
# std::dynamic_pointer_cast requires -frtti to properly cast the pointer to its rightful subclass.
set_source_files_properties(
    src/main/jni/com_twilio_video_ConnectOptions.cpp PROPERTIES COMPILE_FLAGS "-frtti")

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

# These libraries are purposefully ordered to satisfy the linker. If the linker complains about
# undefined symbols that are clearly defined in the static libraries then reorder these
# libraries accordingly.
target_link_libraries(${VIDEO_ANDROID_LIBRARY} ${CORE_STATIC_LIB_DIR}/libtwilio-video.a
        ${CORE_STATIC_LIB_DIR}/libPocoNet.a
        ${CORE_STATIC_LIB_DIR}/libPocoNetSSL.a
        ${CORE_STATIC_LIB_DIR}/libPocoUtil.a
        ${CORE_STATIC_LIB_DIR}/libPocoCrypto.a
        ${CORE_STATIC_LIB_DIR}/libPocoFoundation.a
        ${CORE_STATIC_LIB_DIR}/libboost_system.a
        ${CORE_STATIC_LIB_DIR}/libboost_coroutine.a
        ${CORE_STATIC_LIB_DIR}/libboost_context.a
        ${CORE_STATIC_LIB_DIR}/libboost_thread.a
        # Several WebRTC classes use native JNI code that are only
        # called from Java classes. The compiler will incorrectly strip some
        # of these native objects during linking because it believes
        # they are unused by the native library. Tell the linker to keep
        # the whole archive to prevent this stripping and then specify in the
        # dependency after we are not using whole archive anymore because the linker
        # applies this option to every library after the option is specified.
        "-Wl,--whole-archive ${CORE_STATIC_LIB_DIR}/libwebrtc.a -Wl,--no-whole-archive"
        ${CORE_STATIC_LIB_DIR}/libjsoncpp.a)
