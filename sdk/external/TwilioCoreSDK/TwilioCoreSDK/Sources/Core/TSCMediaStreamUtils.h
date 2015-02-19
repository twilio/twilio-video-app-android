//
//  TSCMediaStreamUtils.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_MEDIA_STREAM_UTILS_H
#define TSC_MEDIA_STREAM_UTILS_H

#include "TSCoreSDKTypes.h"
#include "talk/app/webrtc/mediastreaminterface.h"

namespace twiliosdk {

class TSCMediaStreamUtils
{
public:
    
    static TSCMediaStreamInfoObject* createMediaStreamInfo(uint64 sessionId,
                                                           webrtc::MediaStreamInterface* stream,
                                                           const std::string& participantAddress,
                                                           TSCMediaStreamOrigin origin);
    
};
    
    
}

#endif // TSC_MEDIA_STREAM_UTILS_H
