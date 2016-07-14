#include "android_platform_info_provider.h"

AndroidPlatformInfoProvider::AndroidPlatformInfoProvider(JNIEnv* jni, jobject context):
        j_context_global_(jni, context),
        j_platform_info_class_(jni,
                               jni->FindClass("com/twilio/rooms/PlatformInfo")),
        j_getPlatfomName_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getPlatfomName",
                                  "()Ljava/lang/String;")),
        j_getPlatformVersion_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getPlatformVersion",
                                  "()Ljava/lang/String;")),
        j_getHwDeviceManufacturer_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceManufacturer",
                                  "()Ljava/lang/String;")),
        j_getHwDeviceModel_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceModel",
                                  "()Ljava/lang/String;")),
        j_getHwDeviceUUID_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceUUID",
                                  "(Landroid/content/Context;)Ljava/lang/String;")),
        j_getHwDeviceConnectionType_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceConnectionType",
                                  "(Landroid/content/Context;)Ljava/lang/String;")),
        j_getHwDeviceNumCores_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceNumCores", "()I")),
        j_getTimeStamp_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getTimeStamp",
                                  "()D")),
        j_getRtcPlatformSdkVersion_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getRtcPlatformSdkVersion",
                                  "()Ljava/lang/String;")),
        j_getOsArch_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getOsArch",
                                  "()Ljava/lang/String;")),
        j_getHwDeviceIPAddress_id(
                GetStaticMethodID(jni,
                                  *j_platform_info_class_,
                                  "getHwDeviceIPAddress",
                                  "()Ljava/lang/String;"))
{}

const TSCPlatformInfoReport AndroidPlatformInfoProvider::getReport() const {
    TSCPlatformInfoReport report;
    report.platformName = this->callStringMethod(j_getPlatfomName_id);
    report.platformVersion = this->callStringMethod(j_getPlatformVersion_id);
    report.hwDeviceManufacturer = this->callStringMethod(j_getHwDeviceManufacturer_id);
    report.hwDeviceModel = this->callStringMethod(j_getHwDeviceModel_id);
    report.hwDeviceUUID = this->callStringMethod(j_getHwDeviceUUID_id, true);
    report.hwDeviceConnectionType = this->callStringMethod(j_getHwDeviceConnectionType_id, true);
    report.rtcPlatformSdkVersion = this->callStringMethod(j_getRtcPlatformSdkVersion_id);
    report.hwDeviceIPAddress = this->callStringMethod(j_getHwDeviceIPAddress_id);
    report.hwDeviceArch = this->callStringMethod(j_getOsArch_id);

    report.hwDeviceNumCores = this->callUnsignedIntMethod(j_getHwDeviceNumCores_id);
    report.timestamp = this->callDoubleMethod(j_getTimeStamp_id);

    return report;
}

std::string AndroidPlatformInfoProvider::callStringMethod(jmethodID methodId,
                                                          bool useContext) const {
    JNIEnv* jni = AttachCurrentThreadIfNeeded();
    jstring rezObj;
    if (useContext) {
        rezObj = (jstring)jni->CallStaticObjectMethod(*j_platform_info_class_,
                                                      methodId,
                                                      *j_context_global_);
    } else {
        rezObj = (jstring)jni->CallStaticObjectMethod(*j_platform_info_class_, methodId);
    }
    std::string result = "";
    if (IsNull(jni, rezObj)) {
        result = "";
    } else {
        result = JavaToStdString(jni, rezObj);
    }
    return result;
}

// Let's hope we have less then 2^31 processors :-)
unsigned int AndroidPlatformInfoProvider::callUnsignedIntMethod(jmethodID methodId) const {
    JNIEnv* jni = AttachCurrentThreadIfNeeded();
    jint result = (jint)jni->CallStaticIntMethod(*j_platform_info_class_, methodId);
    return (unsigned int)result;
}

double AndroidPlatformInfoProvider::callDoubleMethod(jmethodID methodId) const {
    JNIEnv* jni = AttachCurrentThreadIfNeeded();
    jdouble result = (jdouble)jni->CallStaticDoubleMethod(*j_platform_info_class_, methodId);
    return (double)result;
}

