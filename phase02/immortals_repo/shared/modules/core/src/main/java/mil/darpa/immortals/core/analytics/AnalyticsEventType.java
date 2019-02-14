package mil.darpa.immortals.core.analytics;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public enum AnalyticsEventType {
    MyImageSent,
    FieldImageReceived,
    MyLocationProduced,
    FieldLocationUpdated,
    ClientStart,
    ClientShutdown,
    DfuMissmatchError,
    UnexepctedIgnorableError,
    Tooling_ValidationServerStarted,
    Tooling_ValidationServerStopped,
    Tooling_ValidationServerClientConnected,
    Tooling_ValidationServerClientDisconnected,
    Tooling_ValidationFinished,
    Analysis_EventOccurred
}
