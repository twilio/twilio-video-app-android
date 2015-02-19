//
//  TSCoreSDKTypes.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/29/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_SDK_TYPES_H
#define TSC_SDK_TYPES_H

#include <map>
#include <vector>
#include <list>
#include <string>
#include "talk/base/scoped_ptr.h"
#include "talk/base/scoped_ref_ptr.h"
#include "talk/base/refcount.h"

namespace twiliosdk {

typedef enum _TSCSIPTransportType {
    kTSCSIPTransportTypeUDP = 0,
    kTSCSIPTransportTypeTCP = 1,
    kTSCSIPTransportTypeTLS = 2
} TSCSIPTransportType;

typedef enum _TSCDisconnectReason {
    kTSCDisconnectReasonParticipantTerminated  = 1,
    kTSCDisconnectReasonWillReconnectPeer = 2
} TSCDisconnectReason;
    
typedef enum _TSCMediaStreamOrigin {
    kTSCMediaStreamLocal  = 0,
    kTSCMediaStreamRemote = 1
} TSCMediaStreamOrigin;
    
typedef enum _TSCSessionState {
    kTSCSessionStateInitialized = 0,
    kTSCSessionStateStarting    = 1,
    kTSCSessionStateInProgress  = 2,
    kTSCSessionStateStartFailed = 3,
    kTSCSessionStateStopping    = 4,
    kTSCSessionStateStopped     = 5,
    kTSCSessionStateStopFailed  = 6
} TSCSessionState;
    
typedef enum _TSCEndpointState {
    kTSCEndpointStateInitialized            = 0,
    kTSCEndpointStateRegistering            = 1,
    kTSCEndpointStateRegistered             = 2,
    kTSCEndpointStateRegistrationFailed     = 3,
    kTSCEndpointStateUnregistering          = 4,
    kTSCEndpointStateUnregistered           = 5,
    kTSCEndpointStateUnregisterationFailed  = 6
} TSCEndpointState;
    
typedef enum _TSCVideoCaptureDeviceType {
    kTSCVideoCaptureNone        = 0,
    kTSCVideoCaptureFrontCamera = 1,
    kTSCVideoCaptureBackCamera  = 2,
    kTSCVideoCaptureScreen      = 3
} TSCVideoCaptureDeviceType;
    
typedef std::map<std::string, std::string> TSCOptions;

class TSCLogChannel;
typedef talk_base::scoped_refptr<TSCLogChannel> TSCLogChannelRef;
    
class TSCError;
typedef talk_base::RefCountedObject<TSCError> TSCErrorObject;
typedef talk_base::scoped_refptr<TSCErrorObject> TSCErrorObjectRef;

class TSCEvent;
typedef talk_base::RefCountedObject<TSCEvent> TSCEventObject;
typedef talk_base::scoped_refptr<TSCEventObject> TSCEventObjectRef;

class TSCParticipant;
typedef talk_base::RefCountedObject<TSCParticipant> TSCParticipantObject;
typedef talk_base::scoped_refptr<TSCParticipantObject> TSCParticipantObjectRef;
    
class TSCSessionObserver;
typedef talk_base::RefCountedObject<TSCSessionObserver> TSCSessionObserverObject;
typedef talk_base::scoped_refptr<TSCSessionObserverObject> TSCSessionObserverObjectRef;

class TSCSession;
class TSCOutgoingSession;
class TSCIncomingSession;
    
typedef talk_base::scoped_refptr<TSCSession> TSCSessionRef;
    
typedef talk_base::RefCountedObject<TSCOutgoingSession> TSCOutgoingSessionObject;
typedef talk_base::scoped_refptr<TSCOutgoingSessionObject> TSCOutgoingSessionObjectRef;

typedef talk_base::RefCountedObject<TSCIncomingSession> TSCIncomingSessionObject;
typedef talk_base::scoped_refptr<TSCIncomingSessionObject> TSCIncomingSessionObjectRef;

typedef std::list<TSCSession*> TSCSessionList;
typedef std::list<TSCSession*>::iterator TSCSessionListIterator;
typedef std::list<TSCSession*>::const_iterator TSCSessionListConstIterator;
   
class TSCPeerConnectionObserver;
typedef talk_base::RefCountedObject<TSCPeerConnectionObserver> TSCPeerConnectionObserverObject;
typedef talk_base::scoped_refptr<TSCPeerConnectionObserverObject> TSCPeerConnectionObserverObjectRef;
    
class TSCPeerConnection;
class TSCOutgoingPeerConnection;
class TSCIncomingPeerConnection;
    
typedef talk_base::RefCountedObject<TSCOutgoingPeerConnection> TSCOutgoingPeerConnectionObject;
typedef talk_base::RefCountedObject<TSCIncomingPeerConnection> TSCIncomingPeerConnectionObject;

typedef talk_base::scoped_refptr<TSCPeerConnection> TSCPeerConnectionObjectRef;
    
class TSCParticipantConnection;
typedef talk_base::scoped_refptr<TSCParticipantConnection> TSCParticipantConnectionRef;

class TSCParticipantConnectionObserver;
typedef talk_base::RefCountedObject<TSCParticipantConnectionObserver> TSCParticipantConnectionObserverObject;
typedef talk_base::scoped_refptr<TSCParticipantConnectionObserver> TSCParticipantConnectionObserverRef;


class TSCSIPAccount;
typedef talk_base::RefCountedObject<TSCSIPAccount> TSCSIPAccountObject;
typedef talk_base::scoped_refptr<TSCSIPAccountObject> TSCSIPAccountObjectRef;
    
class TSCSIPCall;
typedef talk_base::RefCountedObject<TSCSIPCall> TSCSIPCallObject;
typedef talk_base::scoped_refptr<TSCSIPCallObject> TSCSIPCallObjectRef;
    
class TSCEndpoint;
typedef talk_base::RefCountedObject<TSCEndpoint> TSCEndpointObject;
typedef talk_base::scoped_refptr<TSCEndpoint> TSCEndpointObjectRef;

class TSCEndpointObserver;
typedef talk_base::RefCountedObject<TSCEndpointObserver> TSCEndpointObserverObject;
typedef talk_base::scoped_refptr<TSCEndpointObserverObject> TSCEndpointObserverObjectRef;

class TSCSessionLifeCycleObserver;
typedef talk_base::RefCountedObject<TSCSessionLifeCycleObserver> TSCSessionLifeCycleObserverObject;
typedef talk_base::scoped_refptr<TSCSessionLifeCycleObserverObject> TSCSessionLifeCycleObserverObjectRef;

class TSCPJSUA;
typedef talk_base::RefCountedObject<TSCPJSUA> TSCPJSUAObject;
typedef talk_base::scoped_refptr<TSCPJSUAObject> TSCPJSUAObjectRef;

class TSCVideoSurface;
typedef talk_base::RefCountedObject<TSCVideoSurface> TSCVideoSurfaceObject;
typedef talk_base::scoped_refptr<TSCVideoSurfaceObject> TSCVideoSurfaceObjectRef;

class TSCVideoSurfaceObserver;
typedef talk_base::RefCountedObject<TSCVideoSurfaceObserver> TSCVideoSurfaceObserverObject;
typedef talk_base::scoped_refptr<TSCVideoSurfaceObserverObject> TSCVideoSurfaceObserverObjectRef;
    
class TSCVideoTrackRender;
typedef talk_base::RefCountedObject<TSCVideoTrackRender> TSCVideoTrackRenderObject;
typedef talk_base::scoped_refptr<TSCVideoTrackRenderObject> TSCVideoTrackRenderObjectRef;
typedef std::map<std::string, TSCVideoTrackRenderObjectRef> TSVideoTrackRenderCollection;
typedef std::map<std::string, TSCVideoTrackRenderObjectRef>::iterator TSVideoTrackRenderCollectionIterator;
typedef std::map<std::string, TSCVideoTrackRenderObjectRef>::const_iterator TSVideoTrackRenderCollectionConstIterator;

class TSCVideoTrackInfo;
typedef talk_base::RefCountedObject<TSCVideoTrackInfo> TSCVideoTrackInfoObject;
typedef talk_base::scoped_refptr<TSCVideoTrackInfoObject> TSCVideoTrackInfoObjectRef;

class TSCAudioTrackInfo;
typedef talk_base::RefCountedObject<TSCAudioTrackInfo> TSCAudioTrackInfoObject;
typedef talk_base::scoped_refptr<TSCAudioTrackInfoObject> TSCAudioTrackInfoObjectRef;
    
class TSCVideoTrackEventData;
typedef talk_base::RefCountedObject<TSCVideoTrackEventData> TSCVideoTrackEventDataObject;
typedef talk_base::scoped_refptr<TSCVideoTrackEventDataObject> TSCVideoTrackEventDataObjectRef;

class TSCVideoCaptureDeviceInfo;
class TSCAudioInputDeviceInfo;
    
class TSCDeviceManager;
typedef talk_base::RefCountedObject<TSCDeviceManager> TSCDeviceManagerObject;
typedef talk_base::scoped_refptr<TSCDeviceManagerObject> TSCDeviceManagerObjectRef;

class TSCMediaStreamInfo;
typedef talk_base::RefCountedObject<TSCMediaStreamInfo> TSCMediaStreamInfoObject;
typedef talk_base::scoped_refptr<TSCMediaStreamInfoObject> TSCMediaStreamInfoObjectRef;

class IAudioInputControllerInterface;
class IVideoCaptureControllerInterface;

class TSCAudioInputController;
typedef talk_base::scoped_refptr<TSCAudioInputController> TSCAudioInputControllerRef;

class TSCAudioInputControllerImpl;
typedef talk_base::scoped_refptr<TSCAudioInputControllerImpl> TSCAudioInputControllerImplRef;
    
class TSCVideoCaptureControllerImpl;
typedef talk_base::scoped_refptr<TSCVideoCaptureControllerImpl> TSCVideoCaptureControllerImplRef;

class TSCVideoCaptureController;
typedef talk_base::scoped_refptr<TSCVideoCaptureController> TSCVideoCaptureControllerRef;

class TSCScreenCaptureProvider;
typedef talk_base::RefCountedObject<TSCScreenCaptureProvider> TSCScreenCaptureProviderObject;
typedef talk_base::scoped_refptr<TSCScreenCaptureProvider> TSCScreenCaptureProviderRef;
    
class ITSCVideoCodec;
typedef talk_base::scoped_refptr<ITSCVideoCodec> TSCVideoCodecRef;

}  // namespace twiliosdk

#endif  // TSC_SDK_TYPES_H
