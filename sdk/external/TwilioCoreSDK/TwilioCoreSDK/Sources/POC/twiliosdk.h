#ifndef TWILIOSDK_H
#define TWILIOSDK_H

#include <string>
#include <vector>

#ifdef WIN32
#include <stdint.h>
#endif

namespace twiliosdk {


// SDK initialization states
typedef enum twilioSdkInitState {
    UNINITIALIZED,
    INITIALIZING,
    INITIALIZATION_ERROR,
    LOGGINGIN,
    LOGGINGIN_ERROR,
    INITIALIZED
} TwilioSdkInitState;

// SDK call states
typedef enum twilioSdkCallState {
    INITIATING_CALL,
    INITIATING_CALL_FAILED,
    INCOMING_CALL,
    ACCEPTING_CALL,
    ACCEPTING_CALL_FAILED,
    CONNECTING,
    CONNECTING_FAILED,
    CONNECTED,
    DISCONNECTING,
    TERMINATED,
    RINGING,
    REJECTED,
    USER_NOT_AVAILABLE
} TwilioSdkCallState;

// SDK call mode
typedef enum twilioSdkCallMode {
    UNDEFINED,
    INITIATE,
    ANSWER
} TwilioSdkCallMode;

typedef enum twilioSdkMediaType {
    TYPE_UNKNOWN_MEDIA,
    TYPE_AUDIO_INPUT,
    TYPE_AUDIO_OUTPUT,
    TYPE_VIDEO_INPUT,
    TYPE_VIDEO_OUTPUT,
    TYPE_DATA
} TwilioSdkMediaType;

// SDK VideoConstraints
typedef enum twilioSdkVideoConstraints {
    MIN_ASPECT_RATIO,
    MAX_ASPECT_RATIO,
    MIN_WIDTH,
    MAX_WIDTH,
    MIN_HEIGHT,
    MAX_HEIGHT,
    MIN_FRAMERATE,
    MAX_FRAMERATE
} TwilioSdkVideoConstraints;
   
typedef enum twilioSdkConstraintType {
    TYPE_MANDATORY = 0,
    TYPE_OPTIONAL = 1
} TwilioSdkConstraintType;

// SDK Source format
typedef enum twilioSdkSourceFormat {
    AUDIO,
    VIDEO_I420, //YUV420 primary
    VIDEO_ARGB,
    VIDEO_RGBA,
    VIDEO_RBGA,
    VIDEO_BGRA,
    VIDEO_24BG, //24 bpp
} TwilioSdkSourceFormat;

// structure passed to initialize SDK
typedef struct twilioSdkInitParams {
    std::string         alias;
    std::string         user;
    std::string         password;
    std::string         accSid;
    std::string         domain;
    std::string         registrar;
    std::string         stun;
    std::string         turn;
    std::string         capabilityToken;
    bool                enableLogger;
    std::string         logFolder;
} TwilioSdkInitParams;

// structure with call data
typedef struct twilioSdkCallParams {
    unsigned int        callId;
    std::string         localUser;
    std::string         remoteUser;
    TwilioSdkCallMode   callMode;
    TwilioSdkCallState  callState;
} TwilioSdkCallParams;

// observer to receive init/call state notifications
class TwilioSdkObserverInterface {
public:
    virtual void onInitStateChange (TwilioSdkInitState state) = 0;
    virtual void onCallStateChange (const TwilioSdkCallParams *callData) = 0;

    //Source related events. Here source mean something that can be played, like local/remote video.
    virtual void onSourceAdded(const std::string& sourceId,
                               const TwilioSdkMediaType sourceType) = 0;
    virtual void onSourceRemoved(const std::string& sourceId) = 0;
    virtual void onSourceDataAvailable(const std::string& sourceId) = 0;
    virtual void onSourceVideoTrack(const std::string& sourceId,
                                    const int width,
                                    const int height,
                                    uint8_t** providedBuffer,
                                    TwilioSdkSourceFormat& neededFormat) = 0;
    //Capture related events. Capture means something we can capture locally and send to remote, like audio
    virtual void onCaptureAdded(const std::string& sourceId,
                               const TwilioSdkMediaType sourceType) = 0;
    virtual void onCaptureRemoved(const std::string& sourceId,
                                  const TwilioSdkMediaType sourceType) = 0;
    virtual void onCaptureFeedbackAvailable(const uint32_t last_effective_width, 
                                            const uint32_t last_effective_height, 
                                            const uint32_t last_effective_framerate) = 0;
    virtual ~TwilioSdkObserverInterface () {}
};

// forward declarations
class TwilioSdkWorker;

// main Twilio SDK interface
class TwilioSDK {
public:
    TwilioSDK();
    ~TwilioSDK();

public:
    bool init (const std::string &uuid,
               TwilioSdkInitParams *params,
               TwilioSdkObserverInterface *observer);
    void call (const std::string &user);

    void answer (unsigned int callId);
    void reject (unsigned int callId);

    void terminate (unsigned int callId);

    bool initialized () const;

    void startPlayback(const std::string &sourceId);
    void stopPlayback(const std::string &sourceId);

    void startCapture(TwilioSdkMediaType type);
    void stopCapture(TwilioSdkMediaType type);
  
    void listDevices(TwilioSdkMediaType type, std::vector<std::string>& devices);
    void setCurrentDevice(TwilioSdkMediaType type, const std::string& device);
    void getCurrentDevice(TwilioSdkMediaType type, std::string& device);

    void setCaptureFeedbackInterval(int64_t timeInMsec);
    void setVolume(int level);
    void getVolume(int &level);

    template <typename T> 
        void setVideoConstraints(TwilioSdkVideoConstraints videoConstraints, T value, TwilioSdkConstraintType type); 

 private:
    TwilioSdkWorker *worker_;
};

}  // namespace twiliosdk

#endif // TWILIOSDK_H
