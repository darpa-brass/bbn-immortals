package mil.darpa.immortals.dfus.images;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.Output;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
public class BitmapReader implements ConsumingPipe<String> {

    private ConsumingPipe<BufferedImage> next;

    public BitmapReader(@Output ConsumingPipe<BufferedImage> next) {
        this.next = next;
    }

    @Override
    public void consume(String input) {
        try {
            FileInputStream fis = new FileInputStream(new File(input));
            BufferedImage bi = ImageIO.read(fis);
            next.consume(bi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flushPipe() {
        next.flushPipe();
    }

    @Override
    public void closePipe() {
        next.closePipe();
    }
}
