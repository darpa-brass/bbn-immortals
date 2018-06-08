package mil.darpa.immortals.examples.tests;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import mil.darpa.immortals.annotation.dsl.ontology.java.testing.annotation.ProvidedFunctionalityValidationAnnotation;
import mil.darpa.immortals.ontology.BaselineFunctionalAspect;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by awellman on 5/8/18.
 */

//@RunWith(AndroidJUnit4.class)
public class DummyTest {

    /**
     * This test must:
     *  - Pass with the original library version
     *  - Fail with the newer library version
     *  - Pass with the partial upgraded library version
     */
//    @Test
//    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    public void adaptationReasonTest() {

        Assert.assertTrue("this".equals("this"));
    }

    /**
     * This test must:
     *  - Fail with the original library version
     *  - Pass with the newer library version
     *  - Pass with the partial upgraded library version
     */
//    @Test
//    @ProvidedFunctionalityValidationAnnotation(validatedAspects = BaselineFunctionalAspect.class)
    public void vulnerabilityValidationTest() {
        Assert.assertTrue("this".equals("this"));
    }
}
