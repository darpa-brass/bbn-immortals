package com.bbn.ataklite.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class ReadHandler implements DispatchHandler {

    private final LinkedBlockingQueue<byte[]> outgoingQueue = new LinkedBlockingQueue<>();

    public ReadHandler() {

    }

    @Override
    public void handleEvent(SelectionEvent selectionEvent) {

        int bytesRead = 0;
        boolean remoteEndpointTerminated = false;
        readBuffer.clear();

        try {
            bytesRead = selectionEvent.getSocketChannel().read(readBuffer);

            if (bytesRead == -1) {
                remoteEndpointTerminated = true;
            } else {
                ByteBuffer copy = ByteBuffer.allocate(bytesRead);
                copy.clear();
                readBuffer.flip();

                copy.put(readBuffer);
                outgoingQueue.offer(copy.array());
            }
        } catch (IOException e) {
            remoteEndpointTerminated = true;
        }

        if (remoteEndpointTerminated) {
            selectionEvent.getSelectionKey().cancel();
            try {
                selectionEvent.getSocketChannel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static final int READ_BUFFER_CAPACITY = 512 * 1024;
    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(READ_BUFFER_CAPACITY);

    public byte[] blockingReadFromQueue() {
        try {
            return outgoingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
