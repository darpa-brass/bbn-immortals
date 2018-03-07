package com.securboration.miniatakapp;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.securboration.immortals.ontology.functionality.alg.encryption.*;
import com.securboration.immortals.ontology.functionality.compression.CompressionAlgorithm;
import com.securboration.immortals.ontology.functionality.imagecapture.AspectWriteImage;
import com.securboration.immortals.ontology.functionality.imagescaling.EnlargeImage;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.imagecapture.FileHandle;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import com.securboration.immortals.ontology.functionality.compression.AspectDeflate;
import com.securboration.immortals.ontology.functionality.compression.AspectInflate;
import com.securboration.immortals.ontology.functionality.compression.Compressor;
import com.securboration.immortals.ontology.functionality.imagecapture.AspectReadImage;
import com.securboration.immortals.ontology.functionality.imagecapture.ImageFileIO;
import com.securboration.immortals.ontology.functionality.locationprovider.GetLastKnownLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Location;

public class MonolithicAtakApplication {

    public MonolithicAtakApplication(){
        //wire up the application
        capturer = new Dfus.ImageCapturer();
        locationGetter = new Dfus.LocationGetter();
        sad = new BusinessLogic.SaDataBuilder(capturer,locationGetter);
        client = new BusinessLogic.MartiClient("localhost:8888");
    }

    private final Dfus.ImageCapturer capturer;
    private final Dfus.LocationGetter locationGetter;
    private final BusinessLogic.SaDataBuilder sad;
    private final BusinessLogic.MartiClient client;


    public void run() throws IOException{
        BusinessLogic.MockSaData data = sad.captureSitrep();

        BusinessLogic.MockSaMessage message = BusinessLogic.MockSaMessage.create(data);

        int response = client.write(message);

        System.out.printf("server returns HTTP %d\n", response);

//        if(false)
        {
            byte[] randomData = new byte[1024*1024];
            new Random().nextBytes(randomData);

            ByteArrayOutputStream tarData = new ByteArrayOutputStream();
            TarArchiveOutputStream os = new TarArchiveOutputStream(tarData);
            TarArchiveEntry entry = new TarArchiveEntry("test");
            entry.setSize(randomData.length);
            os.putArchiveEntry(entry);
            os.write(randomData);
            os.closeArchiveEntry();
            os.close();

            System.out.printf(
                    "orig = %dB, tar = %dB",
                    randomData.length,
                    tarData.toByteArray().length
            );
        }
    }


    private static class Dfus{

        @DfuAnnotation(
                functionalityBeingPerformed = HashFunction.class
        )
        private static class HashUtils{

