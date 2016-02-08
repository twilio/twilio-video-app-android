NDK_TOOLCHAIN_VERSION := 4.9
APP_PLATFORM := android-16
APP_STL := c++_static
APP_ABI := armeabi-v7a
ifeq ($(shell test "$(APP_DEBUGGABLE)" = "true" -o "$(NDK_DEBUG)" = "1" && echo true || echo false),true)
APP_OPTIM := debug
else
APP_OPTIM := release
endif
APP_CPPFLAGS += -std=gnu++11 -fexceptions
