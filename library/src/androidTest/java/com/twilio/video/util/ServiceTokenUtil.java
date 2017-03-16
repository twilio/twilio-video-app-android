package com.twilio.video.util;

import com.twilio.video.IceServer;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.twilioapi.TwilioApiUtils;
import com.twilio.video.twilioapi.model.TwilioServiceToken;
import com.twilio.video.twilioapi.model.TwilioIceServer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceTokenUtil {
    private static TwilioServiceToken twilioServiceToken = null;

    public static Set<IceServer> getIceServers() {
        Map<String, String> credentials = CredentialsUtils.resolveCredentials(
                Environment.fromString(BuildConfig.ENVIRONMENT),
                /*
                 * We just pass P2P for now, but we don't actually care about topology since
                 * we are only interested in ACCOUNT_SID and AUTH_TOKEN to use Twilio API.
                 */
                Topology.P2P);
        if (isExpired(twilioServiceToken)) {
            twilioServiceToken = TwilioApiUtils
                    .getServiceToken(credentials.get(CredentialsUtils.ACCOUNT_SID),
                            credentials.get(CredentialsUtils.AUTH_TOKEN),
                            BuildConfig.ENVIRONMENT);
        }
        return convertToIceServersSet(twilioServiceToken.getIceServers());
    }

    private static boolean isExpired(TwilioServiceToken twilioServiceToken) {
        if (twilioServiceToken == null) {
            return true;
        }
        long ttl = Long.parseLong(twilioServiceToken.getTtl());

        // RFC-2822 For example: "Tue, 07 Feb 2017 22:42:03 +0000"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        try {
            Date createDate = simpleDateFormat.parse(twilioServiceToken.getDateCreated());
            Date currentDate = new Date();
            long secondsPassed = (currentDate.getTime() - createDate.getTime()) / 1000;
            if (secondsPassed >= ttl) {
                return true;
            }
        } catch (ParseException e) {
            return true;
        }
        return false;
    }

    private static Set<IceServer> convertToIceServersSet(
        List<TwilioIceServer> twilioIceServers) {
        Set<IceServer> result = new HashSet<>();
        if (twilioIceServers != null && twilioIceServers.size() > 0) {
            for (TwilioIceServer twilioIceServer : twilioIceServers) {
                IceServer iceServer = new IceServer(
                    twilioIceServer.getUrl(),
                    twilioIceServer.getUsername(),
                    twilioIceServer.getCredential());
                result.add(iceServer);
            }
        }
        return result;
    }
}