            @FunctionalAspectAnnotation(
                    aspect=AspectHash.class
            )
            private static String hash(
                    @BinaryData
                            byte[] data
            ){
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                    md.update(data);

                    return Base64.getEncoder().encodeToString(md.digest());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @DfuAnnotation(
                functionalityBeingPerformed = ImageFileIO.class
        )
        public static class ImageCapturer{

            @FunctionalAspectAnnotation(
                    aspect=AspectReadImage.class
            )
            @Image
            public byte[] snap(){
                byte[] randomBytes = new byte[16*16];
                new Random().nextBytes(randomBytes);

                System.out.printf(
                        "captured an image with %dB\n",randomBytes.length
                );

                return randomBytes;
            }

        }

        @DfuAnnotation(
                functionalityBeingPerformed = LocationProvider.class
        )
        public static class LocationGetter{

            @FunctionalAspectAnnotation(
                    aspect=GetLastKnownLocationAspect.class
            )
            @Location
            public String getCurrentLocation(){

                Random rng = new Random();

                double lat =  -90d + 180*rng.nextDouble();
                double lon = -180d + 360*rng.nextDouble();

                String location = String.format("{\"lat\":%1.4f,\"lon\":%1.4f}", lat, lon);

                System.out.println("captured location " + location);

                return location;
            }

        }
        
        
        
        /*
         * These are add-ons not initially present in the application that would
         * improve its performance
         * 
         * Compression reduces the amount of network traffic
         * Cipher increases the privacy of the data transmitted
         * 
         */

        @DfuAnnotation(
                functionalityBeingPerformed = Compressor.class
        )
        public static class GzipCompressor{

            @FunctionalAspectAnnotation(
                    aspect=AspectDeflate.class
            )
            public @BinaryData byte[] compress(
                    @BinaryData
                            byte[] data
            ) throws IOException{
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                GZIPOutputStream s = new GZIPOutputStream(output);

                IOUtils.copy(new ByteArrayInputStream(data), s);

                return output.toByteArray();
            }


            @FunctionalAspectAnnotation(
                    aspect=AspectInflate.class
            )
            public @BinaryData byte[] decompress(
                    @BinaryData
                            byte[] data
            ) throws IOException{
                GZIPInputStream s = new GZIPInputStream(
                        new ByteArrayInputStream(data)
                );

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOUtils.copy(s, out);

                return out.toByteArray();
            }

        }

        @DfuAnnotation(
                functionalityBeingPerformed = Cipher.class
        )
        public static class SimpleAesCipher{

            private final byte pad = 0x55;
            
            @FunctionalAspectAnnotation(
                    aspect = AspectCipherEncryptStream.class
            )
            @BinaryData
            public byte[] encryptStream(@BinaryData byte[] message, @mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.OutputStream OutputStream outputStream) {
                return null;
            }
            
            @FunctionalAspectAnnotation(
                    aspect = AspectCipherDecryptStream.class
            )
            public byte[] decryptStream(@BinaryData byte[] message, @mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.InputStream InputStream inputStream) {
                return null;
            }
        }

    }

    /**
     * Application-specific abstractions and functionality needed to implement
     * our application
     *
     * @author jstaples
     *
     */
    private static class BusinessLogic{

        public static class MockSaData{

            private byte[] image;
            private String loc;

            @Override
            public String toString(){
                return String.format(
                        "{\n  \"image\":\"%s\",\n  \"loc\":%s,\n  \"checksum\":\"%s\"\n}",
                        Base64.getEncoder().encodeToString(image),
                        loc,
                        checksum()
                );
            }

            public String checksum(){
                final byte[] loc = this.loc.getBytes();
                byte[] data = new byte[image.length + loc.length];

                System.arraycopy(image, 0, data, 0, image.length);
                System.arraycopy(loc, 0, data, image.length, loc.length);

                return Dfus.HashUtils.hash(data);
            }
        }

        public static class MockSaMessage{
            private String json;

            public static MockSaMessage create(MockSaData data){
                StringBuilder sb = new StringBuilder();

                sb.append(data.toString());

                MockSaMessage message = new MockSaMessage();
                message.json = sb.toString();
                return message;
            }
        }

        public static class SaDataBuilder{

            private final Dfus.ImageCapturer imageCapture;
            private final Dfus.LocationGetter locationGetter;

            public SaDataBuilder(
                    Dfus.ImageCapturer imageCapture,
                    Dfus.LocationGetter locationGetter
            ) {
                super();
                this.imageCapture = imageCapture;
                this.locationGetter = locationGetter;
            }

            public MockSaData captureSitrep(){

                MockSaData data = new MockSaData();
                data.image = imageCapture.snap();
                data.loc = locationGetter.getCurrentLocation();

                System.out.println("SA Data: " + data);

                return data;
            }
        }

        public static class MartiClient{

            private final String endpoint;

            public MartiClient(final String endpoint){
                this.endpoint = endpoint;
            }

            public int write(MockSaMessage message){
                return PlatformInterface.MockNetworkApi.transmitHttp(
                        "POST",
                        endpoint,
                        message.json
                );
            }

        }

    }




    public static class PlatformInterface {

        public static class MockNetworkApi {

            public static int transmitHttp(
                    String verb,
                    String endpoint,
                    String body
            ){

                System.out.printf(
                        "HTTP %s %s\nwith body:\n%s\n",
                        verb,
                        endpoint,
                        ">  " + body.replace("\n", "\n>  ")
                );

                return 200;
            }

        }

    }



}
