package com.bbn.ataklite.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

public class WriteHandler implements DispatchHandler {

    public WriteHandler() {
    }

    @Override
    public void handleEvent(SelectionEvent selectionEvent) {

        @SuppressWarnings("unchecked")
        LinkedBlockingQueue<ByteBuffer> queue = (LinkedBlockingQueue<ByteBuffer>) selectionEvent.getData();

        ByteBuffer currentWriteBuffer = null;

        if (selectionEvent.getSelectionKey().attachment() != null) {
            currentWriteBuffer = (ByteBuffer) selectionEvent.getSelectionKey().attachment();
        } else {
            currentWriteBuffer = queue.poll();
        }

        while (currentWriteBuffer != null && currentWriteBuffer.hasRemaining()) {
            try {
                //Much more needed here for encoding, but this works for now
                selectionEvent.getSocketChannel().write(currentWriteBuffer);
                if (currentWriteBuffer.remaining() > 0) {
                    selectionEvent.getSelectionKey().attach(currentWriteBuffer);
                    break;
                } else {
                    selectionEvent.getSelectionKey().attach(null);
                    currentWriteBuffer = queue.poll();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (currentWriteBuffer == null) {
            selectionEvent.getSelectionKey().interestOps(selectionEvent.getSelectionKey().interestOps() ^ SelectionKey.OP_WRITE);
        }
    }

}
