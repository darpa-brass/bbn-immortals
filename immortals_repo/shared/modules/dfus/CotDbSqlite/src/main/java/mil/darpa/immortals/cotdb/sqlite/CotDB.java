package mil.darpa.immortals.cotdb.sqlite;

import com.google.gson.Gson;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabaseColumns;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabasePerturbation;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabaseTableConfiguration;
import mil.darpa.immortals.datatypes.cot.Detail;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.datatypes.cot.Point;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * CoT Database interface
 * <p>
 * Created by awellman@bbn.com on 4/7/17.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CotDB {

    private static final String getAllForUserSinceTime_filled = "SELECT * FROM CotEvent WHERE uid LIKE 'ATAKLite-dbpopulatorchallenge_0-000%' AND time >= '2017-06-05T13:00:31.472Z'";
    private static final String getTracksForUser_filled = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE uid == 'ATAKLite-dbpopulatorchallenge_0-001'";
    private static final String getImagesForUserSinceTime_filled = "SELECT imageHeight, imageWidth, imageMime, imageSize, imageData FROM CotEvent WHERE uid LIKE 'ATAKLite-dbpopulatorchallenge_0-003%' AND imageData IS NOT NULL AND time >= '2017-06-12T11:16:16.000Z'";
    private static final String getUserTracksAndImagesWithinLocationSinceTime_filled = "SELECT time, pointLat, pointLon, imageHeight, imageWidth, imageMime, imageSize, imageData FROM CotEvent WHERE imageData IS NOT NULL AND pointLat < 48.562068 AND pointLat > 30.755641 AND pointLon < -81.490127 AND pointLon > -122.3871 AND time >= '2017-06-05T13:00:31.472Z'";
    private static final String getUserTracksWithinRegion_filled = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE pointLat < -21.176879 AND pointLat > -31.250515 AND pointLon < 144.663708 AND pointLon > 116.714491";
    private static final String getSecureActivitySinceTime_filled = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE how == 'm-r-p' AND time >= '2017-06-05T13:00:31.472Z'";

    public static final String getAllForUserSinceTime_statement = "SELECT * FROM CotEvent WHERE uid LIKE ? AND time >= ?";
    public static final String getTracksForUser_statement = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE uid == ?";
    public static final String getImagesForUserSinceTime_statement = "SELECT imageHeight, imageWidth, imageMime, imageSize, imageData FROM CotEvent WHERE uid LIKE ? AND imageData IS NOT NULL AND time >= ?";
    public static final String getUserTracksAndImagesWithinLocationSinceTime_statement = "SELECT time, pointLat, pointLon, imageHeight, imageWidth, imageMime, imageSize, imageData FROM CotEvent WHERE imageData IS NOT NULL AND pointLat < ? AND pointLat > ? AND pointLon < ? AND pointLon > ? AND time >= ?";
    public static final String getUserTracksWithinRegion_statement = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE pointLat < ? AND pointLat > ? AND pointLon < ? AND pointLon > ?";
    public static final String getSecureActivitySinceTime_statement = "SELECT uid, time, pointLat, pointLon FROM CotEvent WHERE how == 'm-r-p' AND time >= ?";

    private static final String TIMESTAMP_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private Connection connection;

    private final SimpleDateFormat simpleDateFormat;

    private static final String SOURCE_DB_RESOURCE_PATH = "/populated_db.sqlite";

    public void insertEvent(Event event) throws SQLException {
        HashMap<String, Object> m = new HashMap<>();
        Point p = event.getPoint();
        Detail d = event.getDetail();
        Detail.Contact c = d == null ? null : d.getContact();
        Detail.Image i = d == null ? null : d.getImage();

        m.put("uid", event.getUid());
        m.put("type", event.getType());
        m.put("version", event.getVersion());
        m.put("time", event.getTime());
        m.put("start", event.getStart());
        m.put("stale", event.getStale());
        m.put("how", event.getHow());
        m.put("opex", event.getOpex());
        m.put("qos", event.getQos());
        m.put("qos", event.getQos());
        m.put("access", event.getAccess());

        if (p != null) {
            m.put("hae", p.getHae());
            m.put("ce", p.getCe());
            m.put("le", p.getLe());
            m.put("pointLat", p.getLat());
            m.put("pointLon", p.getLon());
        }

        if (c != null) {
            m.put("contactCallsign", c.getCallsign());
            m.put("contactEndpoint", c.getEndpoint());
        }

        if (i != null) {
            m.put("imageHeight", i.getHeight());
            m.put("imageWidth", i.getWidth());
            m.put("imageMime", i.getMime());
            m.put("imageSize", i.getSize());
            m.put("imageData", i.getValue());
        }

        StringBuilder kb = new StringBuilder();
        StringBuilder vb = new StringBuilder();

        for (String key : m.keySet()) {
            Object v = m.get(key);

            if (v != null) {

                if (v instanceof String || v instanceof XMLGregorianCalendar) {
                    kb.append(", ").append(key);
                    vb.append(", '").append(v).append("'");

                } else if (v instanceof Integer || v instanceof BigDecimal || v instanceof Long) {
                    kb.append(", ").append(key);
                    vb.append(", ").append(v);

                } else {
                    throw new RuntimeException("Unexpected value type of " + v.getClass() + "!");
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMATTER, Locale.US);
        String now = "'" + sdf.format(new Date()) + "'";
        String insertStatement = "INSERT INTO CotEvent(receiveTime" + kb.toString() + ") VALUES (" + now + vb.toString() + ");";

        Statement statement = connection.createStatement();
        statement.execute(insertStatement);
        statement.close();
    }

    private static CotDB cotDb;

    public static CotDB getInstance() {
        return cotDb;
    }

    private CotDB() {
        simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMATTER, Locale.US);
    }

    public static CotDB initializeInstance() throws IOException {
        if (cotDb != null) {
            throw new RuntimeException("CotDB Instance has already been initialized!");
        }
        cotDb = new CotDB();
        cotDb.initialize();
        return cotDb;
    }

    private void initialize() throws IOException {

        try {
            Class.forName("org.sqlite.JDBC");

            if (!Files.exists(Paths.get("immortals.db"))) {
                InputStream original_is = this.getClass().getResourceAsStream(SOURCE_DB_RESOURCE_PATH);
                Files.copy(original_is, Paths.get("immortals.db"));
            }

            connection = DriverManager.getConnection("jdbc:sqlite:immortals.db");
            System.out.println("Connected to CotDB!");

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int submitQuery(String query) throws SQLException {
        int count = 0;
        Statement statement = connection.createStatement();
        ResultSet r = statement.executeQuery(query);

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getAllForUserSinceTime(String uid, Date startDate) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getAllForUserSinceTime_statement);
        statement.setString(1, uid + "%");
        statement.setString(2, simpleDateFormat.format(startDate));
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getTracksForUser(String uid) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getTracksForUser_statement);
        statement.setString(1, uid);
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getImagesForUserSinceTime(String uid, Date startDate) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getImagesForUserSinceTime_statement);
        statement.setString(1, uid + "%");
        statement.setString(2, simpleDateFormat.format(startDate));
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getUserTracksAndImagesWithinLocationSinceTime(QueryGenerator.QuadrantRestrictedBoundingBox bbox, Date startDate) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getUserTracksAndImagesWithinLocationSinceTime_statement);
        statement.setDouble(1, bbox.maxLat);
        statement.setDouble(2, bbox.minLat);
        statement.setDouble(3, bbox.maxLon);
        statement.setDouble(4, bbox.minLon);
        statement.setString(5, simpleDateFormat.format(startDate));
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getUserTracksWithinRegion(QueryGenerator.QuadrantRestrictedBoundingBox bbox) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getUserTracksWithinRegion_statement);
        statement.setDouble(1, bbox.maxLat);
        statement.setDouble(2, bbox.minLat);
        statement.setDouble(3, bbox.maxLon);
        statement.setDouble(4, bbox.minLon);
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }

    public int getSecureActivitySinceTime(Date startDate) throws SQLException {
        int count = 0;
        PreparedStatement statement = connection.prepareStatement(getSecureActivitySinceTime_statement);
        statement.setString(1, simpleDateFormat.format(startDate));
        ResultSet r = statement.executeQuery();

        while (r.next()) {
            count++;
        }
        statement.close();
        return count;
    }


    public static void main(String args[]) {
        
        Gson gson = new Gson();
        InputStream is = CotDB.class.getResourceAsStream("/sample_perturbation.json");
        DatabasePerturbation dp = gson.fromJson(new InputStreamReader(is), DatabasePerturbation.class);
        
        System.out.println("WHAAT?");
        
        for (DatabaseTableConfiguration tc : dp.tables) {
            System.out.println("TC: ");
            for(DatabaseColumns c : tc.columns) {
                System.out.println("\n" + c.name());
            }
        }
        
        if (args.length > 0) {
            if ("--gen".equals(args[0])) {
                List<String> stmts = QueryGenerator.getSampleQueries();
                for (String stmt : stmts) {
                    System.out.println(stmt);
                }
                System.out.println();

                stmts = QueryGenerator.getSamplePreparedStatements();
                for (String stmt : stmts) {
                    System.out.println(stmt);
                }
            } else if ("--test".equals(args[0])) {
                try {
                    CotDB db = CotDB.initializeInstance();


                    System.out.println("Executing getAllForUserSinceTime_statement query: \n\t" + getAllForUserSinceTime_statement);
                    System.out.println("Result Count: " +
                            db.getAllForUserSinceTime(
                                    QueryGenerator.CotEventUser.User00.uid,
                                    QueryGenerator.KnownDataRange.D2017_06_05.startDate()
                            ));

                    System.out.println("RC: " + db.submitQuery(getAllForUserSinceTime_filled));


                    System.out.println("Executing getTracksForUser_statement query: \n\t" + getTracksForUser_statement);
                    System.out.println("Result Count: " + db.getTracksForUser(QueryGenerator.CotEventUser.User01.uid));
                    System.out.println("RC: " + db.submitQuery(getTracksForUser_filled));


                    System.out.println("Executing getImagesForUserSinceTime_statement query: \n\t" + getImagesForUserSinceTime_statement);
                    System.out.println("Result Count: " + db.getImagesForUserSinceTime(
                            QueryGenerator.CotEventUser.User02.uid,
                            QueryGenerator.KnownDataRange.D2017_06_12.startDate()));
                    System.out.println("RC: " + db.submitQuery(getImagesForUserSinceTime_filled));


                    System.out.println("Executing getUserTracksAndImagesWithinLocationSinceTime_statement query: \n\t" + getUserTracksAndImagesWithinLocationSinceTime_statement);
                    System.out.println("Result Count: " + db.getUserTracksAndImagesWithinLocationSinceTime(
                            QueryGenerator.QuadrantRestrictedBoundingBox.UnitedStates,
                            QueryGenerator.KnownDataRange.D2017_06_05.startDate()));
                    System.out.println("RC: " + db.submitQuery(getUserTracksAndImagesWithinLocationSinceTime_filled));


                    System.out.println("Executing getUserTracksWithinRegion_statement query: \n\t" + getUserTracksWithinRegion_statement);
                    System.out.println("Result Count: " + db.getUserTracksWithinRegion(
                            QueryGenerator.QuadrantRestrictedBoundingBox.Australia));
                    System.out.println("RC: " + db.submitQuery(getUserTracksWithinRegion_filled));


                    System.out.println("Executing getSecureActivitySinceTime_statement query: \n\t" + getSecureActivitySinceTime_statement);
                    System.out.println("Result Count: " + db.getSecureActivitySinceTime(
                            QueryGenerator.KnownDataRange.D2017_06_05.startDate()));
                    System.out.println("RC: " + db.submitQuery(getSecureActivitySinceTime_filled));


                } catch (IOException
                        | SQLException
                        e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}


