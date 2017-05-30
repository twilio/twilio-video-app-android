package com.twilio.video.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.jsonwebtoken.impl.DefaultJwtBuilder;

/*
 * We had to extend DefaultJwtBuilder in order to
 * disable serialization exception in case where object class is empty.
 */
public class VideoJwtBuilder extends DefaultJwtBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public VideoJwtBuilder() {
        // this is the only difference from default jwt builder
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    protected byte[] toJson(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }
}
