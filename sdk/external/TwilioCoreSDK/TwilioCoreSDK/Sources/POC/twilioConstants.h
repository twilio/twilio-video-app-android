
#ifndef TWILIOCONSTANTS_H
#define TWILIOCONSTANTS_H

namespace twiliosdk {

extern const int TwilioDefaultCaptureFeedbackInterval;

// Sip Transport udp/tcp/tls
typedef enum twilioSipTransportType {
    TWILIO_SIP_TRANSPORT_TYPE_UDP,
    TWILIO_SIP_TRANSPORT_TYPE_TCP,
    TWILIO_SIP_TRANSPORT_TYPE_TLS
} TwilioSipTransportType;

// Ports on chunder
extern const int TWILIO_DEFAULT_CHUNDER_PORT_TLS;
//const int TWILIO_DEFAULT_CHUNDER_PORT_TCP = 10193;
extern const int TWILIO_DEFAULT_CHUNDER_PORT_TCP;

// Standard Twilio SIP header key values
extern const char* TWILIO_CALLSID_SIP_HEADER;
extern const char* TWILIO_USERNAME_SIP_HEADER;
extern const char* TWILIO_PASSWORD_SIP_HEADER;
extern const char* TWILIO_PARAMS_SIP_HEADER;
extern const char* TWILIO_TOKEN_SIP_HEADER;
extern const char* TWILIO_CLIENT_SIP_HEADER;
extern const char* TWILIO_ACCOUNTSID_SIP_HEADER;

// Client GLL SIP header constants
extern const char* TWILIO_CLIENTVERSION_SIP_HEADER;
extern const char* TWILIO_CLIENTVERSION_NUMBER;

}

#endif //TWILIOCONSTANTS_H
