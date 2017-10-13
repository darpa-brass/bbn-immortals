package com.bbn.ataklite.pipes;

import android.graphics.Bitmap;
import android.location.Location;
import com.bbn.ataklite.ATAKLite;
import com.bbn.ataklite.CoTMessage;
import mil.darpa.immortals.core.synthesis.ObjectPipeMultiplexerTail;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;

/**
 * A class that combines a location and bitmap into a proper CoT message
 * Created by awellman@bbn.com on 6/22/16.
 */
public class ImageCotifier extends ObjectPipeMultiplexerTail<Bitmap, Location, CoTMessage> {

    private final Bitmap.CompressFormat DEFAULT_COMPRESSION_FORMAT = Bitmap.CompressFormat.JPEG;
    private final int DEFAULT_COMPRESSION_QUALITY = 72;


    public ImageCotifier(@Nonnull ConsumingPipe<CoTMessage> outputPipe) {
        super(outputPipe);
    }

    @Override
    protected CoTMessage process(Bitmap bitmap, Location location) {
        CoTMessage message = new CoTMessage(location, ATAKLite.getConfigInstance().callsign);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(DEFAULT_COMPRESSION_FORMAT, DEFAULT_COMPRESSION_QUALITY, baos);
        message.setImage(baos.toByteArray(), bitmap.getHeight(), bitmap.getWidth(), DEFAULT_COMPRESSION_FORMAT);
        message.updateTimestamp();
        return message;
    }

}