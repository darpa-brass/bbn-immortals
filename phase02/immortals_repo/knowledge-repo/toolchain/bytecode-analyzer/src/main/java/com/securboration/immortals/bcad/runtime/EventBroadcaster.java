package com.securboration.immortals.bcad.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Routing point for incoming event notifications
 * 
 * @author jstaples
 *
 */
public class EventBroadcaster {

    /**
     * A chain of listeners to which events are transmitted in a filter chain
     */
    private static final List<IEventListener> listeners = new ArrayList<>();

    /**
     * 
     * @return a reference to the currently used listener pipeline.
     */
    public static List<IEventListener> getListenersModifiable() {
        return listeners;
    }

    public static void postEntry(final String methodHash) {
        for (IEventListener listener : listeners) {
            boolean shouldPropagate = listener.postEntry(methodHash);

            if (!shouldPropagate) {
                return;
            }
        }
    }

    public static void preReturn(final String methodHash) {
        for (IEventListener listener : listeners) {
            boolean shouldPropagate = listener.preReturn(methodHash);

            if (!shouldPropagate) {
                return;
            }
        }
    }

    public static void postCatch(final String methodHash, final Throwable t) {
        for (IEventListener listener : listeners) {
            boolean shouldPropagate = listener.postCatch(methodHash, t);

            if (!shouldPropagate) {
                return;
            }
        }
    }

    public static void uncaught(final String methodHash, final Throwable t) {
        for (IEventListener listener : listeners) {
            boolean shouldPropagate = listener.uncaught(methodHash, t);

            if (!shouldPropagate) {
                return;
            }
        }
    }

    public static void postControlFlowPathTaken(String pathId) {
        for (IEventListener listener : listeners) {
            boolean shouldPropagate = listener.postControlFlowPathTaken(pathId);

            if (!shouldPropagate) {
                return;
            }
        }
    }

    public static void reset() {
        listeners.forEach(l -> l.reset());
    }

}
