//
//  TSCEvent.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_EVENT_H
#define TSC_EVENT_H

#include <string>

namespace twiliosdk {

typedef enum _TSCEventType {
    kTSCoreEventTypeICECandidateFound = 1,
} TSCEventType;

class TSCEvent
{
public:
    TSCEvent(TSCEventType eventType, const std::string& eventPayload = "");
    virtual ~TSCEvent();
    
    TSCEventType getType() const;
    const std::string& getPayload() const;
    
private:
    TSCEvent();
    TSCEvent(const TSCEvent&);
    TSCEvent& operator=(TSCEvent&);
    
    TSCEventType m_type;
    std::string m_payload;
};
    
    
}

#endif //TSC_EVENT_H
