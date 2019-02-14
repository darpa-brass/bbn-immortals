package com.bbn.marti.dataservices;

import com.google.gson.Gson;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.zeromq.ZMQ;

/**
 * Created by awellman@bbn.com on 6/11/18.
 */
public class ValidationBroker {

    private static final Gson gson = new Gson();

    private ZMQ.Context context;
    private ZMQ.Socket responder;
    private Thread activityTread;
    private final AnalyticsEndpointInterface endpointInterface;

    public ValidationBroker(AnalyticsEndpointInterface endpointInterface) {
        this.endpointInterface = endpointInterface;
    }

    public synchronized void start() {
        context = ZMQ.context(1);
        String addr = "tcp://*:53265";
        responder = context.socket(ZMQ.REP);
        responder.bind(addr);

        activityTread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    byte[] in = responder.recv(0);
                    String req = new String(in);
                    System.out.println("IMMORTALS-RECEIVING: " + req);
                    AnalyticsEvent event = gson.fromJson(req, AnalyticsEvent.class);
                    endpointInterface.log(event);
                    responder.send("", 0);
                }
                shutdown();
            }
        });
        activityTread.setDaemon(true);
        activityTread.start();
    }

    public synchronized void shutdown() {
        if (responder != null) {
            responder.close();
            responder = null;
        }
        if (context != null) {
            context.term();
            context = null;
        }
    }
    
    public static void main(String[] args) {
        ValidationBroker vb = new ValidationBroker(new AnalyticsEndpointInterface() {
            @Override
            public void start() {
                System.out.println("START");
            }

            @Override
            public void log(AnalyticsEvent event) {
                System.out.println(gson.toJson(event));
            }

            @Override
            public void shutdown() {
                System.out.println("SHUTDOWN");

            }
        });
        
        vb.start();
        System.out.println("MEH");
    }
}
