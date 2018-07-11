//package com.bbn.cot;
//
//import com.bbn.marti.immortals.net.Protocol;
//import com.bbn.marti.immortals.net.tcp.Transport;
//import com.bbn.marti.net.ProtocolListener;
//import org.dom4j.io.OutputFormat;
//import org.dom4j.io.XMLWriter;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//
//public class CotPrintingProtocolListener implements ProtocolListener<CotEventContainer> {
//    private OutputFormat format = OutputFormat.createPrettyPrint();
//    private XMLWriter writer;
//    private OutputStream out;
//
//    public CotPrintingProtocolListener(OutputStream out) throws UnsupportedEncodingException {
//        this.writer = new XMLWriter(out, format);
//        this.out = out;
//    }
//
//    @Override
//    public void onDataReceived(CotEventContainer data,
//                               final Transport transport, final Protocol<CotEventContainer> protocol) {
//        try {
//            out.write(("From: " + transport + "\n").getBytes());
//            writer.write(data.getDocument());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
