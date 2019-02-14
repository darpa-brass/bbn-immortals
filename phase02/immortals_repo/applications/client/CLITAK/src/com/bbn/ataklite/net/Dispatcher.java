package com.bbn.ataklite.net;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
import mil.darpa.immortals.core.analytics.Analytics;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Dispatcher implements Runnable, ConsumingPipe<byte[]>, ProducingPipe<byte[]> {

    private Selector selector;
    private SocketChannel socketChannel;
    private AtomicBoolean pendingWrite = new AtomicBoolean(false);
    private HashMap<Integer, DispatchHandler> handlers = new HashMap(3);
    private LinkedBlockingQueue<ByteBuffer> pendingData = new LinkedBlockingQueue<ByteBuffer>();
    private Thread clientThread = null;
    private AtomicBoolean stopServer = new AtomicBoolean(false);
    private String hostAddress;
    private int port;
    private SelectionKey selectionKey;
    private AtomicInteger serverState = new AtomicInteger(DISPATCHER_STOPPED);

    private ReadHandler readHandler;


    public static final int DISPATCHER_STOPPED = 0;
    public static final int DISPATCHER_STARTED = 1;

    public Dispatcher(InetAddress hostAddress, int port) {
        readHandler = new ReadHandler();

        handlers.put(SelectionKey.OP_CONNECT, new ServerConnectionHandler());
        handlers.put(SelectionKey.OP_READ, readHandler);
        handlers.put(SelectionKey.OP_WRITE, new WriteHandler());

        this.hostAddress = hostAddress.getHostAddress();
        this.port = port;
    }

    public void start() {
        if (!serverStarted()) {
            clientThread = new Thread(this);
            Analytics.registerThread(clientThread);

            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            clientThread.start();
        }
    }

    public void stop() {
        if (serverStarted()) {
            stopServer.set(true);
            if (selector != null && selector.isOpen()) {
                selector.wakeup();
            }
        }
    }

    public boolean serverStarted() {
        return (serverState.get() == DISPATCHER_STARTED);
    }

    @Override
    public void run() {

        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(hostAddress, port));

            selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

            while (!stopServer.get() && !Thread.interrupted()) {

                if (pendingWrite.get()) {
                    if (selectionKey.isValid()) {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    } else {
                        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                }

                selector.select();

                Iterator<SelectionKey> ski = selector.selectedKeys().iterator();

                while (ski.hasNext()) {
                    SelectionKey key = ski.next();
                    ski.remove();

                    if (key.isValid()) {
                        if (key.isConnectable()) {
                            handlers.get(SelectionKey.OP_CONNECT).handleEvent(new SelectionEvent(key, socketChannel, null));
                            serverState.set(DISPATCHER_STARTED);
                        } else if (key.isReadable()) {
                            handlers.get(SelectionKey.OP_READ).handleEvent(new SelectionEvent(key, socketChannel, null));
                        } else if (key.isWritable()) {
                            handlers.get(SelectionKey.OP_WRITE).handleEvent(new SelectionEvent(key, socketChannel, pendingData));
                            if ((key.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
                                pendingWrite.set(false);
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        try {
            socketChannel.close();
            selector.close();
        } catch (IOException e) {
            //Do nothing; releasing resources
        }

    }

    @Override
    public void consume(byte[] bytes) {
        pendingData.add(ByteBuffer.wrap(bytes));
        pendingWrite.set(true);

        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    @Override
    public void flushPipe() {
    }

    @Override
    public byte[] produce() {
        return readHandler.blockingReadFromQueue();
    }

    @Override
    public void closePipe() {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }
}
