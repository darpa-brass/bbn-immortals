package com.securboration.miniatakapp;


import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SocketChannelTesting {
    
    public static void main(String[] args) throws IOException {
        
        SocketChannel socketChannel;
        Selector selector;
        SelectionKey selectionKey;
        selector = Selector.open();
        socketChannel = new WrapperSocketChannel(SocketChannel.open());
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 4444));

        selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

        socketChannel.close();
    }
    
    
}
