package mil.darpa.immortals.examples.tests;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by awellman on 5/8/18.
 */

@RunWith(AndroidJUnit4.class)
public class DummyTest {

    /**
     * This test must:
     *  - Pass with the original library version
     *  - Fail with the newer library version
     *  - Pass with the partial upgraded library version
     */
    @Test
    public void adaptationReasonTest() {

        Assert.assertTrue("this".equals("this"));
    }

    /**
     * This test must:
     *  - Fail with the original library version
     *  - Pass with the newer library version
     *  - Pass with the partial upgraded library version
     */
    @Test
    public void vulnerabilityValidationTest() {
        Assert.assertTrue("this".equals("this"));
    }

