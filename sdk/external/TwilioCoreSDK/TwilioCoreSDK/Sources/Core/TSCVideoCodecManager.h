//
//  TSCVideoCodecManager.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/4/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CODEC_MANAGER_H
#define TSC_VIDEO_CODEC_MANAGER_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {
    
class TSCVideoCodecManager
{
public:
    TSCVideoCodecManager();
    virtual ~TSCVideoCodecManager();
    
    void addVideoCodec(const TSCVideoCodecRef& codec);
    std::vector<TSCVideoCodecRef> getVideoCodecs();
private:
    TSCVideoCodecManager(const TSCVideoCodecManager&);
    TSCVideoCodecManager& operator=(TSCVideoCodecManager&);
    class TImpl;
};
    
}
#endif // TSC_VIDEO_CODEC_MANAGER_H
