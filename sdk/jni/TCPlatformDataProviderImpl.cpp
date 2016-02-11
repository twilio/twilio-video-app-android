#include "TCPlatformDataProviderImpl.h"

TCPlatformDataProviderImpl::TCPlatformDataProviderImpl(JNIEnv* jni, jobject context):
	j_context_global_(jni, context),
	j_platform_info_class_(jni,
		jni->FindClass("com/twilio/conversations/impl/core/PlatformInfo")),
	j_getPlatfomName_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getPlatfomName", "()Ljava/lang/String;")),
	j_getPlatformVersion_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getPlatformVersion", "()Ljava/lang/String;")),
	j_getHwDeviceManufacturer_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getHwDeviceManufacturer", "()Ljava/lang/String;")),
	j_getHwDeviceModel_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getHwDeviceModel", "()Ljava/lang/String;")),
	j_getHwDeviceUUID_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getHwDeviceUUID", "()Ljava/lang/String;")),
	j_getHwDeviceConnectionType_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getHwDeviceConnectionType", "(Landroid/content/Context;)Ljava/lang/String;")),
	j_getHwDeviceNumCores_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getHwDeviceNumCores", "()I")),
	j_getTimeStamp_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getTimeStamp", "()D")),
	j_getRtcPlatformSdkVersion_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getRtcPlatformSdkVersion", "()Ljava/lang/String;")),
	j_getOsArch_id(
		GetStaticMethodID(jni, *j_platform_info_class_, "getOsArch", "()Ljava/lang/String;"))
{}

const TSCPlatformInfoReport TCPlatformDataProviderImpl::getReport() const {
	JNIEnv* jni = AttachCurrentThreadIfNeeded();
	TSCPlatformInfoReport report;
	jstring rezObj = (jstring)jni->CallStaticObjectMethod(*j_platform_info_class_, j_getPlatfomName_id);
	if (IsNull(jni, rezObj)) {
		report.platformName = "";
	} else {
		report.platformName = JavaToStdString(jni, rezObj);
	}
	return report;
}
