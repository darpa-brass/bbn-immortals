package com.bbn.marti.remote;

import java.io.Serializable;

public class RemoteContact implements Serializable {
    public String contactName = null;
    public long lastHeardFromMillis = 0;
    public String endpoint = null;

    @Override
    public boolean equals(Object otherContact) {
        return otherContact != null && otherContact instanceof RemoteContact &&
                this.contactName.equals(
                        ((RemoteContact) otherContact).contactName);
    }

    @Override
    public int hashCode() {
        return 7612 + contactName.hashCode();
    }

    @Override
    public String toString() {
        return "<Contact " + contactName + " @ " + endpoint + ">";
    }
}
