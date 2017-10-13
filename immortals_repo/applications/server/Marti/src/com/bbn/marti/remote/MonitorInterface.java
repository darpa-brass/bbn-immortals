package com.bbn.marti.remote;

import java.rmi.RemoteException;

public interface MonitorInterface extends CoreConfigInterface {

    void addCallback(MonitorCallbackinterface callback) throws RemoteException;
}
