package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.Output;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
public class BitmapWriter implements ConsumingPipe<Bitmap> {

    private final ConsumingPipe<String> next;

    public BitmapWriter(@Output ConsumingPipe<String> next) {
        this.next = next;
    }

    @Override
    public void consume(Bitmap input) {
        FileOutputStream out = null;
        String fileName = "/sdcard/ataklite/ATAKLite-received-" + System.currentTimeMillis() + ".jpg";
        try {
            out = new FileOutputStream(new File(fileName));
            input.compress(Bitmap.CompressFormat.JPEG, 50, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        next.consume(fileName);
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