//
//  TSCParticipant.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PARTICIPANT_H
#define TSC_PARTICIPANT_H

#include <string>

namespace twiliosdk {

class TSCParticipant
{
public:
    TSCParticipant(const std::string& address);
    TSCParticipant(const TSCParticipant&);
    virtual ~TSCParticipant();

    TSCParticipant& operator=(const TSCParticipant&);

    const std::string& getAddress() const;
    
private:
    TSCParticipant();
    std::string m_address;
};
    
}

#endif //TSC_PARTICIPANT_H
