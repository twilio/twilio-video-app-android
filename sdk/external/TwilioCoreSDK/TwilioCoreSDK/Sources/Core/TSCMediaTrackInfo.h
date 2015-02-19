//
//  TSCMediaTrackInfo.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_MEDIA_TRACK_INFO_H
#define TSC_MEDIA_TRACK_INFO_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCMediaTrackIdentity
{
public:
    
    TSCMediaTrackIdentity(uint64 sessionId,
                          uint64 streamId,
                          const std::string& trackId,
                          TSCMediaStreamOrigin streamOrigin);
    TSCMediaTrackIdentity(const TSCMediaTrackIdentity&);
    virtual ~TSCMediaTrackIdentity();
    
    TSCMediaTrackIdentity& operator=(const TSCMediaTrackIdentity&);
    
    uint64 getSessionId() const;
    uint64 getStreamId() const;
    const std::string& getTrackId() const;
    TSCMediaStreamOrigin getStreamOrigin() const;

private:
    TSCMediaTrackIdentity();
    
    uint64 m_session_id;
    uint64 m_stream_id;
    std::string m_track_id;
    TSCMediaStreamOrigin m_stream_origin;
};
    
class TSCMediaTrackInfo
{
public:
    
    TSCMediaTrackInfo(const TSCMediaTrackIdentity& identity,
                      const std::string& participantAddress);
    TSCMediaTrackInfo(const TSCMediaTrackInfo&);
    virtual ~TSCMediaTrackInfo();

    TSCMediaTrackInfo& operator=(const TSCMediaTrackInfo&);
    
    const TSCMediaTrackIdentity& getIdentity() const;
    
    uint64 getSessionId() const;
    uint64 getStreamId() const;
    const std::string& getTrackId() const;
    const std::string& getParticipantAddress() const;
    TSCMediaStreamOrigin getStreamOrigin() const;
    
private:
    TSCMediaTrackInfo();

    TSCMediaTrackIdentity m_identity;
    std::string m_participant_address;
};
    
class TSCVideoTrackInfo: public TSCMediaTrackInfo
{
public:

    TSCVideoTrackInfo(const TSCMediaTrackIdentity& identity,
                      const std::string& participantAddress);
    TSCVideoTrackInfo(const TSCVideoTrackInfo&);
    virtual ~TSCVideoTrackInfo();
    
    TSCVideoTrackInfo& operator=(const TSCVideoTrackInfo&);
    
private:
    TSCVideoTrackInfo();
};

class TSCAudioTrackInfo: public TSCMediaTrackInfo
{
public:
    
    TSCAudioTrackInfo(const TSCMediaTrackIdentity& identity,
                      const std::string& participantAddress);
    TSCAudioTrackInfo(const TSCAudioTrackInfo&);
    virtual ~TSCAudioTrackInfo();
    
    TSCAudioTrackInfo& operator=(const TSCAudioTrackInfo&);
    
private:
    TSCAudioTrackInfo();
};
    
}

#endif // TSC_MEDIA_TRACK_INFO_H
