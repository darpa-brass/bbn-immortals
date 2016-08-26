package mil.darpa.immortals.dfus.compression;

import mil.darpa.immortals.core.synthesis.adapters.InputStreamToPipe;
import mil.darpa.immortals.core.synthesis.adapters.OutputStreamToPipe;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by awellman@bbn.com on 6/15/16.
 */
public class CompressionHelperTest {

    private static final String originalString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private static final String[] newStringArray = {originalString, "\n", "\n", "This i", "s Lati", "n.", " I don", "'t know La", "tin.", ".\n\n"};

    private static final String SCRATCH_FILE = "scratchFile.dat";

    @Test
    public void GzipBlockTest() {
        GzipCompressionHelper gzipCompressionHelper = new GzipCompressionHelper();

        byte[] compressedBytes = gzipCompressionHelper.compressAll(originalString.getBytes());
        byte[] decompressedBytes = gzipCompressionHelper.decompressAll(compressedBytes);

        System.out.println("OriginalString: '" + originalString + "'");
        System.out.println("DecompressedString: '" + new String(decompressedBytes) + "'");
        System.out.println("OriginalBytesLength: '" + originalString.getBytes().length + "'");
        System.out.println("CompressedBytesLength: '" + compressedBytes.length + "'");
        System.out.println("DecompressedBytesLength: '" + decompressedBytes.length + "'");

        Assert.assertTrue(compressedBytes.length < decompressedBytes.length);
    }

    @Test
    public void GzipStreamTest() {
        try {
            String originalString = "";
            long originalStringSize = 0;
            long compressedStringSize;
            String decompressedString;


            // Assemble and compress the original String
            GzipCompressor compressor = new GzipCompressor(
                            new FileOutputStream(new File(SCRATCH_FILE))
            );

            for (String str : newStringArray) {
                originalString += str;
                originalStringSize += str.getBytes().length;
                compressor.consume(str.getBytes(Charset.defaultCharset()));
            }
            compressor.flush();
            compressor.close();

            // Get the size of the compressed bytes
            compressedStringSize = new File(SCRATCH_FILE).length();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GzipDecompressor decompressor = new GzipDecompressor(
                            new FileInputStream(new File(SCRATCH_FILE))
            );

            byte[] produced;
            while ((produced = decompressor.produce()) != null) {
                baos.write(produced);
            }
            decompressor.close();

            decompressedString = new String(baos.toByteArray());

            System.out.println("Original String:\n" + originalString);
            System.out.println("Produced String:\n" + new String(baos.toByteArray()));
            System.out.println("Original Data Size:\n" + originalStringSize);
            System.out.println("Compressed Data Size:\n" + compressedStringSize);
            Assert.assertTrue(originalString.equals(decompressedString));
            Assert.assertTrue(originalStringSize > compressedStringSize);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
