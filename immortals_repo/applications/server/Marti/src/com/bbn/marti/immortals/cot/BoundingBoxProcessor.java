package com.bbn.marti.immortals.cot;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotData;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;

import javax.annotation.Nonnull;

/**
 * Modifies messages based on a bounding box
 *
 * Created by awellman@bbn.com on 1/8/16.
 */
public class BoundingBoxProcessor {

    private final boolean ignoreCotWithoutLocation;
    private final BoundingBox boundingBox;

    public BoundingBoxProcessor(long upperLeftLatitude, long upperLeftLongitude, long lowerRightLatitude, long lowerRightLongitude, boolean ignoreCotWithoutLocation) {
        this.ignoreCotWithoutLocation = ignoreCotWithoutLocation;
        boundingBox = new BoundingBox(upperLeftLatitude, upperLeftLongitude, lowerRightLatitude, lowerRightLongitude);

    }

    /**
     * Restricts messages to a given area
     */
    public class RestrictToArea extends AbstractOutputProvider<CotData> implements InputProviderInterface<CotData> {

        @Override
        public void handleData(@Nonnull CotData data) {
            CotEventContainer cot = data.cotEventContainer;

            String latString = cot.getLat();
            String lonString = cot.getLon();

            if ((latString == null || lonString == null) && ignoreCotWithoutLocation) {
                distributeResult(data);
            } else {
                long latitude = Long.parseLong(latString);
                long longitude = Long.parseLong(lonString);

                if(boundingBox.containsCoordinate(latitude, longitude)) {
                    distributeResult(data);
                }
            }
        }
    }

    /**
     * Excludes messages from a given area
     */
    public class ExcludeArea extends AbstractOutputProvider<CotData> implements InputProviderInterface<CotData> {

        @Override
        public void handleData(@Nonnull CotData data) {
            CotEventContainer cot = data.cotEventContainer;

            String latString = cot.getLat();
            String lonString = cot.getLon();

            if ((latString == null || lonString == null) && ignoreCotWithoutLocation) {
                distributeResult(data);
            } else {
                long latitude = Long.parseLong(latString);
                long longitude = Long.parseLong(lonString);

                if(!boundingBox.containsCoordinate(latitude, longitude)) {
                    distributeResult(data);
                }
            }
        }
    }
}
