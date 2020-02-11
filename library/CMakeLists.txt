cmake_minimum_required(VERSION 3.4.1)

# Find bbe for JNI class reference renaming
find_program(BBE bbe)
if(NOT BBE)
    message(FATAL_ERROR "bbe must be installed.")
endif()

# Set compiler name and version
string(REGEX MATCH "^([0-9]+\\.[0-9]+)" COMPILER_VERSION ${CMAKE_CXX_COMPILER_VERSION})
string(TOLOWER ${CMAKE_CXX_COMPILER_ID}-${COMPILER_VERSION} COMPILER)

# Set build type
string(TOLOWER ${CMAKE_BUILD_TYPE} BUILD_TYPE)

# Set core paths
set(CORE_INCLUDE_DIR ${VIDEO_DEPS_PREFIX}/include)
set(CORE_STATIC_LIB_DIR ${VIDEO_DEPS_PREFIX}/lib/${ANDROID_ABI}/${COMPILER}/${BUILD_TYPE})

# Set WebRTC paths
set(WEBRTC_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party)
set(THIRD_PARTY_INCLUDE_DIR ${CORE_INCLUDE_DIR}/third_party/webrtc/third_party)

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

# Create the library with name provided from build.gradle
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

# Extract and rename all WebRTC JNI signature symbols to contain correct classpath prefix.
#
# This process must be done before the creation of the .so file in order to ensure
# that the application can link WebRTC Java native method definitions to the appropriate
# native counterparts at runtime.
add_custom_command(
        TARGET ${VIDEO_ANDROID_LIBRARY}
        PRE_BUILD
        COMMAND
        # Extract all JNI symbols
        ${CMAKE_NM} -g ${CORE_STATIC_LIB_DIR}/libwebrtc.a | grep 'Java_org_webrtc' |
        # Output symbol rename definitions to the following multi-line format
        # Java_org_webrtc_SymbolOne Java_tvi_webrtc_SymbolOne
        # Java_org_webrtc_SymbolTwo Java_tvi_webrtc_SymbolTwo
        cut -d \" \" -f3 |
        sed 's/\\\(Java_\\\)org\\\(_.*\\\)/\\1org\\2 \\1${VIDEO_ANDROID_CLASSPATH_PREFIX}\\2/' >
        ${CMAKE_BINARY_DIR}/webrtc-jni-symbols-definitions.txt &&
        # Rename the JNI symbols
        ${CMAKE_OBJCOPY}
        --redefine-syms=${CMAKE_BINARY_DIR}/webrtc-jni-symbols-definitions.txt
        ${CORE_STATIC_LIB_DIR}/libwebrtc.a
)

# Rename classpath reference symbols
add_custom_command(
        TARGET ${VIDEO_ANDROID_LIBRARY}
        POST_BUILD
        # There are two notable design decisions about this command:
        #
        # 1. Modifying the symbols in place corrupts the final .so file packaged into the aar. As
        # a result, the process is split into two steps: renaming the symbols to a temporary file
        # and moving the temporary file to the appropriate location.
        #
        # 2. Ninja fails when referencing target variables in multiple commands. The command is
        # intentionally combined using the && operator to workaround this issue.
        COMMAND
        # Rename the symbols and output to a temporary file
        bbe -e 's_org/webrtc_${VIDEO_ANDROID_CLASSPATH_PREFIX}/webrtc_'
        $<TARGET_FILE:${VIDEO_ANDROID_LIBRARY}>
        -o $<TARGET_FILE_DIR:${VIDEO_ANDROID_LIBRARY}>/${VIDEO_ANDROID_LIBRARY}-sbs.so &&
        # Move the temporary file into the proper location
        mv $<TARGET_FILE_DIR:${VIDEO_ANDROID_LIBRARY}>/${VIDEO_ANDROID_LIBRARY}-sbs.so
        $<TARGET_FILE:${VIDEO_ANDROID_LIBRARY}>
)
