#ifndef TOKEN_UTILS_H
#define TOKEN_UTILS_H

#include <string>
#include <map>

#include "Base64.h"

#include "Poco/JSON/JSON.h"
#include "Poco/JSON/Object.h"
#include "Poco/JSON/Array.h"
#include "Poco/JSON/Parser.h"
#include "Poco/JSON/Stringifier.h"
#include "Poco/Dynamic/Var.h"
#include "Poco/Timer.h"
#include "Poco/Exception.h"


namespace TwilioCommon {

typedef std::map<std::string, std::string> TwilioTokenPayload;

//payload keys
extern const char* kPayloadAccountSid;
extern const char* kPayloadGrants;
extern const char* kPayloadExpTime;
extern const char* kPayloadIdentity;
extern const char* kPayloadFormat;

class TokenUtils {
public:
    static bool        parseToken(const std::string &token, TwilioTokenPayload &payload);
    static double      getTimeRemainingBeforeExpiry(double expTime);
    static bool        hasTokenExpired(double expTime);

private:
    static void extractTokenPayload(const TwilioPoco::Dynamic::Var &parsedToken, TwilioTokenPayload &payload);
    static bool isFormatNew(const std::string &grantsJson);
    static std::string extractIdentityFromGrants(const std::string &grantsJson);
};

} //namespace TwilioCommon

#endif //TOKEN_UTILS_H
