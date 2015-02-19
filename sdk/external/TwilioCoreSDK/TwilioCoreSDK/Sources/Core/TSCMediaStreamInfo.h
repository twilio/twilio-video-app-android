//
//  TSCMediaStreamInfo.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_MEDIA_STREAM_INFO_H
#define TSC_MEDIA_STREAM_INFO_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCMediaStreamInfo
{
public:
    
    TSCMediaStreamInfo(uint64 sessionId,
                       uint64 streamId,
                       const std::string& participantAddress,
                       TSCMediaStreamOrigin streamOrigin);
    TSCMediaStreamInfo(const TSCMediaStreamInfo&);
    virtual ~TSCMediaStreamInfo();

    TSCMediaStreamInfo& operator=(const TSCMediaStreamInfo&);
    
    uint64 getSessionId() const;
    uint64 getStreamId() const;
    const std::string& getParticipantAddress() const;
    TSCMediaStreamOrigin getStreamOrigin() const;

    const std::vector<TSCVideoTrackInfo>& getVideoTracks() const;
    void setVideoTracks(const std::vector<TSCVideoTrackInfo>& tracks);
    
    const std::vector<TSCAudioTrackInfo>& getAudioTracks() const;
    void setAudioTracks(const std::vector<TSCAudioTrackInfo>& tracks);
    
private:
    TSCMediaStreamInfo();

    uint64 m_session_id;
    uint64 m_stream_id;
    std::string m_participant_address;

    std::vector<TSCVideoTrackInfo> m_video_tracks;
    std::vector<TSCAudioTrackInfo> m_audio_tracks;
    
    TSCMediaStreamOrigin m_stream_origin;
};
    
}

#endif // TSC_MEDIA_STREAM_INFO_H
