//
//  TSCMediaTrackInfo.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCMediaTrackInfo.h"

namespace twiliosdk {

TSCMediaTrackIdentity::TSCMediaTrackIdentity(uint64 sessionId,
                                             uint64 streamId,
                                             const std::string& trackId,
                                             TSCMediaStreamOrigin streamOrigin)
{
    m_session_id = sessionId;
    m_stream_id = streamId;
    m_track_id = trackId;
    m_stream_origin = streamOrigin;
}

TSCMediaTrackIdentity::~TSCMediaTrackIdentity()
{
}

#pragma mark-

TSCMediaTrackIdentity::TSCMediaTrackIdentity(const TSCMediaTrackIdentity& object)
{
    m_session_id = object.getSessionId();
    m_stream_id = object.getStreamId();
    m_track_id = object.getTrackId();
    m_stream_origin = object.getStreamOrigin();
}

TSCMediaTrackIdentity&
TSCMediaTrackIdentity::operator=(const TSCMediaTrackIdentity& object)
{
    if (this != &object)
    {
        m_session_id = object.getSessionId();
        m_stream_id = object.getStreamId();
        m_track_id = object.getTrackId();
        m_stream_origin = object.getStreamOrigin();
    }
    return *this;
}

#pragma mark-
    
uint64
TSCMediaTrackIdentity::getSessionId() const
{
    return m_session_id;
}

uint64
TSCMediaTrackIdentity::getStreamId() const
{
    return m_stream_id;
}
    
const std::string&
TSCMediaTrackIdentity::getTrackId() const
{
    return m_track_id;
}

TSCMediaStreamOrigin
TSCMediaTrackIdentity::getStreamOrigin() const
{
    return m_stream_origin;
}
    
#pragma mark-
    
TSCMediaTrackInfo::TSCMediaTrackInfo(const TSCMediaTrackIdentity& identity,
                                     const std::string& participantAddress):m_identity(identity)
{
    m_participant_address = participantAddress;
}

TSCMediaTrackInfo::~TSCMediaTrackInfo()
{
}

#pragma mark-
    
TSCMediaTrackInfo::TSCMediaTrackInfo(const TSCMediaTrackInfo& object):m_identity(object.getIdentity())
{
    m_participant_address = object.getParticipantAddress();
}

TSCMediaTrackInfo&
TSCMediaTrackInfo::operator=(const TSCMediaTrackInfo& object)
{
    if (this != &object)
    {
        m_identity = object.getIdentity();
        m_participant_address = object.getParticipantAddress();
    }
    return *this;
}

#pragma mark-
    
const TSCMediaTrackIdentity&
TSCMediaTrackInfo::getIdentity() const
{
    return m_identity;
}
    
uint64
TSCMediaTrackInfo::getSessionId() const
{
    return m_identity.getSessionId();
}
    
const std::string&
TSCMediaTrackInfo::getTrackId() const
{
    return m_identity.getTrackId();
}

const std::string&
TSCMediaTrackInfo::getParticipantAddress() const
{
    return m_participant_address;
}

TSCMediaStreamOrigin
TSCMediaTrackInfo::getStreamOrigin() const
{
    return m_identity.getStreamOrigin();
}

#pragma mark-
    
TSCVideoTrackInfo::TSCVideoTrackInfo(const TSCMediaTrackIdentity& identity,
                                     const std::string& participantAddress):
    TSCMediaTrackInfo(identity, participantAddress)
{
}

TSCVideoTrackInfo::~TSCVideoTrackInfo()
{
}

#pragma mark-

TSCVideoTrackInfo::TSCVideoTrackInfo(const TSCVideoTrackInfo& object):
    TSCMediaTrackInfo(object)
{
}

TSCVideoTrackInfo&
TSCVideoTrackInfo::operator=(const TSCVideoTrackInfo& object)
{
    TSCMediaTrackInfo::operator=(object);
    return *this;
}

#pragma mark-
    
    TSCAudioTrackInfo::TSCAudioTrackInfo(const TSCMediaTrackIdentity& identity,
                                         const std::string& participantAddress):
    TSCMediaTrackInfo(identity, participantAddress)
{
}

TSCAudioTrackInfo::~TSCAudioTrackInfo()
{
}
    
#pragma mark-
    
TSCAudioTrackInfo::TSCAudioTrackInfo(const TSCAudioTrackInfo& object):
    TSCMediaTrackInfo(object)
{
}

TSCAudioTrackInfo&
TSCAudioTrackInfo::operator=(const TSCAudioTrackInfo& object)
{
    TSCMediaTrackInfo::operator=(object);
    return *this;
}
    
}