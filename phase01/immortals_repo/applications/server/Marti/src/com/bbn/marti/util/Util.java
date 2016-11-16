package com.bbn.marti.util;

import org.apache.log4j.Logger;

import java.util.UUID;

public class Util {

    private static Logger log = Logger.getLogger(Util.class.getSimpleName());

    public static final String generateUid() {
        return UUID.randomUUID().toString();
    }

    public static final void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Thread sleep interrupted", e);
        }
    }

    public static final void sleepSecs(int seconds) {
        sleepMillis(seconds * 1000);
    }

    /**
     * Covers protocol:addr:port (option 1)
     * addr:port:protocol (option 2)
     */
    public static final CotEndpoint parseCotEndpoint(String endpoint) {
        CotEndpoint ret = new CotEndpoint();

        String[] tokens = endpoint.split(":");

        if (tokens.length < 3) {
            log.error("Malformed subscription endpoint: " + endpoint);
            return null;
        }

        try {
            // Try option #1
            ret.port = Integer.parseInt(tokens[2]);
            ret.address = tokens[1];
            ret.protocol = tokens[0];
        } catch (NumberFormatException e) {
            try {
                // Try option #2
                ret.port = Integer.parseInt(tokens[1]);
                ret.address = tokens[0];
                ret.protocol = tokens[2];
            } catch (NumberFormatException nfe) {
                // Both failed!
                return null;
            }
        }

        return ret;
    }

    public static class CotEndpoint {
        public String address = null;
        public int port = -1;
        public String protocol = null;
    }

}
