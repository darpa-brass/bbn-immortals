package com.securboration.miniatakapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class SecurborationSocketChannel extends SocketChannel {

    SocketChannel socketChannel;
    OutputStream cipherOutputStream;

    public SecurborationSocketChannel(SocketChannel _socketChannel) {
        super(null);
        socketChannel = _socketChannel;
        
        try {
            CipherImpl c = new CipherImpl(
                    "AES",
                    16,
                    "CBC",
                    "PKCS5Padding",
                    "a test password",
                    "an init vector");
            cipherOutputStream =  c.acquire(socketChannel.socket().getOutputStream());
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected SecurborationSocketChannel(SelectorProvider selectorProvider) {
        super(selectorProvider);
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        implCloseSelectableChannel();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        implConfigureBlocking(block);
    }

    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        return null;
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        return null;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return null;
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return null;
    }

    @Override
    public SocketChannel shutdownInput() throws IOException {
        return null;
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException {
        return null;
    }

    @Override
    public Socket socket() {
        return socketChannel.socket();
    }

    @Override
    public boolean isConnected() {
        return socketChannel.isConnected();
    }

    @Override
    public boolean isConnectionPending() {
        return socketChannel.isConnectionPending();
    }

    @Override
    public boolean connect(SocketAddress socketAddress) throws IOException {
        return socketChannel.connect(socketAddress);
    }

    @Override
    public boolean finishConnect() throws IOException {
        return socketChannel.finishConnect();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return null;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        return socketChannel.read(byteBuffer);
    }

    @Override
    public long read(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return socketChannel.read(byteBuffers, i, i1);
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        cipherOutputStream.write(byteBuffer.array());
        return byteBuffer.array().length;
    }

    @Override
    public long write(ByteBuffer[] byteBuffers, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return null;
    }

}
