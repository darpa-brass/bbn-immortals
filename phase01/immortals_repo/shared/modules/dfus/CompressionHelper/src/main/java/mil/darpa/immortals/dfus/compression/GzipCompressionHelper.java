package mil.darpa.immortals.dfus.compression;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;


/**
 * Base encryption DFU
 * <p>
 * Created by awellman@bbn.com on 5/18/16.
 */
public class GzipCompressionHelper {

    private final String compressionAlgorithm = "gzip";
    private final int bufferSize = 2048;

    final int getBufferSize() {
        return bufferSize;
    }

    public GzipCompressionHelper() {
    }

    private static GZIPInputStream getInputStream(String compressionAlgorithm, int bufferSize, InputStream sourceStream) throws IOException {
        return new GZIPInputStream(sourceStream, bufferSize);
    }

    private static GZIPOutputStream getOutputStream(String compressionAlgorithm, int bufferSize, OutputStream targetStream) throws IOException {
        return new GZIPOutputStream(targetStream, bufferSize, true);
    }

    public GZIPOutputStream attachCompressingOutputStream(OutputStream targetStream) throws IOException {
        return getOutputStream(compressionAlgorithm, bufferSize, targetStream);
    }

    public GZIPInputStream attachDecompressingInputStream(InputStream inputStream) throws IOException {
        return getInputStream(compressionAlgorithm, bufferSize, inputStream);
    }

    public byte[] compressAll(byte[] bytes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
            OutputStream cos = attachCompressingOutputStream(baos);
            cos.write(bytes);
            cos.flush();
            cos.close();
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] decompressAll(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            InputStream dos = attachDecompressingInputStream(bais);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);

            byte[] buffer = new byte[bufferSize];
            int read = 0;

            while ((read = dos.read(buffer)) > 0) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
