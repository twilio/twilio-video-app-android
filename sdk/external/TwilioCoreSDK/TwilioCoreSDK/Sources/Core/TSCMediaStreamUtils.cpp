//
//  TSCMediaStreamUtils.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCMediaStreamUtils.h"
#include "TSCMediaStreamInfo.h"
#include "TSCMediaTrackInfo.h"

namespace twiliosdk {

namespace {
    
    std::string createTrackID(uint64 streamUID, const std::string& streamLabel, uint64 trackUID)
    {
        char buf[128];
        sprintf(buf, "%llu:%s:%llu", streamUID, streamLabel.c_str(), trackUID);
        return (std::string) buf;
    }
    
}

TSCMediaStreamInfoObject*
TSCMediaStreamUtils::createMediaStreamInfo(uint64 sessionId,
                                           webrtc::MediaStreamInterface* stream,
                                           const std::string& participantAddress,
                                           TSCMediaStreamOrigin origin)
{
    TSCMediaStreamInfoObject* info = new TSCMediaStreamInfoObject(sessionId, (uint64) stream,
                                                                  participantAddress, origin);
    std::vector<TSCVideoTrackInfo> videoTracks;
    for (unsigned int i = 0; i < stream->GetVideoTracks().size(); ++i)
    {
        webrtc::VideoTrackInterface* track = stream->GetVideoTracks()[i].get();
        std::string trackUID = createTrackID((uint64) stream,
                                             stream->label(),
                                             (uint64)track);
        TSCMediaTrackIdentity identity(sessionId, (uint64) stream, trackUID, origin);
        TSCVideoTrackInfo trackInfo(identity, participantAddress);
        videoTracks.push_back(trackInfo);
    }
    info->setVideoTracks(videoTracks);

    std::vector<TSCAudioTrackInfo> audioTracks;
    for (unsigned int i = 0; i < stream->GetAudioTracks().size(); ++i)
    {
        webrtc::AudioTrackInterface* track = stream->GetAudioTracks()[i].get();
        std::string trackUID = createTrackID((uint64) stream,
                                             stream->label(),
                                             (uint64)track);
        TSCMediaTrackIdentity identity(sessionId, (uint64) stream, trackUID, origin);
        TSCAudioTrackInfo trackInfo(identity, participantAddress);
        audioTracks.push_back(trackInfo);
    }
    info->setAudioTracks(audioTracks);

    return info;
}
    
}