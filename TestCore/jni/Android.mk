LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := QuickStart
LOCAL_SRC_FILES := QuickStart.cpp

include $(BUILD_SHARED_LIBRARY)
