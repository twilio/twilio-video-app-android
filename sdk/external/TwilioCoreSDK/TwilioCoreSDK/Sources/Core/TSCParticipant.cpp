//
//  TSCParticipant.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCParticipant.h"

namespace twiliosdk {

TSCParticipant::TSCParticipant(const std::string& address)
{
    m_address = address;
}

TSCParticipant::~TSCParticipant()
{
}

#pragma mark-
    
TSCParticipant::TSCParticipant(const TSCParticipant& object)
{
    m_address = object.getAddress();
}
    
TSCParticipant&
TSCParticipant::operator=(const TSCParticipant& object)
{
    m_address = object.getAddress();
    return *this;
}

#pragma mark-

const std::string&
TSCParticipant::getAddress() const
{
    return m_address;
}

}