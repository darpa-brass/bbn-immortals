package com.bbn.marti.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface SubscriptionManagerInterface extends Remote {
    ArrayList<RemoteSubscription> getSubscriptionList() throws RemoteException;

    boolean deleteSubscription(String subscriptionName) throws RemoteException;

    // QoS management for subscriptions
//    ArrayList<String> getQosModeList() throws RemoteException;
//    void setBandwidthForUid(String uid, int bandwidth) throws RemoteException;
//    void setQosModeForUid(String uid, String mode) throws RemoteException;
//    void setImgModeForUid(String uid, String mode) throws RemoteException;

    String getXpathForUid(String uid) throws RemoteException;

    void setXpathForUid(String uid, String xpath) throws RemoteException;
}
