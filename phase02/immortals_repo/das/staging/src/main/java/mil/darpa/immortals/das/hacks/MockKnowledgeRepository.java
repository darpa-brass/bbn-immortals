package mil.darpa.immortals.das.hacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;
import mil.darpa.immortals.das.sourcecomposer.configuration.paradigms.ConfigurationContainer;
import mil.darpa.immortals.das.hacks.configuration.applications.ApplicationProfile;

import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A class to obtain all the data from a JSON file that should ideally be stored in the knowledge repository
 * 
 * Created by awellman@bbn.com on 5/8/17.
 */
public class MockKnowledgeRepository {

    public final ArrayList<ApplicationProfile> applications;

    public final ArrayList<ConfigurationContainer> dfus;

    public static MockKnowledgeRepository mockKnowledgeRepository = null;

    public static MockKnowledgeRepository getInstance() {
        if (mockKnowledgeRepository == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            InputStreamReader isr = new InputStreamReader(MockKnowledgeRepository.class.getResourceAsStream("/MockKnowledgeRepository.json"));
            mockKnowledgeRepository = gson.fromJson(isr, MockKnowledgeRepository.class);
        }
        return mockKnowledgeRepository;
    }

    private MockKnowledgeRepository(ArrayList<ApplicationProfile> applications,
                                    ArrayList<ConfigurationContainer> dfus) {
        this.applications = applications;
        this.dfus = dfus;
    }

    public ApplicationProfile getApplication(CompositionTarget compositionTarget) throws CompositionException {
        for (ApplicationProfile app : applications) {
            if (app.compositionTarget == compositionTarget) {
                return app;
            }
        }
        throw new CompositionException.ApplicationUuidException(compositionTarget.toString());
    }

}
