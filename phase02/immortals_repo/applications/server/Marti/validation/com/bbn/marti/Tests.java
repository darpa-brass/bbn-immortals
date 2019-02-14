package com.bbn.marti;

import ch.qos.logback.classic.Level;
import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.pipelines.CotDbInsertionPipe;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.google.gson.Gson;
import mil.darpa.immortals.annotation.dsl.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation;
import mil.darpa.immortals.core.api.ll.phase1.Status;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.validation.Validators;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.datatypes.cot.dom4j.CotParser;
import mil.darpa.immortals.dfus.TakServerDataManager.DataManager;
import mil.darpa.immortals.ontology.BaselineFunctionalAspect;
import mil.darpa.immortals.ontology.ElevationDfu;
import mil.darpa.immortals.ontology.GetElevationFunctionalAspect;
import mockit.Expectations;
import org.postgresql.ds.PGPoolingDataSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by awellman@bbn.com on 8/3/17.
 */
// TODO: This is a good idea to speed up mutation, but we need all tests to run for the initial execution. It should probably be replaced with a gradle update that can use the --fail-fast parameter
//@Listeners(ValidationRunner.FailureListener.class)
public class Tests {

    private static final String COT_DATA_SOURCE = "TakDataSource2";
    private static final String SERVER_NAME = "localhost";
    private static final String DATABASE_NAME = "immortals";
    private static final String USER = "immortals";
    private static final String PASSWORD = "immortals";
    private static final int MAXIMUM_NUMBER_CONNECTIONS = 4;
    private static final String REPORTING_SCHEMA = "takrpt";

