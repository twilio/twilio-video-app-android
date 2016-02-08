#ifndef ACCESS_MANAGER_H
#define ACCESS_MANAGER_H

#include <string>
#include <vector>
#include <iostream>
#include <map>

namespace TwilioPoco {
class Timer;
}

namespace TwilioCommon {

typedef std::map<std::string, std::string> TwilioTokenPayload;
class AccessManagerObserver;

class AccessManager {
public:
    AccessManager(std::string &jwt, AccessManagerObserver* obs = 0);
    virtual ~AccessManager();
   
    std::string        getToken(); 
    TwilioTokenPayload getTokenPayload();
    std::string        getIdentity(); 
    bool               isExpired();
    double             getTokenExpTime();
    void               updateToken(const std::string &jwt); //Async
    void               attachObserver(AccessManagerObserver* obs);
    void               detachObserver(AccessManagerObserver* obs);

private:
    void onTokenExpiredPriv(TwilioPoco::Timer &timer);

    std::string         m_token;
    std::string         m_identity;
    TwilioTokenPayload  m_payload;
    bool                m_expired;
    double              m_exptime;
    TwilioPoco::Timer*  m_timer;
    std::vector<AccessManagerObserver*> m_observers;
};

class AccessManagerObserver {
public:
    virtual ~AccessManagerObserver(){}
    virtual void onTokenExpired(AccessManager* mgr){}
    virtual void onTokenUpdated(AccessManager* mgr){}
    virtual void onError(const std::string &errorMsg){}

protected:
    AccessManagerObserver(){}
};

} //namespace TwilioCommon

#endif //ACCESS_MANAGER_H
