package mil.darpa.immortals.dfus.images;

import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by awellman@bbn.com on 6/22/17.
 */
public class BitmapWriter extends AbstractFunctionConsumingPipe<BufferedImage, Path> {

    private static final String IMAGE_FORMAT = "jpg";
    
    private static AtomicInteger ticker = new AtomicInteger();

    private final Path storagePath;

    public BitmapWriter(@Nonnull Path storagePath, @Nullable ConsumingPipe<Path> next) {
        super(true, next);
        this.storagePath = storagePath;
    }

    @Override
    public Path process(BufferedImage input) {
        Path p = storagePath.resolve("image-" + ticker.getAndIncrement() + ".jpg");
        try {
            ImageIO.write(input, IMAGE_FORMAT, p.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    }
}