    public Tests() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ALL);
    }

    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    @Test(priority = 100)
    public void testImageTransmission() {
        TestResult result = ValidationRunner.getInstance().execute(Validators.CLIENT_IMAGE_SHARE);
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    @Test(priority = 100)
    public void testLatestSaTransmission() {
        TestResult result = ValidationRunner.getInstance().execute(Validators.CLIENT_LOCATION_SHARE);
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    @Test(priority = 100)
    public void testImageSave() {
        // TODO: This test should be threaded. But no time...
        ValidationRunner.getInstance().execute();

        int imageCount = 0;

        long endTime = ValidationRunner.getInstance().getStartTimeMS() + ValidationRunner.getInstance().getTimeoutMS();

        System.out.println("IMGDIR=" + ValidationRunner.getInstance().getMartiStorageDirectory());
        File dir = ValidationRunner.getInstance().getMartiStorageDirectory().toFile();

//        while (System.currentTimeMillis() < endTime && imageCount < 3) {
//            imageCount = 0;

            File[] fileList = dir.listFiles();
            Assert.assertNotNull(fileList);
            for (File f : dir.listFiles()) {
                String fp = f.getAbsolutePath();
                if (fp.endsWith("jpg") || fp.endsWith("jpeg")) {
                    imageCount++;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Assert.fail("Failure due to test interruption!");
                throw new RuntimeException(e);
            }
//        }

        Assert.assertTrue(imageCount >= 3);
    }

    @ProvidedFunctionalityValidationAnnotation(validatedFunctionality = ElevationDfu.class, validatedAspects = {BaselineFunctionalAspect.class, GetElevationFunctionalAspect.class})
    @Test(priority = 100)
    public void testElevationAccuracyEnhancement() {
        // The radius of the earth. Kind of like a negative value?
        double maxLe = 6371000;

        final AtomicBoolean assertionPerformed = new AtomicBoolean(false);

        CotByteBufferPipe cbbp = new CotByteBufferPipe(new ConsumingPipe<CotEventContainer>() {
            @Override
            public void consume(CotEventContainer input) {
                double le = input.getLe();
                Assert.assertTrue(input.getLe() < maxLe);
                assertionPerformed.set(true);
            }

            @Override
            public void flushPipe() {

            }

            @Override
            public void closePipe() {

            }
        });

        String cotXml = "<event version=\"2.0\" type=\"a-f-G-U-C\" how=\"h-g-i-g-o\" uid=\"1337\" time=\"2015-10-20T09:48:28.449Z\" start=\"2015-10-20T09:48:28.449Z\" stale=\"2015-10-21T09:48:28.449Z\"><point lat=\"48.062954962946364\" lon=\"-99.72727857974587\" hae=\"33\" ce=\"100\" le=\"20\"/><detail/></event>";
        cbbp.consume(cotXml.getBytes());

        Assert.assertTrue(assertionPerformed.get());
    }

    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    @Test(priority = 100)
    public void testCotByteBufferPipe() {
        final AtomicBoolean assertionPerformed = new AtomicBoolean(false);

        CotByteBufferPipe cbbp = new CotByteBufferPipe(new ConsumingPipe<CotEventContainer>() {
            @Override
            public void consume(CotEventContainer input) {
                Assert.assertTrue("a-f-G-U-C".equals(input.getType()));
                Assert.assertTrue("1337".equals(input.getUid()));
                Assert.assertTrue("2015-10-20T09:48:28.449Z".equals(input.getTime()));
                Assert.assertTrue("2015-10-20T09:48:28.449Z".equals(input.getStart()));
                Assert.assertTrue("2015-10-21T09:48:28.449Z".equals(input.getStale()));
                Assert.assertTrue(48.062954962946364 == input.getLat());
                Assert.assertTrue(-99.72727857974587 == input.getLon());
                Assert.assertTrue(9999999 == input.getCe());
                assertionPerformed.set(true);
            }

            @Override
            public void flushPipe() {

            }

            @Override
            public void closePipe() {

            }
        });

        String cotXml = "<event version=\"2.0\" type=\"a-f-G-U-C\" how=\"h-g-i-g-o\" uid=\"1337\" time=\"2015-10-20T09:48:28.449Z\" start=\"2015-10-20T09:48:28.449Z\" stale=\"2015-10-21T09:48:28.449Z\"><point lat=\"48.062954962946364\" lon=\"-99.72727857974587\" hae=\"9999999\" ce=\"9999999\" le=\"9999999\"/><detail/></event>";
        cbbp.consume(cotXml.getBytes());

        Assert.assertTrue(assertionPerformed.get());
    }

    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    @Test(priority = 100)
    public void testDbIntegration() {
        try {
            PGPoolingDataSource dataSource = null;


            if ("true".equals(System.getProperty("mil.darpa.immortals.mock"))) {
                DataManager dm = new DataManager();
                new Expectations(DataManager.class) {{
                    dm.insertEvent((Event) any);
                }};

            } else {
                dataSource = new PGPoolingDataSource();
                dataSource.setDataSourceName(COT_DATA_SOURCE);
                dataSource.setServerName(SERVER_NAME);
                dataSource.setDatabaseName(DATABASE_NAME);
                dataSource.setUser(USER);
                dataSource.setPassword(PASSWORD);
                dataSource.setMaxConnections(MAXIMUM_NUMBER_CONNECTIONS);
                dataSource.setCurrentSchema(REPORTING_SCHEMA);
            }

            Random r = new Random();
            final AtomicBoolean assertionPerformed = new AtomicBoolean(false);
            final String uuid = "AC763E";
            final double lat = 180 * r.nextDouble() - 90;
            final double lon = 360 * r.nextDouble() - 180;
            final String cotType = "a-f-G-U-C";
            final String how = "hhhhh-g-i-g-o";
            final int hae = r.nextInt(3000);
            final int ce = r.nextInt(30);
            final int le = r.nextInt(40);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            long currentTimeLong = System.currentTimeMillis();
            String currentTime = sdf.format(currentTimeLong);
            String stale = sdf.format(currentTimeLong + 12000000);

            String cotXml = "<event version=\"2.0\" type=\"" + cotType + "\" how=\"" + how + "\" uid=\"" + uuid +
                    "\" time=\"" + currentTime + "\" start=\"" + currentTime + "\" stale=\"" + stale +
                    "\"><point lat=\"" + lat + "\" lon=\"" + lon + "\" hae=\"" + hae + "\" ce=\"" + ce + "\" le=\"" +
                    le + "\"/><detail/></event>";
            String selectStatement = "SELECT * FROM tak.cot_event_position, tak.source, tak.cot_event " +
                    "WHERE cot_event.id = cot_event_position.cot_event_id and cot_event.source_id = source.source_id" +
                    " AND name = '" + uuid + "' AND latitude = " + lat + " AND longitude = " + lon +
                    " AND point_hae = " + hae + " AND point_le = " + le + " AND point_ce = " + ce +
                    " AND how = '" + how + "' AND cot_type = '" + cotType + "';";

            CotDbInsertionPipe cdip = new CotDbInsertionPipe(new ConsumingPipe<CotEventContainer>() {
                @Override
                public void consume(CotEventContainer input) {
                    Assert.assertEquals(uuid, input.getUid());
                    Assert.assertEquals(currentTime, input.getTime());
                    Assert.assertEquals(currentTime, input.getStart());
                    Assert.assertEquals(stale, input.getStale());
                    Assert.assertEquals(lat, input.getLat());
                    Assert.assertEquals(lon, input.getLon());
                    Assert.assertEquals(cotType, input.getType());
                    Assert.assertEquals(how, input.getHow());
                    Assert.assertEquals(ce, input.getCe().intValue());
                    Assert.assertEquals(le, input.getLe().intValue());
                    Assert.assertEquals(hae, input.getHae().intValue());
                    assertionPerformed.set(true);
                }

                @Override
                public void flushPipe() {

                }

                @Override
                public void closePipe() {

                }
            });

            CotEventContainer cec = new CotEventContainer(CotParser.parse(cotXml));
            cdip.consume(cec);

            if (dataSource != null) {
                ResultSet resultSet = dataSource.getConnection().prepareStatement(selectStatement).executeQuery();
                CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
                rowSet.populate(resultSet);
                Assert.assertEquals(rowSet.size(), 1);
            }

            Assert.assertTrue(assertionPerformed.get());
        } catch (Exception e) {
            Assert.fail("Exception encountered: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
