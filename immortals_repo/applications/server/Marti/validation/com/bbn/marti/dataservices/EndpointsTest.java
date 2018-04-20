// TODO: hddRASS should be able to handle multiple test result files or we should combine them into one!
//package com.bbn.marti.dataservices;
//
//
//import org.testng.annotations.AfterTest;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.fail;
//
//public class EndpointsTest {
//
//    private DataServiceProxy proxy = null;
//
//    @BeforeTest
//    public void setUp() throws Exception {
//        System.err.println("GSREFSSDD");
//
//        DataService.start("test", DataService.BASE_URL);
//
//        DataServiceConfiguration config = new DataServiceConfiguration(DataService.BASE_URL);
//
//        proxy = new DataServiceProxy(config);
//    }
//
//    @AfterTest
//    public void tearDown() throws Exception {
//
//        DataService.shutdown();
//
//    }
//
//    @Test
//    public void test() {
//
//        try {
//            final String TEST_STRING = "Echo test.";
//            String response = proxy.sendEchoTest(TEST_STRING);
//            assertEquals(TEST_STRING, response);
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//}
