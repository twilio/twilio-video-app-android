//
//  TSCEvent.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCEvent.h"

namespace twiliosdk {

TSCEvent::TSCEvent(TSCEventType eventType, const std::string& eventPayload)
{
    m_type = eventType;
    m_payload = eventPayload;
}

TSCEvent::~TSCEvent()
{
}

#pragma mark-

TSCEventType
TSCEvent::getType() const
{
    return m_type;
}

const std::string&
TSCEvent::getPayload() const
{
    return m_payload;
}

}