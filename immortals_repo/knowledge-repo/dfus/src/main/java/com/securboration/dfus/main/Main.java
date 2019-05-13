package com.securboration.dfus.main;

import com.securboration.dfus.loader.resource.FileResourceLoader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String test = FileResourceLoader.readFileFromClassLoader(
                "outgoingXslt-processMdlSentToServer-com.securboration.client.MessageListenerClient");
        System.out.println(test);
    }
}
