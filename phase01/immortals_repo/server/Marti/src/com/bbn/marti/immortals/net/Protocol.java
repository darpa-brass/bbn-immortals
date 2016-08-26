package com.bbn.marti.immortals.net;

import com.bbn.marti.immortals.data.CotData;

import java.io.IOException;

/**
 * A typed decorator interface that, once implemented, provides the encoding of the data type T to the transport.
 *
 * @param <T>
 * @author kusbeck
 */
public interface Protocol<T> {

    /**
     * Writes data to transport
     *
     * @param data
     * @param transport
     * @return number of bytes written to the "wire"
     * @throws IOException
     */
    void sendingCotData(CotData data) throws IOException;
}
