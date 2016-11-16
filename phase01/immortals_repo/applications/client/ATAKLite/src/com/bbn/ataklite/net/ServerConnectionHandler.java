package com.bbn.ataklite.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class ServerConnectionHandler implements DispatchHandler {

    public ServerConnectionHandler() {
    }

    @Override
    public void handleEvent(SelectionEvent selectionEvent) {

        try {
            selectionEvent.getSocketChannel().finishConnect();
            selectionEvent.getSelectionKey().interestOps(selectionEvent.getSelectionKey().interestOps() ^ SelectionKey.OP_CONNECT);
        } catch (IOException exception) {
            selectionEvent.getSelectionKey().cancel();
            exception.printStackTrace();
        }
    }
}
