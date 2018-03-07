package com.bbn.marti;

import ch.qos.logback.classic.Level;
import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.google.gson.Gson;
import mil.darpa.immortals.core.api.ll.phase1.Status;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.validation.Validators;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by awellman@bbn.com on 8/3/17.
 */
@Listeners(ValidationRunner.FailureListener.class)
public class Tests {
    
    public static final String INTEGRATION_TESTS = "integrationTests";
    public static final String VALIDATION_TESTS = "validationTests";
    
    private static ValidationRunner validationRunner = ValidationRunner.getInstance();
    private static ValidationResults results;


    public Tests() {

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ALL);
    }

    @Test(groups = {INTEGRATION_TESTS}, priority = 100)
    public void testImageTransmission() {
        TestResult result = validationRunner.execute(Validators.CLIENT_IMAGE_SHARE);
        System.out.println(new Gson().toJson(result));
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @Test(groups = {INTEGRATION_TESTS}, priority = 100)
    public void testLatestSaTransmission() {
        TestResult result = validationRunner.execute(Validators.CLIENT_LOCATION_SHARE);
        System.out.println(new Gson().toJson(result));
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @Test(groups = {INTEGRATION_TESTS}, priority = 100)
    public void testImageSave() {
        validationRunner.execute();

        int imageCount = 0;

        long endTime = validationRunner.getStartTimeMS() + validationRunner.getTimeoutMS();

        File dir = validationRunner.getMartiStorageDirectory().toFile();

        while (System.currentTimeMillis() < endTime && imageCount < 3) {
            imageCount = 0;

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
        }

        Assert.assertTrue(imageCount >= 3);
    }
    
    @Test(groups = {INTEGRATION_TESTS}, priority = 0)
    public void testElevationAccuracyEnhancement() {
        double maxLe = 10000;

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
        
        String cotXml = "<event version=\"2.0\" type=\"a-f-G-U-C\" how=\"h-g-i-g-o\" uid=\"1337\" time=\"2015-10-20T09:48:28.449Z\" start=\"2015-10-20T09:48:28.449Z\" stale=\"2015-10-21T09:48:28.449Z\"><point lat=\"48.062954962946364\" lon=\"-99.72727857974587\" hae=\"9999999\" ce=\"9999999\" le=\"9999999\"/><detail/></event>";
        cbbp.consume(cotXml.getBytes());
        
        Assert.assertTrue(assertionPerformed.get());
    }
}
