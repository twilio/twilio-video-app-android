package com.tw.conv.testapp.util;

import java.util.HashSet;
import java.util.Set;

public class ParticipantParser {
    private static final String SEPARATOR = ",";

    public static Set<String> getParticipants(String commaDelimitedParticipants) {
        Set<String> participants = new HashSet<>();
        if(commaDelimitedParticipants != null && commaDelimitedParticipants.length() != 0) {
            String[] participantsArray = commaDelimitedParticipants.split(SEPARATOR);
            for (String participant : participantsArray) {
                participant = participant.trim();
                participants.add(participant);
            }
        }
        return participants;
    }

}
