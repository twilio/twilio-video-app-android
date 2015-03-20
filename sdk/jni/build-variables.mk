
#OPENSSL_LIBS := \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libssl.a \
	$(TWSDK_JNI_PATH)/../thirdparty/openssl-stock-android/lib/$(TARGET_ARCH_ABI)/libcrypto.a
	
WEBRTC_LIB_DIR := $(TWSDK_JNI_PATH)/../thirdparty/webrtc/build-android/prebuild/libs/$(TARGET_ARCH_ABI)
	
#WEBRTC_LIBS := \
	-lwebrtc-full
		
	
#WEBRTC_LDLIBS := -L$(WEBRTC_LIB_DIR) $(WEBRTC_LIBS)
