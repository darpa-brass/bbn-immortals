package com.bbn.marti.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface DataFerryManagerInterface extends Remote {
    Collection<RemoteFile> getFileList() throws RemoteException;

    Collection<RemoteContact> getContactList() throws RemoteException;
}
