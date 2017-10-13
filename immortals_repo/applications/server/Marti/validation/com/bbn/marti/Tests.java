package com.bbn.marti;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import mil.darpa.immortals.core.api.ll.phase1.Status;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.validation.Validators;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by awellman@bbn.com on 8/3/17.
 */
public class Tests {

    private static ValidationRunner validationRunner;
    private static ValidationResults results;

    public Tests() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ALL);
    }

    private synchronized void executeValidationRunner() {
        if (validationRunner == null) {
            String[] validators = {
                    Validators.CLIENT_LOCATION_SHARE.identifier,
                    Validators.CLIENT_IMAGE_SHARE.identifier
            };

            validationRunner = new ValidationRunner(validators);
            results = validationRunner.execute();
        }
    }

    private TestResult getResult(String validatorIdentifier) {
        for (TestResult r : results.results) {
            if (r.validatorIdentifier.equals(validatorIdentifier)) {
                return r;
            }
        }
        return null;
    }

    @Test
    public void testImageTransmission() {
        executeValidationRunner();
        TestResult result = getResult(Validators.CLIENT_IMAGE_SHARE.identifier);
        System.out.println(new Gson().toJson(result));
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @Test
    public void testSaTransmission() {
        executeValidationRunner();
        TestResult result = getResult(Validators.CLIENT_LOCATION_SHARE.identifier);
        System.out.println(new Gson().toJson(result));
        Assert.assertEquals(result.currentState, Status.SUCCESS);
    }

    @Test
    public void testImageSave() {
        executeValidationRunner();

        int imageCount = 0;

        long endTime = validationRunner.startTimeMS + validationRunner.timeoutMS;

        while (System.currentTimeMillis() < endTime && imageCount < 3) {
            imageCount = 0;
            File dir = new File(validationRunner.storageDirectory);

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
}
