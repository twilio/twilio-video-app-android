#ifndef TC_PLATFORM_DATA_PROVIDER_IMPL_H
#define TC_PLATFORM_DATA_PROVIDER_IMPL_H

#include <jni.h>
#include <string.h>
#include "TSCoreSDKTypes.h"
#include "TSCPlatformDataProvider.h"
#include "TSCPlatformInfoReport.h"

#include "talk/app/webrtc/java/jni/jni_helpers.h"

using namespace webrtc_jni;
using namespace twiliosdk;

class TCPlatformDataProviderImpl : public TSCPlatformDataProvider {

public:
	TCPlatformDataProviderImpl(JNIEnv* jni, jobject context);

	virtual ~TCPlatformDataProviderImpl() {}

    virtual const TSCPlatformInfoReport getReport() const;

private:
    const ScopedGlobalRef<jobject> j_context_global_;
    const ScopedGlobalRef<jclass> j_platform_info_class_;
    const jmethodID j_getPlatfomName_id;
    const jmethodID j_getPlatformVersion_id;
    const jmethodID j_getHwDeviceManufacturer_id;
    const jmethodID j_getHwDeviceModel_id;
    const jmethodID j_getHwDeviceUUID_id;
    const jmethodID j_getHwDeviceConnectionType_id;
    const jmethodID j_getHwDeviceNumCores_id;
    const jmethodID j_getTimeStamp_id;
    const jmethodID j_getRtcPlatformSdkVersion_id;
    const jmethodID j_getOsArch_id;


};

#endif
