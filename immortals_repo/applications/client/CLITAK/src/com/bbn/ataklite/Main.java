package com.bbn.ataklite;

public class Main {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        CLITAK clitak = new CLITAK();
        clitak.initialize();
        clitak.start();
    }

}
