package mil.darpa.immortals.modulerunner.generators;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mil.darpa.immortals.modulerunner.configuration.AnalysisGeneratorConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by awellman@bbn.com on 8/5/16.
 */
public enum SemanticType {
    GeneralText(JavaClassType.String),
    JpegFilepath(JavaClassType.String);
//    CotLocationMessage(Void.class),
//    CotImageMessage(Void.class),
//    GeneralLogMessage(Void.class);

    public final JavaClassType producedType;

    SemanticType(JavaClassType producedType) {
        this.producedType = producedType;
    }

    public AnalysisGeneratorConfiguration constructDefaultGenerator() {
        AnalysisGeneratorConfiguration generatorConfiguration;

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("configurations/generators/" + this.name() + ".json");

        if (is == null) {
            throw new RuntimeException("No default configuration for the Semantic Type '" + this.name() + "' has been created!");
        }

        try {
            JsonReader jr = new JsonReader(new InputStreamReader(is));
            generatorConfiguration = (new Gson()).fromJson(jr, AnalysisGeneratorConfiguration.class);
            jr.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return generatorConfiguration;
    }

    public static SemanticType getByOntologySemanticType(String identifier) {
        return null;
    }
}
