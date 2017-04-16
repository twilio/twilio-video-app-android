package com.twilio.video.app.data.api.model;

public class VideoConfiguration {
    public final ConfigurationProfileSids configurationProfileSids;

    public VideoConfiguration(ConfigurationProfileSids configurationProfileSids) {
        this.configurationProfileSids = configurationProfileSids;
    }

    public String getSid(Topology topology) {
        switch (topology) {
            case P2P:
                return configurationProfileSids.p2p;
            case SFU:
                return configurationProfileSids.sfu;
            case SFU_RECORDING:
                return configurationProfileSids.sfuRecording;
            default:
                throw new RuntimeException("Could not get sid for topology -> " + topology);
        }
    }
}
