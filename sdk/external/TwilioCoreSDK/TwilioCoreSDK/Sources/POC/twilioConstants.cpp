
#include "twilioConstants.h"

namespace twiliosdk {


const int TwilioDefaultCaptureFeedbackInterval = 5000; 

// Ports on chunder
const int TWILIO_DEFAULT_CHUNDER_PORT_TLS = 10194;
//const int TWILIO_DEFAULT_CHUNDER_PORT_TCP = 10193;
const int TWILIO_DEFAULT_CHUNDER_PORT_TCP = 5060;

// Standard Twilio SIP header key values
const char* TWILIO_CALLSID_SIP_HEADER = "X-Twilio-CallSid";
const char* TWILIO_USERNAME_SIP_HEADER = "X-Twilio-Username";
const char* TWILIO_PASSWORD_SIP_HEADER = "X-Twilio-Password";
const char* TWILIO_PARAMS_SIP_HEADER = "X-Twilio-Params";
const char* TWILIO_TOKEN_SIP_HEADER = "X-Twilio-Token";
const char* TWILIO_CLIENT_SIP_HEADER = "X-Twilio-Client";
const char* TWILIO_ACCOUNTSID_SIP_HEADER = "X-Twilio-AccountSid";

// Client GLL SIP header constants
const char* TWILIO_CLIENTVERSION_SIP_HEADER = "X-Twilio-ClientVersion";
const char* TWILIO_CLIENTVERSION_NUMBER = "2";

}
