//
//  ITSCVideoCodec.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/4/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CODEC_H
#define TSC_VIDEO_CODEC_H

#include "TSCoreSDKTypes.h"

namespace cricket {
    class WebRtcVideoDecoderFactory;
    class WebRtcVideoEncoderFactory;
}

namespace twiliosdk {
    
class ITSCVideoCodec : public talk_base::RefCountInterface
{
public:
    virtual ~ITSCVideoCodec() {}
    virtual cricket::WebRtcVideoEncoderFactory* createVideoEncoderFactory() = 0;
    virtual cricket::WebRtcVideoDecoderFactory* createVideoDecoderFactory() = 0;

    virtual const std::string getName() = 0;
};
    
} // namespace twiliosdk

#endif // TSC_VIDEO_CODEC_H
