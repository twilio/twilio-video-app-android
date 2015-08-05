NDK_TOOLCHAIN_VERSION := 4.9
#NDK_TOOLCHAIN_VERSION := clang
APP_PLATFORM := android-16
#APP_STL := stlport_static
APP_STL := c++_static
#APP_ABI := armeabi armeabi-v7a x86 mips
APP_ABI := armeabi-v7a
APP_OPTIM := release
#APP_CPPFLAGS := -fexceptions
APP_CPPFLAGS += -std=gnu++11 -fexceptions
