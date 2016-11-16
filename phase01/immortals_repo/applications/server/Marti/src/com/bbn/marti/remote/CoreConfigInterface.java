package com.bbn.marti.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface CoreConfigInterface extends Remote {

    Map<String, Object> getAttributeMap() throws RemoteException;

    void setAttributeRemote(String settingName, Object value) throws RemoteException;

    Object getAttributeRemote(String settingName) throws RemoteException;

}

