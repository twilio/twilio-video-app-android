package com.twilio.video.app.data.api.model;

public enum Topology {
    P2P("P2P"),
    SFU("SFU"),
    SFU_RECORDING("SFU Recording");

    private final String topology;

    Topology(String topolgy) {
        this.topology = topolgy;
    }

    public String getString() {
        return topology;
    }

    public static Topology fromString(String topology) {
        if (topology.equals(P2P.topology)) {
            return P2P;
        } else if (topology.equals(SFU.topology)) {
            return SFU;
        } else if (topology.equals(SFU_RECORDING.topology)) {
            return SFU_RECORDING;
        } else {
            throw new RuntimeException("Unsupported topology string -> " + topology);
        }
    }
}
