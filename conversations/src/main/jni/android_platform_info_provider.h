#ifndef TC_PLATFORM_DATA_PROVIDER_IMPL_H
#define TC_PLATFORM_DATA_PROVIDER_IMPL_H

#include <jni.h>
#include <string.h>
#include "TSCoreSDKTypes.h"
#include "TSCPlatformDataProvider.h"
#include "TSCPlatformInfoReport.h"

#include "webrtc/api/java/jni/jni_helpers.h"

using namespace webrtc_jni;
using namespace twiliosdk;

class AndroidPlatformInfoProvider : public TSCPlatformDataProvider {

public:
    AndroidPlatformInfoProvider(JNIEnv* jni, jobject context);

    virtual ~AndroidPlatformInfoProvider() {}

    virtual const TSCPlatformInfoReport getReport() const;

private:
    std::string callStringMethod(jmethodID methodId, bool useContext = false) const;
    unsigned int callUnsignedIntMethod(jmethodID methodId) const;
    double callDoubleMethod(jmethodID methodId) const;


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
    const jmethodID j_getHwDeviceIPAddress_id;

};

#endif
