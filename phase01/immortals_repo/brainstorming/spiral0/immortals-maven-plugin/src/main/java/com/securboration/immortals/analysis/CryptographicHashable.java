//package com.securboration.immortals.analysis;
//
//import java.io.UnsupportedEncodingException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//
//public interface CryptographicHashable extends BinaryContainer {
//    public default String getCryptographicHash()
//            throws UnsupportedEncodingException, NoSuchAlgorithmException {
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//        md.update(getBinaryForm());
//        return Base64.getEncoder().encodeToString(md.digest());
//    }
//
//}
