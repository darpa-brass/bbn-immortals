package mil.darpa.immortals.core.api.ll.phase2.result;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Directly using a "LinkedList<TestDetails>" object type as the body in REST bodies has issues.
 * This also adds a timestamp
 */
public class TestDetailsList extends LinkedList<TestDetails> {

    public int sequence = -1;

    public TestDetailsList() {
        super();
    }

    public TestDetailsList(Collection<TestDetails> testDetails) {
        super(testDetails);
    }

    private TestDetailsList(int sequence, Collection<TestDetails> testDetails) {
        super(testDetails);
        this.sequence = sequence;
    }

    public TestDetailsList producePendingList() {
        TestDetailsList rval = new TestDetailsList();
        for (TestDetails td : this) {
            TestDetails td2 = td.clone();
            td2.testCaseReport = null;
            td2.currentState = TestOutcome.PENDING;
            td2.detailMessages.clear();
            td2.errorMessages.clear();
            rval.add(td2);
        }
        return rval;
    }

    public TestDetailsList produceRunningList() {
        TestDetailsList rval = new TestDetailsList();
        for (TestDetails td : this) {
            TestDetails td2 = td.clone();
            td2.testCaseReport = null;
            td2.currentState = TestOutcome.RUNNING;
            td2.detailMessages.clear();
            td2.errorMessages.clear();
            rval.add(td2);
        }
        return rval;
    }
    
    public static TestDetailsList fromTestCaseReportSet(@Nonnull String adaptationIdentifier, @Nonnull TestCaseReportSet testCaseReports) {
        TestDetailsList rval = new TestDetailsList();

        for (TestCaseReport tcr : testCaseReports) {
            TestDetails td = new TestDetails(tcr, adaptationIdentifier);
            rval.add(td);
        }
        return rval;
    }

    public static class TestDetailsListDeserializer implements JsonDeserializer<TestDetailsList> {

        @Override
        public TestDetailsList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jo = (JsonObject) json;
            Type type = new TypeToken<LinkedList<TestDetails>>() {
            }.getType();
            return new TestDetailsList(
                    jo.get("sequence").getAsInt(),
                    context.deserialize(jo.getAsJsonArray("values"), type));
        }
    }


    public static class TestDetailsListSerializer implements JsonSerializer<TestDetailsList> {

        @Override
        public JsonElement serialize(TestDetailsList src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject rval = new JsonObject();
            rval.addProperty("sequence", src.sequence);
            JsonArray ja = new JsonArray();

            for (TestDetails td : src) {
                ja.add(context.serialize(td));
            }
            rval.add("values", ja);
            return rval;
        }
    }
}
