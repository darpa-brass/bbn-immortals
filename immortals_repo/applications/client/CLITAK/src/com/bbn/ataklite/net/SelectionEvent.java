package com.bbn.ataklite.net;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SelectionEvent {

    public SelectionEvent(SelectionKey selectionKey, SocketChannel socketChannel, Object data) {
        this.selectionKey = selectionKey;
        this.socketChannel = socketChannel;
        this.data = data;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private SelectionKey selectionKey;
    private SocketChannel socketChannel;
    private Object data;
}
