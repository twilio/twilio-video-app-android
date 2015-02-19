//
//  TSCMediaStreamInfo.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCMediaStreamInfo.h"
#include "TSCMediaTrackInfo.h"

namespace twiliosdk {

TSCMediaStreamInfo::TSCMediaStreamInfo(uint64 sessionId,
                                       uint64 streamId,
                                       const std::string& participantAddress,
                                       TSCMediaStreamOrigin streamOrigin)
{
    m_session_id = sessionId;
    m_stream_id = streamId;
    m_participant_address = participantAddress;
    m_stream_origin = streamOrigin;
}

TSCMediaStreamInfo::~TSCMediaStreamInfo()
{
}

#pragma mark-
    
TSCMediaStreamInfo::TSCMediaStreamInfo(const TSCMediaStreamInfo& object)
{
    m_session_id = object.getSessionId();
    m_stream_id = object.getStreamId();
    m_participant_address = object.getParticipantAddress();
    m_stream_origin = object.getStreamOrigin();
    m_video_tracks = object.getVideoTracks();
    m_audio_tracks = object.getAudioTracks();
}

TSCMediaStreamInfo&
TSCMediaStreamInfo::operator=(const TSCMediaStreamInfo& object)
{
    if (this != &object)
    {
        m_session_id = object.getSessionId();
        m_stream_id = object.getStreamId();
        m_participant_address = object.getParticipantAddress();
        m_stream_origin = object.getStreamOrigin();
        m_video_tracks = object.getVideoTracks();
        m_audio_tracks = object.getAudioTracks();
    }
    return *this;
}

#pragma mark-

uint64
TSCMediaStreamInfo::getSessionId() const
{
    return m_session_id;
}
    
uint64
TSCMediaStreamInfo::getStreamId() const
{
    return m_stream_id;
}

const std::string&
TSCMediaStreamInfo::getParticipantAddress() const
{
    return m_participant_address;
}

TSCMediaStreamOrigin
TSCMediaStreamInfo::getStreamOrigin() const
{
    return m_stream_origin;
}

const std::vector<TSCVideoTrackInfo>&
TSCMediaStreamInfo::getVideoTracks() const
{
    return m_video_tracks;
}
    
const std::vector<TSCAudioTrackInfo>&
TSCMediaStreamInfo::getAudioTracks() const
{
    return m_audio_tracks;
}
    
#pragma mark-
    
void
TSCMediaStreamInfo::setVideoTracks(const std::vector<TSCVideoTrackInfo>& tracks)
{
    m_video_tracks = tracks;
}

void
TSCMediaStreamInfo::setAudioTracks(const std::vector<TSCAudioTrackInfo>& tracks)
{
    m_audio_tracks = tracks;
}
    
}