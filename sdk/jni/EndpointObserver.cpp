#include <android/log.h>
#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"

#include "com_twilio_signal_impl_SignalCore.h"


using namespace twiliosdk;


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
		__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "Received notification: onRegistrationDidComplete", 1);

		/*jobject callbacks = tw_jni_fetch_object(env, mainUserAgent, "callbacks",
							"Lcom/twilio/client/impl/useragent/config/UserAgentConfig$Callbacks;");

		if (!callbacks) {
			return;
		}

		jmethodID meth = tw_jni_get_method(env, callbacks, "onRegistrationState",
						"(Lcom/twilio/client/impl/session/Account;Lcom/twilio/client/impl/session/Account$RegistrationInfo;)V");
		if (!meth) {
			return;
		}

		LOG_D(TAG, "Forwarding Registration status to JAVA");

		jobject account = (jobject) pjsua_acc_get_user_data(acc_id);
		jobject regInfo = tw_reg_info_wrap(env, info);
		env->CallVoidMethod(callbacks, meth, account, regInfo);
		*/

    }

    void onUnregistrationDidComplete(TSCErrorObject* error)
    {
    	__android_log_print(ANDROID_LOG_VERBOSE, "JNI SIGNAL", "onUnregistrationDidComplete", 1);
    }

};
