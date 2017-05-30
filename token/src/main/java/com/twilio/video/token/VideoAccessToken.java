package com.twilio.video.token;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/*
 * Represents a video access token. Loosely based on implementation AccessToken in Twilio Java
 * Helper library, but simplified for video use case.
 */
public class VideoAccessToken {
    private static final String CTY = "twilio-fpa;v=1";
    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.HS256;
    private static final Map<String, Object> headers = new HashMap<String, Object>() {{
        put("cty", CTY);
        put("typ", "JWT");
    }};

    private final String id;
    private final String accountSid;
    private final String apiKey;
    private final String apiKeySecret;
    private final SecretKeySpec keySpec;
    private final String configurationProfileSid;
    private final Date expiration;
    private final String identity;
    private final Date nbf;
    private final String jwt;

    private VideoAccessToken(Builder builder) {
        Date now = new Date();
        this.id = builder.apiKey + "-" + (int)(Math.floor(now.getTime() / 1000.0f));
        this.accountSid = builder.accountSid;
        this.identity = builder.identity;
        this.apiKey = builder.apiKey;
        this.apiKeySecret = builder.apiSecret;
        this.keySpec = new SecretKeySpec(apiKeySecret.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());
        this.configurationProfileSid = builder.configurationProfileSid;
        this.expiration = new Date(new Date().getTime() + builder.ttl * 1000);
        this.nbf = builder.nbf;
        this.jwt = buildJwt();
    }

    public Date getNbf() {
        return this.nbf;
    }

    public String getId() {
        return this.id;
    }

    public String getSubject() {
        return this.accountSid;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> grants = new HashMap<>();
        Map<String, Object> payload = new HashMap<>();

        // Setup grants payload
        grants.put("identity", identity);
        if (configurationProfileSid != null) {
            grants.put("rtc", new Payload(configurationProfileSid));
        } else {
            grants.put("video", new Video());
        }
        payload.put("grants", grants);

        return payload;
    }

    @Override
    public String toString() {
        return Jwts.parser()
                .setSigningKey(keySpec)
                .parseClaimsJws(jwt).toString();
    }

    public String getJwt() {
        return jwt;
    }

    private String buildJwt() {
        // Initialize jwt builder
        JwtBuilder builder = new VideoJwtBuilder()
                .signWith(ALGORITHM, keySpec)
                .setHeaderParams(headers)
                .setIssuer(apiKey)
                .setExpiration(expiration);
        builder.setId(id);
        builder.setSubject(accountSid);
        if (nbf != null) {
            builder.setNotBefore(nbf);
        }

        // Add claims
        for (Map.Entry<String, Object> entry : getClaims().entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }

        return builder.compact();
    }

    public static class Builder {
        private static final int TTL_DEFAULT = 3600;
        private static final int DEFAULT_IDENTITY_LENGTH = 10;

        private String accountSid;
        private String apiKey;
        private String apiSecret;
        private String configurationProfileSid;
        private String identity = RandUtils.generateRandomString(DEFAULT_IDENTITY_LENGTH);
        private Date nbf = null;
        private int ttl = TTL_DEFAULT;

        public Builder(String accountSid,
                       String apiKeySid,
                       String apiKeySecret) {
            this.accountSid = accountSid;
            this.apiKey = apiKeySid;
            this.apiSecret = apiKeySecret;
        }

        public Builder identity(String identity) {
            this.identity = identity;
            return this;
        }

        public Builder ttl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder nbf(Date nbf) {
            this.nbf = nbf;
            return this;
        }

        public Builder configurationProfileSid(String configurationProfileSid) {
            this.configurationProfileSid = configurationProfileSid;
            return this;
        }

        public VideoAccessToken build() {
            return new VideoAccessToken(this);
        }
    }

    private static class Payload {
        public final String configuration_profile_sid;

        Payload(String configurationProfileSid) {
            this.configuration_profile_sid = configurationProfileSid;
        }
    }

    private static class Video {
        Video() {}
    }
}
