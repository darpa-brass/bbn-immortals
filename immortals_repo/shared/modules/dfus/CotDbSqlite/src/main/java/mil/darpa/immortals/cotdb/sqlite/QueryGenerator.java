package mil.darpa.immortals.cotdb.sqlite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Used to generate queries
 * <p>
 * Created by awellman@bbn.com on 6/2/17.
 */
public class QueryGenerator {
    /**
     * Bounding boxes where the math is simple
     * <p>
     * Created by awellman@bbn.com on 6/2/17.
     */
    @SuppressWarnings("unused")
    public enum QuadrantRestrictedBoundingBox {
        UnitedStates(
                30.755641,
                48.562068,
                -122.387100,
                -81.490127
        ),
        Australia(
                -31.250515,
                -21.176879,
                116.714491,
                144.663708
        ),
        Argentina(
                -38.176958,
                -25.137360,
                -68.123313,
                -60.466707
        ),
        Russia(
                56.914912,
                65.924007,
                42.534806,
                131.480111
        );

        public final double minLat;
        public final double maxLat;
        public final double minLon;
        public final double maxLon;

        QuadrantRestrictedBoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }
    }

    /**
     * Created by awellman@bbn.com on 6/2/17.
     */
    @SuppressWarnings("unused")
    public enum CotEventHow {
        AndroidGps("m-g"),
        BluetoothGps("m-g-s-b"),
        Manual("h-e-s"),
        SaasmTrustedGps("m-r-p"),
        UsbGps("m-g-s-u");

        public final String value;

        CotEventHow(String value) {
            this.value = value;
        }
    }

    @SuppressWarnings("unused")
    public enum CotEventUser {
        User00("ATAKLite-dbpopulatorchallenge_0-000"),
        User01("ATAKLite-dbpopulatorchallenge_0-001"),
        User02("ATAKLite-dbpopulatorchallenge_0-003"),
        User03("ATAKLite-dbpopulatorchallenge_0-002");

        public final String uid;

        CotEventUser(String uid) {
            this.uid = uid;
        }
    }


    public static final String TIMESTAMP_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    @SuppressWarnings("unused")
    public enum KnownDataRange {

        D2017_06_05("2017-06-05T13:00:31.472Z", "2017-06-05T14:00:53.434Z"),
        D2017_06_12("2017-06-12T11:16:16.000Z", "2017-06-12T11:30:12.485Z");
//        D2017_06_02("2017-06-02T16:00:36.316Z", "2017-06-02T17:00:26.320Z"),
//        D2017_06_05("2017-06-05T10:00:20.000Z", "2017-06-05T11:15:50.591Z");

        public final String start;
        public final String end;

        KnownDataRange(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public Date startDate() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMATTER, Locale.US);
                return sdf.parse(start);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public Date endDate() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMATTER, Locale.US);
                return sdf.parse(end);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @SuppressWarnings("unused")
    public enum SelectionType {
        All("*"),
        UserTracks("uid, time, pointLat, pointLon"),
        ImageData("imageHeight, imageWidth, imageMime, imageSize, imageData"),
        UserTracksAndImageData("time, pointLat, pointLon, imageHeight, imageWidth, imageMime, imageSize, imageData");

        public final String selectors;

        SelectionType(String selectors) {
            this.selectors = selectors;
        }
    }

    public static String generatePreparedStatement(
            @Nonnull SelectionType selectionType,
            @Nullable CotEventHow how,
            @Nullable Boolean hasImage,
            boolean requireUid,
            boolean requireBoundingBox,
            boolean requireDateStart,
            boolean requireDateEnd) {

        LinkedList<String> withValues = new LinkedList<>();
        if (requireUid) {
            if (selectionType == SelectionType.All || selectionType == SelectionType.UserTracksAndImageData
                    || selectionType == SelectionType.ImageData) {
                withValues.add("uid LIKE ?");
            } else {
                withValues.add("uid == ?");
            }
        }

        if (how != null) {
            withValues.add("how == '" + how.value + "'");
        }

        if (hasImage != null) {
            if (hasImage) {
                withValues.add("imageData IS NOT NULL");
            } else {
                withValues.add("imageData IS NULL");
            }
        }

        if (requireBoundingBox) {
            withValues.add("pointLat < ? AND pointLat > ? AND pointLon < ? AND pointLon > ?");

        }

        if (requireDateStart) {
            withValues.add("time >= ?");
        }

        if (requireDateEnd) {
            withValues.add("time < ?");
        }

        if (withValues.size() == 0) {
            return "SELECT " + selectionType.selectors + " FROM CotEvent;";

        } else {
            StringBuilder sb = new StringBuilder();

            for (String withValue : withValues) {
                if (sb.length() == 0) {
                    sb.append("SELECT ").append(selectionType.selectors).append(" FROM CotEvent WHERE ").append(withValue);

                } else {
                    sb.append(" AND ").append(withValue);
                }
            }
            return sb.toString();
        }

    }

    public static String generateEventSelectionQuery(
            @Nonnull SelectionType selectionType,
            @Nullable CotEventUser user,
            @Nullable QueryGenerator.CotEventHow how,
            @Nullable Boolean hasImage,
            @Nullable QueryGenerator.QuadrantRestrictedBoundingBox bbox,
            @Nullable Date startDate,
            @Nullable Date endDate) {

        String stmt = generatePreparedStatement(selectionType, how, hasImage,
                user != null, bbox != null,
                startDate != null, endDate != null);

        if (user != null) {

            if (selectionType == SelectionType.All || selectionType == SelectionType.UserTracksAndImageData
                    || selectionType == SelectionType.ImageData) {
                stmt = stmt.replaceFirst("\\?", "'" + user.uid + "%'");
            } else {
                stmt = stmt.replaceFirst("\\?", "'" + user.uid + "'");
            }
        }

        if (bbox != null) {
            stmt = stmt.replaceFirst("\\?", String.valueOf(bbox.maxLat));
            stmt = stmt.replaceFirst("\\?", String.valueOf(bbox.minLat));
            stmt = stmt.replaceFirst("\\?", String.valueOf(bbox.maxLon));
            stmt = stmt.replaceFirst("\\?", String.valueOf(bbox.minLon));
        }

        // TODO: FIX
        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMATTER);
        if (startDate != null) {
            stmt = stmt.replaceFirst("\\?", "'" + sdf.format(startDate) + "'");
        }

        if (endDate != null) {
            stmt = stmt.replaceFirst("\\?", "'" + sdf.format(endDate) + "'");
        }

        return stmt;
    }

    public static List<String> getSamplePreparedStatements() {
        ArrayList<String> returnList = new ArrayList<String>(6);
        String id, query;

        id = "getAllForUserSinceTime";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        QueryGenerator.SelectionType.All,
                        null,
                        null,
                        true,
                        false,
                        true,
                       false 
                ) + "\";";
        returnList.add(query);

        id = "getTracksForUser";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        SelectionType.UserTracks,
                        null,
                        null,
                        true,
                        false,
                        false,
                        false
                ) + "\";";
        returnList.add(query);

        id = "getImagesForUserSinceTime";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        SelectionType.ImageData,
                        null,
                        true,
                        true,
                        false,
                        true,
                       false 
                ) + "\";";
        returnList.add(query);

        id = "getUserTracksAndImagesWithinLocationSinceTime";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        SelectionType.UserTracksAndImageData,
                        null,
                        true,
                        false,
                        true,
                        true,
                       false 
                ) + "\";";
        returnList.add(query);

        id = "getUserTracksWithinRegion";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        SelectionType.UserTracks,
                        null,
                        null,
                        false,
                        true,
                        false,
                        false
                ) + "\";";
        returnList.add(query);

        id = "getSecureActivitySinceTime";
        query = "public static final String " + id + "_statement = \"" +
                QueryGenerator.generatePreparedStatement(
                        SelectionType.UserTracks,
                        CotEventHow.SaasmTrustedGps,
                        null,
                        false,
                        false,
                        true,
                       false 
                ) + "\";";
        returnList.add(query);

        return returnList;
    }


    public static List<String> getSampleQueries() {
        ArrayList<String> returnList = new ArrayList(6);
        String query, id;

        id = "getAllForUserSinceTime";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        QueryGenerator.SelectionType.All,
                        QueryGenerator.CotEventUser.User00,
                        null,
                        null,
                        null,
                        QueryGenerator.KnownDataRange.D2017_06_05.startDate(),
//                        QueryGenerator.KnownDataRange.D2017_06_05.endDate()
                        null

                ) + "\";";
        returnList.add(query);

        id = "getTracksForUser";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        SelectionType.UserTracks,
                        QueryGenerator.CotEventUser.User01,
                        null,
                        null,
                        null,
                        null,
                        null
                ) + "\";";
        returnList.add(query);

        id = "getImagesForUserSinceTime";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        SelectionType.ImageData,
                        QueryGenerator.CotEventUser.User02,
                        null,
                        true,
                        null,
                        KnownDataRange.D2017_06_12.startDate(),
