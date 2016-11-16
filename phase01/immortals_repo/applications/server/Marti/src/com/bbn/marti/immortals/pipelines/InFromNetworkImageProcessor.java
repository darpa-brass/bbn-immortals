package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 2/29/16.
 */

public class InFromNetworkImageProcessor extends AbstractOutputProvider<CotEventContainer> implements InputProviderInterface<CotEventContainer> {

    public static final String IMAGE_FORMAT = "jpg";

    // The current save "index"
    private static int identifier = 0;

    // How many photos to save until it starts overwriting
    private static int maxSaves = 10;

    private synchronized String getPhotoFileName() {
        String imageName = "image" + identifier + "." + IMAGE_FORMAT;

        if (identifier >= maxSaves) {
            identifier = 0;
        } else {
            identifier++;
        }
        return imageName;
    }

    @Nullable
    public BufferedImage process(@Nullable BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }
        try {
            ImageIO.write(bufferedImage, IMAGE_FORMAT, new File(getPhotoFileName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }


    public InFromNetworkImageProcessor() {
    }

    public void handleData(@NotNull CotEventContainer data) {
        BufferedImage bufferedImage = data.getBufferedImage();

        // SOImages-work: 853D8A8C-1EE5-44CD-91C2-5A7E2B57922F
        if (bufferedImage != null) {
            try {
                ImageIO.write(bufferedImage, IMAGE_FORMAT, new File(getPhotoFileName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            distributeResult(data);
        }
        // SOImages-work-end
    }
}

