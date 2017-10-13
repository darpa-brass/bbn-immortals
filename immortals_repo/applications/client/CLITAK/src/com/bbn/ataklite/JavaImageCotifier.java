package com.bbn.ataklite;

//import android.graphics.Bitmap;

//import android.location.Location;

import mil.darpa.immortals.core.synthesis.ObjectPipeMultiplexerTail;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.Coordinates;
import mil.darpa.immortals.datatypes.cot.Detail;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.javatypeconverters.CotDataCoordinateConverter;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A class that combines a location and bitmap into a proper CoT message
 * Created by awellman@bbn.com on 6/22/16.
 */
public class JavaImageCotifier extends ObjectPipeMultiplexerTail<BufferedImage, Coordinates, Event> {

    // TODO: Set Compression
    private final int DEFAULT_COMPRESSION_QUALITY = 72;

    private final String callsign;


    public JavaImageCotifier(@Nonnull String callsign, @Nonnull ConsumingPipe<Event> outputPipe) {
        super(outputPipe);
        this.callsign = callsign;
    }

    @Override
    protected Event process(BufferedImage bitmap, Coordinates location) {
        try {
            Event message = CotDataCoordinateConverter.toEvent(location, callsign);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bitmap, "jpeg", baos);

            message.setType("a-u-G");

            if (message.getDetail() == null) {
                message.setDetail(new Detail());
            }

            if (message.getDetail().getImage() == null) {
                message.getDetail().setImage(new Detail.Image());
            }

            Detail.Image i = message.getDetail().getImage();

            i.setHeight(bitmap.getHeight());
            i.setWidth(bitmap.getWidth());
            i.setMime("image/jpeg");
            i.setSize((long) baos.size());
            i.setValue(new String(Base64.encodeBase64(baos.toByteArray())));

            // TODO: Update timestamp
//            message.updateTimestamp();

            return message;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}