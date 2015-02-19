#include "twiliosdk.h"
#include "twilioSdkWorker.h"
#include "twilioDeviceManager.h"

namespace twiliosdk {

TwilioSDK::TwilioSDK() :
    worker_(NULL) {
}

TwilioSDK::~TwilioSDK() {
    // cleanup SDK worker
    if (worker_)
        delete worker_;

    // cleanup OpenSSL
    talk_base::OpenSSLAdapter::CleanupSSL();
}

bool TwilioSDK::init(const std::string &uuid, TwilioSdkInitParams *params,
        TwilioSdkObserverInterface *observer) {
    // initialize OpenSSL library
    if (talk_base::OpenSSLAdapter::InitializeSSL(NULL)) {
        worker_ = new TwilioSdkWorker(uuid, params, observer);
        talk_base::Thread *worker = new talk_base::Thread();
        // run the initialization
        worker->Start(worker_);

        // we are done for now
        return true;
    } else {
        return false;
    }
}

void TwilioSDK::startPlayback(const std::string &sourceId) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_START_PLAYBACK,
                            talk_base::WrapMessageData(MediaSourceData(sourceId, TYPE_UNKNOWN_MEDIA)));
    } else {
        LOG_ERROR("SDK not initialized, cannot play streams!");
    }
}

void TwilioSDK::stopPlayback(const std::string &sourceId) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_STOP_PLAYBACK,
                            talk_base::WrapMessageData(MediaSourceData(sourceId, TYPE_UNKNOWN_MEDIA)));
    } else {
        LOG_ERROR("SDK not initialized, cannot stop streams!");
    }
}

void TwilioSDK::startCapture(TwilioSdkMediaType type) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_START_CAPTURE,
                            talk_base::WrapMessageData(MediaSourceData("", type)));
    } else {
        LOG_ERROR("SDK not initialized, cannot play streams!");
    }
}

void TwilioSDK::stopCapture(TwilioSdkMediaType type) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_STOP_CAPTURE,
                            talk_base::WrapMessageData(MediaSourceData("", type)));
    } else {
        LOG_ERROR("SDK not initialized, cannot stop streams!");
    }
}

void TwilioSDK::listDevices(TwilioSdkMediaType type,
        std::vector<std::string>& devices) {
    TwilioDeviceManager::instance().listDevices(type, devices);
}

void TwilioSDK::setCurrentDevice(TwilioSdkMediaType type,
                                 const std::string& device) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_SET_CURRENT_DEVICE,
                            talk_base::WrapMessageData(MediaSourceData(device, type)));
    } else {
        LOG_ERROR("SDK not initialized, cannot play streams!");
    }
}

void TwilioSDK::getCurrentDevice(TwilioSdkMediaType type, std::string& device) {
    TwilioDeviceManager::instance().getCurrentDevice(type, device);
}

void TwilioSDK::setVolume(int level) {
    if (initialized()) {
        worker_->post(TwilioSdkWorker::MSG_SET_VOLUME_LEVEL,
                      talk_base::WrapMessageData(level));
    } else {
        LOG_ERROR("SDK not initialized, cannot play streams!");
    }

}

void TwilioSDK::getVolume(int &level) {
    if (initialized()) {
        worker_->getConnectionManager()->getVolume(level);
    } else {
        LOG_ERROR("SDK not initialized, cannot play streams!");
    }
}

template<typename T>
void TwilioSDK::setVideoConstraints(TwilioSdkVideoConstraints videoConstraints, T value, TwilioSdkConstraintType type) {
    if (initialized()) {
        switch (videoConstraints) {
            case MIN_ASPECT_RATIO:
            case MAX_ASPECT_RATIO:
                worker_->post(TwilioSdkWorker::MSG_SET_VIDEO_CONSTRAINTS_DOUBLE,
                              talk_base::WrapMessageData(VideoConstraintsData<T>(videoConstraints, value, type)));
                break;
            case MIN_WIDTH: 
            case MAX_WIDTH: 
            case MIN_HEIGHT: 
            case MAX_HEIGHT: 
            case MIN_FRAMERATE: 
            case MAX_FRAMERATE: 
                worker_->post(TwilioSdkWorker::MSG_SET_VIDEO_CONSTRAINTS_INT,
                              talk_base::WrapMessageData(VideoConstraintsData<T>(videoConstraints, value, type)));
                break;
            default:
                LOG_ERROR("Invalid video constraint!");
                break;
        }
    } else {
        LOG_ERROR("SDK not initialized, cannot set video constraints!");
    }
}

template void TwilioSDK::setVideoConstraints<int>(TwilioSdkVideoConstraints videoConstraints, int value, TwilioSdkConstraintType type);
template void TwilioSDK::setVideoConstraints<double>(TwilioSdkVideoConstraints videoConstraints, double value, TwilioSdkConstraintType type);

void TwilioSDK::call(const std::string &user) {
    worker_->post(TwilioSdkWorker::MSG_START_CALL,
                  talk_base::WrapMessageData(user));
}

void TwilioSDK::answer(unsigned int callId) {
    worker_->post(TwilioSdkWorker::MSG_OFFER_ACCEPTED,
                  talk_base::WrapMessageData(callId));
}

void TwilioSDK::reject(unsigned int callId) {
    worker_->post(TwilioSdkWorker::MSG_REJECT,
                  talk_base::WrapMessageData(callId));
}

void TwilioSDK::terminate(unsigned int callId) {
    worker_->post(TwilioSdkWorker::MSG_TERMINATE,
                  talk_base::WrapMessageData(callId));
}

void TwilioSDK::setCaptureFeedbackInterval(int64_t timeInMsec) {
    this->worker_->post(TwilioSdkWorker::MSG_SET_CAPTURE_FEEDBACK_INTERVAL,
                        talk_base::WrapMessageData(timeInMsec));
}

bool TwilioSDK::initialized() const {
    return worker_->initialized();
}

} // namespace twiliosdk
