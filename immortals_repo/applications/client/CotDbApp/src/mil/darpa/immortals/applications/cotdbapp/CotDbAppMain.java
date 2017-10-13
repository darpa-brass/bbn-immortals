package mil.darpa.immortals.applications.cotdbapp;

import mil.darpa.immortals.cotdb.sqlite.CotDB;
import mil.darpa.immortals.cotdb.sqlite.QueryGenerator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by awellman@bbn.com on 6/8/17.
 */
public class CotDbAppMain {


    public static void main(String args[]) {
        try {
            CotDB db = CotDB.initializeInstance();

            System.out.println("Executing getAllForUserSinceTime_statement query: \n\t" + CotDB.getAllForUserSinceTime_statement);
            System.out.println("Result Count: " +
                    db.getAllForUserSinceTime(
                            QueryGenerator.CotEventUser.User00.uid,
                            QueryGenerator.KnownDataRange.D2017_06_05.startDate()
                    ));



            System.out.println("Executing getTracksForUser_statement query: \n\t" + CotDB.getTracksForUser_statement);
            System.out.println("Result Count: " + db.getTracksForUser(QueryGenerator.CotEventUser.User01.uid));


            System.out.println("Executing getImagesForUserSinceTime_statement query: \n\t" + CotDB.getImagesForUserSinceTime_statement);
            System.out.println("Result Count: " + db.getImagesForUserSinceTime(
                    QueryGenerator.CotEventUser.User02.uid,
                    QueryGenerator.KnownDataRange.D2017_06_12.startDate()));


            System.out.println("Executing getUserTracksAndImagesWithinLocationSinceTime_statement query: \n\t" + CotDB.getUserTracksAndImagesWithinLocationSinceTime_statement);
            System.out.println("Result Count: " + db.getUserTracksAndImagesWithinLocationSinceTime(
                    QueryGenerator.QuadrantRestrictedBoundingBox.UnitedStates,
                    QueryGenerator.KnownDataRange.D2017_06_05.startDate()));


            System.out.println("Executing getUserTracksWithinRegion_statement query: \n\t" + CotDB.getUserTracksWithinRegion_statement);
            System.out.println("Result Count: " + db.getUserTracksWithinRegion(
                    QueryGenerator.QuadrantRestrictedBoundingBox.Australia));


            System.out.println("Executing getSecureActivitySinceTime_statement query: \n\t" + CotDB.getSecureActivitySinceTime_statement);
            System.out.println("Result Count: " + db.getSecureActivitySinceTime(
                    QueryGenerator.KnownDataRange.D2017_06_05.startDate()));

        } catch (IOException
                | SQLException
                e) {
            throw new RuntimeException(e);
        }
    }
}
