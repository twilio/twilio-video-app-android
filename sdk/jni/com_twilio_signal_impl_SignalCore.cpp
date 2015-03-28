#include "TSCoreSDK.h"
#include <twilio-jni/twilio-jni.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"

#include "com_twilio_signal_impl_SignalCore.h"

#define ANDROID 1

using namespace twiliosdk;

TSCSDK* tscSdk = NULL;

class EndpointObserver: public TSCEndpointObserverObject
{
public:
	EndpointObserver()
    {};

    virtual ~EndpointObserver()
    {};

protected:

    void onRegistrationDidComplete(TSCErrorObject* error)
    {
		__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onRegistrationDidComplete", 1);
    }

    void onUnregistrationDidComplete(TSCErrorObject* error)
    {
    	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onUnregistrationDidComplete", 1);
    }

    void onStateChange(TSCEndpointState state) {

    }
    void onReceiveIncomingCall(const TSCIncomingSessionObjectRef& session) {

    }

};
	JNIEXPORT jstring JNICALL
    Java_com_twilio_signal_impl_SignalCore_initCore(JNIEnv *env, jobject obj)
    {
		tscSdk = TSCSDK::instance();
		return env->NewStringUTF("Hello from C++ over JNI!");
    }

	JNIEXPORT jboolean JNICALL
	Java_com_twilio_signal_impl_SignalCore_isCoreInitialized(JNIEnv *env, jobject obj)
	{
		if (tscSdk != NULL && tscSdk->isInitialized())
		{
			return JNI_TRUE;
		}
		return JNI_FALSE;
	}


	JNIEXPORT jboolean JNICALL
		Java_com_twilio_signal_impl_SignalCore_login(JNIEnv *env, jobject obj)
		{

			if(tscSdk != NULL) {
				TSCOptions coreOptions;

				__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "tscSdk  initialized", 1);

				coreOptions.insert(std::make_pair("account-sid","AC96ccc904753b3364f24211e8d9746a93"));
				coreOptions.insert(std::make_pair("alias-name","kumkum"));
				coreOptions.insert(std::make_pair("capability-token", "eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.eyJpc3MiOiAiQUM5NmNjYzkwNDc1M2IzMzY0ZjI0MjExZThkOTc0NmE5MyIsICJzY29wZSI6ICJzY29wZTpjbGllbnQ6b3V0Z29pbmc_YXBwU2lkPUFQZTc1ODg5Yzk3YjNhZjBmMzM4NjlhOGM1N2Q4ZDg0MjUmY2xpZW50TmFtZT1rdW1rdW0gc2NvcGU6Y2xpZW50OmluY29taW5nP2NsaWVudE5hbWU9a3Vta3VtIHNjb3BlOnN0cmVhbTpzdWJzY3JpYmU_cGF0aD0lMkYyMDEwLTA0LTAxJTJGRXZlbnRzIiwgImV4cCI6IDE0Mjc1MTM0MDd9.gMgK3cvtmyNV8S849KHPPo5uZTpA7qLtLRuLkX6F2nM"));
				coreOptions.insert(std::make_pair("domain", "twil.io"));
				coreOptions.insert(std::make_pair("password", "LoJEG0gd29yx0hv9xmWSMOsndyV8Pfe0xY8g3fqKRgU"));
				coreOptions.insert(std::make_pair("registrar",  "public-sip0.us1.twilio.com"));
				coreOptions.insert(std::make_pair("sip-client-version",  "2"));
				coreOptions.insert(std::make_pair("sip-transport-port", "5061"));
				coreOptions.insert(std::make_pair("sip-transport-type", "tls"));
				coreOptions.insert(std::make_pair("sip-user-agent", "Twilio"));
				coreOptions.insert(std::make_pair("stun-url", "stun:global.stun.twilio.com:3478?transport=udp"));
				coreOptions.insert(std::make_pair("turn-url", "turn:global.turn.twilio.com:3478?transport=udp"));
				coreOptions.insert(std::make_pair("user-name", "37683a06a8618336e06ec44e7b88076632cc73dffbd16084da0b69d5652c7511"));


				TSCEndpointObjectRef endpoint = tscSdk->createEndpoint(coreOptions, TSCEndpointObserverObjectRef(new EndpointObserver()));

				if(endpoint == NULL) {
					__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "createEndPoint  failed", 1);
				}
				__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Log createEndPoint  succeeded", 1);

				endpoint->registerEndpoint();

				__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Log called Endpoint.register", 1);
				} else {
				__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Endpoint NOT REGISTERED", 1);
			}


			return JNI_FALSE;
		}
