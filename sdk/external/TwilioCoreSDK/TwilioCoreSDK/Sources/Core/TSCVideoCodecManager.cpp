//
//  TSCVideoCodecManager.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/4/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoCodecManager.h"
#include "ITSCVideoCodec.h"

namespace twiliosdk {
    
class TSCVideoCodecManager::TImpl
{
public:
    
    static TImpl& instance()
    {
        static TImpl instance;
        return instance;
    }
    
    ~TImpl()
    {
    }
    
    void addVideoCodec(const TSCVideoCodecRef& codec)
    {
        m_video_codecs.push_back(codec);
    }
    
    std::vector<TSCVideoCodecRef> getVideoCodecs()
    {
        return m_video_codecs;
    }
    
private:
    TImpl()
    {
    }
    
    std::vector<TSCVideoCodecRef> m_video_codecs;
};

#pragma mark-

TSCVideoCodecManager::TSCVideoCodecManager()
{
}

TSCVideoCodecManager::~TSCVideoCodecManager()
{
}
    
#pragma mark-
    
void
TSCVideoCodecManager::addVideoCodec(const TSCVideoCodecRef& codec)
{
    return TImpl::instance().addVideoCodec(codec);
}
    
std::vector<TSCVideoCodecRef>
TSCVideoCodecManager::getVideoCodecs()
{
    return TImpl::instance().getVideoCodecs();
}

}
