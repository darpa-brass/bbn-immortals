package mil.darpa.immortals.datagenerators;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datagenerators.javaclasstypes.AbstractDataGenerator;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 8/19/16.
 */
public class JpegFilepathGenerator extends AbstractDataGenerator<String> implements ConsumingPipe<Object> {

    private final List<File> fileList = new LinkedList<>();

    private final String imageDirectory;

    private int currentIdx = 0;

//    public JpegFilepathGenerator(String imageDirectory, int dataTransferBurstIntervalMS, int dataTransferBurstCount) {
//        super(dataTransferBurstIntervalMS, dataTransferBurstCount);
//        this.imageDirectory = imageDirectory;
//        loadFileList();
//    }

    public JpegFilepathGenerator(String imageDirectory, int dataTransferBurstIntervalMS, int dataTransferBurstCount, @Nonnull ConsumingPipe<String> next) {
        super(dataTransferBurstIntervalMS, dataTransferBurstCount, next);
        this.imageDirectory = imageDirectory;
        loadFileList();
    }

    private void loadFileList() {
        try {
            File f = new File(imageDirectory);

            if (f != null && f.exists() && f.isDirectory()) {
                File[] fileArray = new File(imageDirectory).listFiles();
                fileList.addAll(Arrays.asList(fileArray));
            } else {

                ClassLoader cl = JpegFilepathGenerator.class.getClassLoader();
                Enumeration<URL> urlResources = cl.getResources("r" + imageDirectory);

                while (urlResources.hasMoreElements()) {
                    fileList.add(new File(urlResources.nextElement().getPath()));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not load any images from 'sample_images'!");
        }
    }

    @Override
    protected String innerProduce() {
        String returnValue = fileList.get(currentIdx).getAbsolutePath();
        currentIdx = (currentIdx+1 == fileList.size() ? 0 : currentIdx+1);
        return returnValue;
    }
}
