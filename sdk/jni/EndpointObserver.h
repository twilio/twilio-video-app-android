//
//  TSCEndpointObserver.h
//  Twilio Signal Android SDK
//

#include <twilio-jni/twilio-jni.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "TSCoreSDK.h"
#include "TSCoreSDKTypes.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCIncomingSession.h"


using namespace twiliosdk;

#define TAG  "SignalCore(native)"

class EndpointObserver: public TSCEndpointObserverObject
{
public:
    EndpointObserver(JNIEnv* env, jobject config, jobject target);
    virtual ~EndpointObserver();
    void destroy(JNIEnv* env);
    void setEndpoint(TSCEndpointObjectRef endpoint);

protected:
    virtual void onRegistrationDidComplete(TSCErrorObject* error);
    virtual void onUnregistrationDidComplete(TSCErrorObject* error);
    virtual void onStateDidChange(TSCEndpointState state);
    virtual void onIncomingCallDidReceive(TSCIncomingSession* session);


private:
    jobject m_config;
    TSCEndpointObjectRef endpoint;
    jobject m_target;
};