//                        QueryGenerator.KnownDataRange.D2017_06_12.endDate()
                        null
                ) + "\";";
        returnList.add(query);

        id = "getUserTracksAndImagesWithinLocationSinceTime";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        SelectionType.UserTracksAndImageData,
                        null,
                        null,
                        true,
                        QuadrantRestrictedBoundingBox.UnitedStates,
                        QueryGenerator.KnownDataRange.D2017_06_05.startDate(),
//                        QueryGenerator.KnownDataRange.D2017_06_05.endDate()
                        null
                ) + "\";";
        returnList.add(query);

        id = "getUserTracksWithinRegion";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        SelectionType.UserTracks,
                        null,
                        null,
                        null,
                        QuadrantRestrictedBoundingBox.Australia,
                        null,
                        null
                ) + "\";";
        returnList.add(query);

        id = "getSecureActivitySinceTime";
        query = "private static final String " + id + "_filled = \"" +
                QueryGenerator.generateEventSelectionQuery(
                        SelectionType.UserTracks,
                        null,
                        CotEventHow.SaasmTrustedGps,
                        null,
                        null,
                        QueryGenerator.KnownDataRange.D2017_06_05.startDate(),
//                        QueryGenerator.KnownDataRange.D2017_06_05.endDate()
                        null
                ) + "\";";
        returnList.add(query);

        return returnList;
    }
}
