package com.bbn.marti.service;

import com.bbn.marti.remote.BadgeOfShame;
import com.bbn.marti.remote.MonitorCallbackinterface;
import com.bbn.marti.remote.MonitorInterface;

import java.rmi.RemoteException;
import java.util.LinkedList;

public class CoreMonitor extends CoreConfig implements MonitorInterface {

    private static final long serialVersionUID = -3256998844640858325L;

    static CoreMonitor instance = null;

    protected CoreMonitor() throws RemoteException {
        setAttribute(BadgeOfShame.SHAMEWALL_KEY, new LinkedList<BadgeOfShame>());
    }

    public static CoreMonitor getInstance() {
        if (instance == null) {
            try {
                instance = new CoreMonitor();
            } catch (RemoteException e) {
                // idk how this could possibly happen...
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public void addCallback(MonitorCallbackinterface callback)
            throws RemoteException {
        // TODO Auto-generated method stub

    }

}
