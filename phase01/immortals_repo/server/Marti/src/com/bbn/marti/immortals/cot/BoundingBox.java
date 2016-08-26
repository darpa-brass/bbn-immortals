package com.bbn.marti.immortals.cot;

/**
 * Created by awellman@bbn.com on 1/8/16.
 */
public class BoundingBox {

    private final long upperLeftLatitude;
    private final long upperLeftLongitude;
    private final long lowerRightLatitude;
    private final long lowerRightLongitude;

    public BoundingBox(long upperLeftLatitude, long upperLeftLongitude, long lowerRightLatitude, long lowerRightLongitude) {
        this.upperLeftLatitude = upperLeftLatitude;
        this.upperLeftLongitude = upperLeftLongitude;
        this.lowerRightLatitude = lowerRightLatitude;
        this.lowerRightLongitude = lowerRightLongitude;
    }

    public boolean containsCoordinate(long latitude, long longitude) {
        return (latitude < upperLeftLatitude && latitude > lowerRightLatitude &&
        longitude > upperLeftLongitude && longitude < lowerRightLongitude);

    }
}
