package com.bbn.immortals.platform;

import java.util.Random;

/**
 * Created by awellman@bbn.com on 1/27/16.
 */
public class Utils {


    private static final Random random = new Random();

    private static final char[] charArray = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String generateRandomAlphaString(int length) {
        char[] charbuf = new char[length];

        for(int i = 0; i < charbuf.length; i++) {
            charbuf[i] = charArray[random.nextInt(charArray.length)];
        }
        return new String(charbuf);
    }
}
