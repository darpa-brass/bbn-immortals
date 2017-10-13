package mil.darpa.immortals.modulerunner.generators.sources.raw;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class FileSource {

    private final InputStream dataSourceInputStream;
    private final int generationUnitByteCount;
    private final byte[] readByteBuffer;

    public FileSource(@Nonnull String sourceFile, int byteSize) throws FileNotFoundException {
        generationUnitByteCount = byteSize;

        InputStream inputStream = FileSource.class.getClassLoader().getResourceAsStream(sourceFile);
        if (inputStream == null) {
            inputStream = new FileInputStream(new File(sourceFile));
        }

        dataSourceInputStream = inputStream;
        readByteBuffer = new byte[generationUnitByteCount];
    }

    public InputStream getInputStream() {
        return dataSourceInputStream;
    }

    public byte[] getBytes() throws IOException {
        int bytesRead = dataSourceInputStream.read(readByteBuffer);

        if (bytesRead <= 0) {
            throw new RuntimeException("The data source has run out of data to generate!");
        }
        return readByteBuffer;
    }
}
