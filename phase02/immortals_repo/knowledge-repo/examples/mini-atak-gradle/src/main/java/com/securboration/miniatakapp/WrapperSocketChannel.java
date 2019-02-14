package com.securboration.miniatakapp;

import com.securboration.miniatakapp.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.channels.spi.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class WrapperSocketChannel extends SocketChannel {

    private SocketChannel socketchannel;

    private CipherImpl cipherimpl;
    
    @Override
    public SocketChannel bind(final SocketAddress socketAddress) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).bind(socketAddress);
    }

    @Override
    public SocketChannel setOption(final SocketOption socketOption, final Object o) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).setOption(socketOption, o);
    }

    @Override
    public SocketChannel shutdownInput() throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).shutdownInput();
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).shutdownOutput();
    }

    @Override
    public Socket socket() {
        return ((WrapperSocketChannel) this.socketchannel).socket();
    }

    @Override
    public boolean isConnected() {
        return ((WrapperSocketChannel) this.socketchannel).isConnected();
    }

    @Override
    public boolean isConnectionPending() {
        return ((WrapperSocketChannel) this.socketchannel).isConnectionPending();
    }

    @Override
    public boolean connect(final SocketAddress socketAddress) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).connect(socketAddress);
    }

    @Override
    public boolean finishConnect() throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).finishConnect();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).getRemoteAddress();
    }

    @Override
    public int read(final ByteBuffer byteBuffer) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).read(byteBuffer);
    }

    @Override
    public long read(final ByteBuffer[] array, final int n, final int n2) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).read(array, n, n2);
    }

    @Override
    public int write(final ByteBuffer byteBuffer) throws IOException {
        final ByteBuffer writeThis;
        try {
            writeThis = ByteBuffer.wrap(this.cipherimpl.encryptChunk(byteBuffer.array()));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        double diff;
        if (byteBuffer.capacity() != writeThis.capacity()) {
            diff = (double) byteBuffer.capacity() / (double) writeThis.capacity();
        } else {
            diff = 1.0;
        }
        int bytesWritten = (this.socketchannel.write(writeThis));
        double compensationForSizeDiff = bytesWritten * diff;
        byteBuffer.position(byteBuffer.position() + (int) compensationForSizeDiff);
        return (int) compensationForSizeDiff;
    }

    @Override
    public long write(final ByteBuffer[] array, final int n, final int n2) throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).write(array, n, n2);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return ((WrapperSocketChannel) this.socketchannel).getLocalAddress();
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return null;
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return null;
    }

    public static CipherImpl initCipherImpl() throws Exception {
        CipherImpl c = new CipherImpl("AES", 16, "CBC", "PKCS5Padding", "a test password", "an init vector");
        return c;
    }

    public WrapperSocketChannel(final SocketChannel socketchannel) {
        super(socketchannel.provider());
        this.socketchannel = socketchannel;
        try {
            this.cipherimpl = initCipherImpl();
        } catch (Exception ex) {
        }
    }

    public java.nio.channels.SelectableChannel configureBlockingWrapped(boolean booleanParam) throws Exception {
        return (this.socketchannel).configureBlocking(booleanParam);
    }

    public SelectionKey registerWrapped(Selector selectorParam, int intParam) throws ClosedChannelException {
        return (this.socketchannel).register(selectorParam, intParam);
    }

    public void implCloseSelectableChannel() throws IOException {
        throw new RuntimeException("Hopefully still works");
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {

    }

    private java.io.OutputStream outputstream;
}



